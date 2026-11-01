/*
 * Copyright 2013-2026 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package consulo.shader.impl.type;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.psi.PsiComment;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiWhiteSpace;
import consulo.application.util.CachedValue;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.RecursionManager;
import consulo.language.psi.PsiModificationTracker;
import consulo.language.psi.PsiPolyVariantReference;
import consulo.language.psi.util.LanguageCachedValueUtil;
import consulo.shader.impl.ShaderLanguageSupport;
import consulo.shader.impl.resolve.ShaderOverloads;
import consulo.util.dataholder.Key;
import consulo.shader.psi.ShaderArraySpecifier;
import consulo.shader.psi.ShaderCallExpression;
import consulo.shader.psi.ShaderConstructorExpression;
import consulo.shader.psi.ShaderDeclarationContainer;
import consulo.shader.psi.ShaderFunction;
import consulo.shader.psi.ShaderIndexExpression;
import consulo.shader.psi.ShaderMemberAccess;
import consulo.shader.psi.ShaderNameKind;
import consulo.shader.psi.ShaderNamedElement;
import consulo.shader.psi.ShaderParameter;
import consulo.shader.psi.ShaderPsiUtil;
import consulo.shader.psi.ShaderReferenceElement;
import consulo.shader.psi.ShaderTemplateParameter;
import consulo.shader.psi.ShaderTypeAlias;
import consulo.shader.psi.ShaderTypeDefinition;
import consulo.shader.psi.ShaderTypeSpecifier;
import consulo.shader.psi.ShaderVariable;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Types of expressions and declarations, for member access: what a name, member, index, call or constructor
 * evaluates to. Arithmetic is not evaluated; a type it cannot tell is {@code null}. Types are cached per element until
 * the next PSI change, and cycles (a typedef of itself) are cut by {@link RecursionManager}; what is computed under a
 * cut is not cached.
 */
public final class ShaderTypeEvaluator {
    private static final Key<CachedValue<Optional<ShaderType>>> EXPRESSION_TYPE = Key.create("ShaderTypeEvaluator.EXPRESSION_TYPE");
    private static final Key<CachedValue<Optional<ShaderType>>> DECLARED_TYPE = Key.create("ShaderTypeEvaluator.DECLARED_TYPE");

    private ShaderTypeEvaluator() {
    }

    @RequiredReadAction
    public static @Nullable ShaderType typeOf(@Nullable PsiElement expression) {
        if (expression == null) {
            return null;
        }
        return LanguageCachedValueUtil.getCachedValue(expression, EXPRESSION_TYPE, () -> CachedValueProvider.Result.create(
            Optional.ofNullable(RecursionManager.doPreventingRecursion(expression, false, () -> doTypeOf(expression))),
            PsiModificationTracker.MODIFICATION_COUNT)).orElse(null);
    }

    @RequiredReadAction
    private static @Nullable ShaderType doTypeOf(PsiElement expression) {
        if (expression instanceof ShaderMemberAccess access) {
            ShaderType qualifier = typeOf(access.getQualifier());
            if (qualifier == null) {
                return null;
            }
            String name = access.getReferenceNameElement().getText();
            ShaderNamedElement member = findMember(qualifier, name);
            return member != null ? substitute(declaredType(member), qualifier, null) : ShaderVectorTypes.swizzle(qualifier, name);
        }
        if (expression instanceof ShaderReferenceElement reference) {
            return reference.getNameKind() == ShaderNameKind.VALUE && resolve(reference) instanceof ShaderNamedElement target
                ? declaredType(target) : null;
        }
        if (expression instanceof ShaderIndexExpression index) {
            ShaderType base = typeOf(index.getBase());
            if (base == null) {
                return null;
            }
            if (base.arrayDepth() > 0) {
                return base.withArrayDepth(base.arrayDepth() - 1);
            }
            return base.element() != null ? base.element() : ShaderVectorTypes.index(base);
        }
        if (expression instanceof ShaderCallExpression call) {
            return typeOfCall(call);
        }
        if (expression instanceof ShaderConstructorExpression constructor) {
            return typeOfSpecifier(constructor);
        }
        // wrappers (parentheses, initializers) and unary operators: the type of their single operand
        List<PsiElement> children = ShaderPsiUtil.compositeChildren(expression);
        return children.size() == 1 ? typeOf(children.get(0)) : null;
    }

    @RequiredReadAction
    private static @Nullable ShaderType typeOfCall(ShaderCallExpression call) {
        PsiElement callee = call.getCallee();
        if (callee instanceof ShaderConstructorExpression constructor) {
            return typeOfSpecifier(constructor);
        }
        if (!(callee instanceof ShaderReferenceElement reference) || !(resolve(reference) instanceof ShaderFunction function)) {
            return null;
        }
        ShaderType owner = callee instanceof ShaderMemberAccess access ? typeOf(access.getQualifier()) : null;
        return substitute(declaredType(function), owner, call.getArguments().isEmpty() ? null : new CallSite(function, call.getArguments()));
    }

    /**
     * The members of a type with that name: fields and methods of a struct or built-in class, members of an interface
     * block. A method definition wins over its prototype.
     */
    @RequiredReadAction
    public static @Nullable ShaderNamedElement findMember(ShaderType type, String name) {
        ShaderNamedElement[] found = new ShaderNamedElement[1];
        processMembers(type, member -> {
            if (!name.equals(member.getReferencedName())) {
                return true;
            }
            boolean prototype = member instanceof ShaderFunction function && !function.isDefinition();
            if (found[0] == null || !prototype) {
                found[0] = member;
            }
            return prototype;
        });
        return found[0];
    }

    @RequiredReadAction
    public static void processMembers(ShaderType type, Predicate<? super ShaderNamedElement> processor) {
        ShaderNamedElement container = type.declaration();
        if (type.arrayDepth() == 0 && container != null && !(container instanceof ShaderTemplateParameter)) {
            ShaderDeclarationContainer.processContents(container, ShaderNameKind.VALUE, processor);
        }
    }

    /**
     * The type a declaration declares: of a variable or parameter its type, of a function its return type, of a struct
     * or an interface block itself.
     */
    @RequiredReadAction
    public static @Nullable ShaderType declaredType(ShaderNamedElement declaration) {
        return LanguageCachedValueUtil.getCachedValue(declaration, DECLARED_TYPE, () -> CachedValueProvider.Result.create(
            Optional.ofNullable(RecursionManager.doPreventingRecursion(declaration, false, () -> doDeclaredType(declaration))),
            PsiModificationTracker.MODIFICATION_COUNT)).orElse(null);
    }

    @RequiredReadAction
    private static @Nullable ShaderType doDeclaredType(ShaderNamedElement declaration) {
        int arrays = ShaderPsiUtil.countChildren(declaration, ShaderArraySpecifier.class);
        if (declaration instanceof ShaderFunction || declaration instanceof ShaderParameter) {
            ShaderType type = typeOfSpecifier(ShaderPsiUtil.findChild(declaration, ShaderTypeSpecifier.class));
            return type == null ? null : type.withArrayDepth(type.arrayDepth() + arrays);
        }
        if (declaration instanceof ShaderVariable || declaration instanceof ShaderTypeAlias) {
            PsiElement parent = declaration.getParent();
            ShaderTypeSpecifier specifier = parent == null ? null : ShaderPsiUtil.findChild(parent, ShaderTypeSpecifier.class);
            ShaderType type;
            if (specifier != null) {
                type = typeOfSpecifier(specifier);
            }
            else {
                ShaderTypeDefinition struct = parent == null ? null : ShaderPsiUtil.findChild(parent, ShaderTypeDefinition.class);
                type = struct == null || struct == declaration ? null : new ShaderType(struct.getName(), struct, null, 0);
            }
            return type == null ? null : type.withArrayDepth(type.arrayDepth() + arrays);
        }
        // a struct, a template parameter, an interface block instance: its own members
        return new ShaderType(declaration.getName(), declaration, null, arrays);
    }

    /**
     * The type a type specifier or constructor names.
     */
    @RequiredReadAction
    public static @Nullable ShaderType typeOfSpecifier(@Nullable PsiElement specifier) {
        if (specifier == null) {
            return null;
        }
        ShaderType type = null;
        ShaderType element = null;
        int arrays = 0;
        for (PsiElement child = specifier.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof PsiWhiteSpace || child instanceof PsiComment) {
                continue;
            }
            if (child instanceof ShaderArraySpecifier) {
                arrays++;
            }
            else if (type == null && child instanceof ShaderReferenceElement reference) {
                PsiElement target = resolve(reference);
                if (target instanceof ShaderTypeAlias alias) {
                    type = declaredType(alias);
                }
                else if (target instanceof ShaderTypeDefinition definition) {
                    type = new ShaderType(definition.getName(), definition, null, 0);
                }
                else {
                    type = builtin(specifier, reference.getReferenceNameElement().getText());
                }
            }
            else if (type == null && child instanceof ShaderTypeDefinition struct) {
                type = new ShaderType(struct.getName(), struct, null, 0);
            }
            else if (type == null && child.getFirstChild() == null && !child.getText().equals("struct")) {
                type = builtin(specifier, child.getText());
            }
            else if (element == null && child.getFirstChild() != null) {
                // template arguments: <float4>, <Light>, <float, 4>
                ShaderTypeSpecifier argument = ShaderPsiUtil.findChild(child, ShaderTypeSpecifier.class);
                element = argument == null ? null : typeOfSpecifier(argument);
            }
        }
        if (type == null) {
            return null;
        }
        return new ShaderType(type.name(), type.declaration(), element != null ? element : type.element(), type.arrayDepth() + arrays);
    }

    @RequiredReadAction
    private static ShaderType builtin(PsiElement place, String name) {
        List<? extends ShaderNamedElement> classes = ShaderLanguageSupport.forLanguage(place.getLanguage()).findBuiltins(place, name, ShaderNameKind.TYPE);
        return new ShaderType(name, classes.isEmpty() ? null : classes.get(0), null, 0);
    }

    /**
     * Replaces a template parameter in {@code type}: by the template argument of the owner of a member, by its default,
     * or for a generic function by the type of the argument declared with it.
     */
    @RequiredReadAction
    private static @Nullable ShaderType substitute(@Nullable ShaderType type, @Nullable ShaderType owner, @Nullable CallSite call) {
        if (type == null || !(type.declaration() instanceof ShaderTemplateParameter parameter)) {
            return type;
        }
        ShaderType replacement = null;
        if (owner != null && owner.element() != null) {
            replacement = owner.element();
        }
        if (replacement == null && call != null) {
            replacement = call.argumentTypeOf(parameter);
        }
        if (replacement == null) {
            replacement = typeOfSpecifier(ShaderPsiUtil.findChild(parameter, ShaderTypeSpecifier.class));
        }
        return replacement == null ? null : replacement.withArrayDepth(replacement.arrayDepth() + type.arrayDepth());
    }

    /**
     * The declaration a name or member means; of overloads the first valid one.
     */
    @RequiredReadAction
    private static @Nullable PsiElement resolve(ShaderReferenceElement reference) {
        PsiReference psiReference = reference.getReference();
        if (psiReference instanceof PsiPolyVariantReference poly) {
            return ShaderOverloads.bestTarget(poly.multiResolve(false));
        }
        return psiReference == null ? null : psiReference.resolve();
    }

    private record CallSite(ShaderFunction function, List<PsiElement> arguments) {
        @RequiredReadAction
        @Nullable ShaderType argumentTypeOf(ShaderTemplateParameter parameter) {
            List<? extends ShaderParameter> parameters = function.getShaderParameters();
            for (int i = 0; i < parameters.size() && i < arguments.size(); i++) {
                ShaderType declared = declaredType(parameters.get(i));
                if (declared != null && declared.declaration() == parameter) {
                    return typeOf(arguments.get(i));
                }
            }
            return null;
        }
    }
}

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
package consulo.shader.impl.resolve;

import consulo.annotation.access.RequiredReadAction;
import consulo.document.util.TextRange;
import consulo.language.impl.ast.LeafElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiPolyVariantReferenceBase;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.resolve.ResolveCache;
import consulo.language.util.IncorrectOperationException;
import consulo.shader.impl.ShaderLanguageSupport;
import consulo.shader.psi.ShaderDeclarationContainer;
import consulo.shader.psi.ShaderNameKind;
import consulo.shader.psi.ShaderNamedElement;
import consulo.shader.psi.ShaderReferenceElement;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.ArrayList;
import java.util.List;

/**
 * The reference of a {@link ShaderReferenceElement}: the declarations the tree walk finds, else the language's
 * built-ins. Poly-variant for overloaded functions; results are cached in {@link ResolveCache}, keyed by the element.
 */
public class ShaderNameReference extends PsiPolyVariantReferenceBase<ShaderReferenceElement> {
    private static final ResolveCache.PolyVariantContextResolver<ShaderNameReference> RESOLVER =
        (reference, file, incompleteCode) -> reference.doMultiResolve();

    @RequiredReadAction
    public ShaderNameReference(ShaderReferenceElement element) {
        super(element, rangeOf(element), false);
    }

    @RequiredReadAction
    static TextRange rangeOf(ShaderReferenceElement element) {
        return element.getReferenceNameElement().getTextRange().shiftRight(-element.getTextRange().getStartOffset());
    }

    @RequiredReadAction
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        PsiFile file = getElement().getContainingFile();
        return file == null ? doMultiResolve() : ResolveCache.getInstance(file.getProject()).resolveWithCaching(this, RESOLVER, true, incompleteCode, file);
    }

    @RequiredReadAction
    private ResolveResult[] doMultiResolve() {
        ShaderReferenceElement element = getElement();
        String name = getValue();
        PsiElement qualifier = element.getQualifierNameElement();
        if (qualifier != null) {
            // Light::Count, Test::staticMethod(): a member of the type the qualifier names
            List<ShaderNamedElement> members = qualifiedMembers(element, qualifier.getText(), name);
            if (!members.isEmpty()) {
                return ShaderOverloads.toResults(element, members);
            }
        }
        ShaderResolveProcessor processor = new ShaderResolveProcessor(name, element.getNameKind());
        ShaderResolver.walk(processor, element);
        List<? extends ShaderNamedElement> declarations = processor.getResults();
        if (declarations.isEmpty() && element.getNameKind() == ShaderNameKind.VALUE && ShaderOverloads.isCallee(element)) {
            // a constructor call names a struct: Light(dir, 1.0)
            ShaderResolveProcessor types = new ShaderResolveProcessor(name, ShaderNameKind.TYPE);
            ShaderResolver.walk(types, element);
            declarations = types.getResults();
        }
        if (declarations.isEmpty()) {
            declarations = ShaderLanguageSupport.forLanguage(element.getLanguage()).findBuiltins(element, name, element.getNameKind());
        }
        return ShaderOverloads.toResults(element, declarations);
    }

    @RequiredReadAction
    private static List<ShaderNamedElement> qualifiedMembers(ShaderReferenceElement element, String qualifier, String name) {
        ShaderResolveProcessor types = new ShaderResolveProcessor(qualifier, ShaderNameKind.TYPE);
        ShaderResolver.walk(types, element);
        List<ShaderNamedElement> members = new ArrayList<>();
        for (ShaderNamedElement type : types.getResults()) {
            ShaderDeclarationContainer.processContents(type, element.getNameKind(), member -> {
                if (name.equals(member.getReferencedName())) {
                    members.add(member);
                }
                return true;
            });
        }
        return members;
    }

    /**
     * The declaration when the name means exactly one valid one.
     */
    @RequiredReadAction
    @Override
    public @Nullable PsiElement resolve() {
        PsiElement single = null;
        for (ResolveResult result : multiResolve(false)) {
            if (result.isValidResult()) {
                if (single != null) {
                    return null;
                }
                single = result.getElement();
            }
        }
        return single;
    }

    @RequiredReadAction
    @Override
    public boolean isReferenceTo(PsiElement element) {
        // names first: Find Usages asks this at every textual occurrence
        return element instanceof ShaderNamedElement declaration && getValue().equals(declaration.getReferencedName())
            && super.isReferenceTo(element);
    }

    @RequiredReadAction
    @Override
    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        if (!(getElement().getReferenceNameElement().getNode() instanceof LeafElement leaf)) {
            throw new IncorrectOperationException("Not an identifier: " + getElement());
        }
        leaf.replaceWithText(newElementName);
        return getElement();
    }

    // the cache key: one reference per element
    @Override
    public boolean equals(Object o) {
        return o instanceof ShaderNameReference other && other.getClass() == getClass() && other.getElement() == getElement();
    }

    @Override
    public int hashCode() {
        return getElement().hashCode();
    }
}

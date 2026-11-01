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
import consulo.language.impl.ast.LeafElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiPolyVariantReferenceBase;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.resolve.ResolveCache;
import consulo.language.util.IncorrectOperationException;
import consulo.shader.impl.type.ShaderType;
import consulo.shader.impl.type.ShaderTypeEvaluator;
import consulo.shader.psi.ShaderFunction;
import consulo.shader.psi.ShaderMemberAccess;
import consulo.shader.psi.ShaderNamedElement;

import java.util.ArrayList;
import java.util.List;

/**
 * The reference of a member name, resolved in the type of the qualifier: a field or method of a struct, a member of an
 * interface block, a method of a built-in type. Poly-variant for overloaded methods, cached in {@link ResolveCache}.
 * A swizzle ({@code v.xyz}) has no declaration and resolves to nothing; the reference is soft.
 */
public class ShaderMemberReference extends PsiPolyVariantReferenceBase<ShaderMemberAccess> {
    private static final ResolveCache.PolyVariantContextResolver<ShaderMemberReference> RESOLVER =
        (reference, file, incompleteCode) -> reference.doMultiResolve();

    @RequiredReadAction
    public ShaderMemberReference(ShaderMemberAccess element) {
        super(element, ShaderNameReference.rangeOf(element), true);
    }

    @RequiredReadAction
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        PsiFile file = getElement().getContainingFile();
        return file == null ? doMultiResolve() : ResolveCache.getInstance(file.getProject()).resolveWithCaching(this, RESOLVER, true, incompleteCode, file);
    }

    @RequiredReadAction
    private ResolveResult[] doMultiResolve() {
        ShaderType type = ShaderTypeEvaluator.typeOf(getElement().getQualifier());
        if (type == null) {
            return ResolveResult.EMPTY_ARRAY;
        }
        String name = getValue();
        List<ShaderNamedElement> members = new ArrayList<>();
        ShaderTypeEvaluator.processMembers(type, member -> {
            if (name.equals(member.getReferencedName())) {
                members.add(member);
                // every overload of a method, the first of anything else
                return member instanceof ShaderFunction;
            }
            return true;
        });
        return ShaderOverloads.toResults(getElement(), members);
    }

    @RequiredReadAction
    @Override
    public boolean isReferenceTo(PsiElement element) {
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
        return o instanceof ShaderMemberReference other && other.getElement() == getElement();
    }

    @Override
    public int hashCode() {
        return getElement().hashCode();
    }
}

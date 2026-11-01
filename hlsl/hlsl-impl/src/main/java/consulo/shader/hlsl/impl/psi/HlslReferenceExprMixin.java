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
package consulo.shader.hlsl.impl.psi;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.shader.hlsl.impl.psi.impl.HlslExpressionImpl;
import consulo.shader.impl.resolve.ShaderNameReference;
import consulo.shader.psi.ShaderNameKind;
import consulo.shader.psi.ShaderReferenceElement;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * The {@code mixin} of a name in an expression: {@code x}, {@code ::x}, {@code Light::Count}, {@code f<float>}. The
 * reference is on the last part of the name.
 */
public abstract class HlslReferenceExprMixin extends HlslExpressionImpl implements ShaderReferenceElement {
    protected HlslReferenceExprMixin(ASTNode node) {
        super(node);
    }

    @Override
    public ShaderNameKind getNameKind() {
        return ShaderNameKind.VALUE;
    }

    @RequiredReadAction
    @Override
    public PsiElement getReferenceNameElement() {
        ASTNode[] names = getNode().getChildren(HlslNamedElementMixin.NAME_TOKENS);
        return Objects.requireNonNull(names[names.length - 1].getPsi());
    }

    @RequiredReadAction
    @Override
    public @Nullable PsiElement getQualifierNameElement() {
        ASTNode[] names = getNode().getChildren(HlslNamedElementMixin.NAME_TOKENS);
        return names.length < 2 ? null : names[names.length - 2].getPsi();
    }

    @RequiredReadAction
    @Override
    public PsiReference getReference() {
        return new ShaderNameReference(this);
    }
}

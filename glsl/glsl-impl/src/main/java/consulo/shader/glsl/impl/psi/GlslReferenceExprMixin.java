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
package consulo.shader.glsl.impl.psi;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.shader.glsl.impl.psi.impl.GlslExpressionImpl;
import consulo.shader.impl.resolve.ShaderNameReference;
import consulo.shader.psi.ShaderNameKind;
import consulo.shader.psi.ShaderReferenceElement;

import java.util.Objects;

/**
 * The {@code mixin} of a name in an expression; it is a single identifier.
 */
public abstract class GlslReferenceExprMixin extends GlslExpressionImpl implements ShaderReferenceElement {
    protected GlslReferenceExprMixin(ASTNode node) {
        super(node);
    }

    @Override
    public ShaderNameKind getNameKind() {
        return ShaderNameKind.VALUE;
    }

    @RequiredReadAction
    @Override
    public PsiElement getReferenceNameElement() {
        return Objects.requireNonNull(getFirstChild());
    }

    @RequiredReadAction
    @Override
    public PsiReference getReference() {
        return new ShaderNameReference(this);
    }
}

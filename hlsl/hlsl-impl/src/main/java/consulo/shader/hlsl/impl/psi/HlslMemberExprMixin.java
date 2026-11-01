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
import consulo.language.ast.TokenSet;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.shader.hlsl.impl.psi.impl.HlslExpressionImpl;
import consulo.shader.impl.resolve.ShaderMemberReference;
import consulo.shader.psi.ShaderMemberAccess;
import consulo.shader.psi.ShaderNameKind;

import java.util.Objects;

/**
 * The {@code mixin} of a member access; the reference is on the member name.
 */
public abstract class HlslMemberExprMixin extends HlslExpressionImpl implements ShaderMemberAccess {
    private static final TokenSet MEMBER_NAME = TokenSet.create(HlslTypes.IDENTIFIER);

    protected HlslMemberExprMixin(ASTNode node) {
        super(node);
    }

    @Override
    public ShaderNameKind getNameKind() {
        return ShaderNameKind.VALUE;
    }

    @RequiredReadAction
    @Override
    public PsiElement getReferenceNameElement() {
        ASTNode[] names = getNode().getChildren(MEMBER_NAME);
        return Objects.requireNonNull(names[names.length - 1].getPsi());
    }

    @RequiredReadAction
    @Override
    public PsiReference getReference() {
        return new ShaderMemberReference(this);
    }
}

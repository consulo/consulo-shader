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
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import consulo.shader.glsl.impl.psi.impl.GlslStatementImpl;
import consulo.shader.impl.resolve.ShaderScopes;
import consulo.shader.psi.ShaderLocalScope;
import org.jspecify.annotations.Nullable;

/**
 * The {@code mixin} of statements that are scopes (blocks, for, if and while statements): their declarations are
 * visible after them.
 */
public abstract class GlslLocalScopeMixin extends GlslStatementImpl implements ShaderLocalScope {
    protected GlslLocalScopeMixin(ASTNode node) {
        super(node);
    }

    @RequiredReadAction
    @Override
    public boolean processDeclarations(PsiScopeProcessor processor, ResolveState state, @Nullable PsiElement lastParent, PsiElement place) {
        return ShaderScopes.processLocalScope(this, processor, state, lastParent);
    }
}

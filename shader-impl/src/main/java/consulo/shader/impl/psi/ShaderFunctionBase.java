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
package consulo.shader.impl.psi;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import consulo.shader.impl.resolve.ShaderScopes;
import consulo.shader.psi.ShaderDeclarationProvider;
import consulo.shader.psi.ShaderFunction;
import consulo.shader.psi.ShaderLocalScope;
import consulo.shader.psi.ShaderNameKind;
import consulo.shader.psi.ShaderNamedElement;
import consulo.shader.psi.ShaderParameter;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * The base of function definitions and prototypes of generated PSI: the parameters are the children of the parameter
 * list child, the body is the block child.
 */
public abstract class ShaderFunctionBase extends ShaderNamedElementBase implements ShaderFunction, ShaderDeclarationProvider {
    protected ShaderFunctionBase(ASTNode node) {
        super(node);
    }

    @RequiredReadAction
    @Override
    public List<? extends ShaderParameter> getShaderParameters() {
        for (PsiElement child = getFirstChild(); child != null; child = child.getNextSibling()) {
            List<ShaderParameter> parameters = new ArrayList<>();
            for (PsiElement grandChild = child.getFirstChild(); grandChild != null; grandChild = grandChild.getNextSibling()) {
                if (grandChild instanceof ShaderParameter parameter) {
                    parameters.add(parameter);
                }
            }
            if (!parameters.isEmpty()) {
                return parameters;
            }
        }
        return List.of();
    }

    @RequiredReadAction
    @Override
    public @Nullable PsiElement getBody() {
        for (PsiElement child = getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof ShaderLocalScope) {
                return child;
            }
        }
        return null;
    }

    /**
     * The parameters, to the body.
     */
    @RequiredReadAction
    @Override
    public boolean processDeclarations(PsiScopeProcessor processor, ResolveState state, @Nullable PsiElement lastParent, PsiElement place) {
        return ShaderScopes.processFunction(this, processor, state, lastParent);
    }

    @RequiredReadAction
    @Override
    public boolean processShaderDeclarations(ShaderNameKind kind, Predicate<? super ShaderNamedElement> processor) {
        return ShaderDeclarationProvider.processIf(this, kind, processor);
    }
}

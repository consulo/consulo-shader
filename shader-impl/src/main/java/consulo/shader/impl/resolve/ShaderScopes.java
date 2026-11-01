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
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import consulo.shader.psi.ShaderDeclarationContainer;
import consulo.shader.psi.ShaderDeclarationProvider;
import consulo.shader.psi.ShaderFunction;
import consulo.shader.psi.ShaderNameKind;
import consulo.shader.psi.ShaderParameter;
import consulo.shader.psi.ShaderTypeDefinition;
import consulo.shader.psi.ShaderVariable;
import org.jspecify.annotations.Nullable;

/**
 * The scope rules of shader languages, for the {@code processDeclarations} of scope elements. A scope offers only
 * when the walk comes from inside it ({@code lastParent} is its child): asked from outside, a block, a function or a
 * struct exposes nothing.
 */
public final class ShaderScopes {
    private ShaderScopes() {
    }

    /**
     * A block, a for or if statement: the declarations before the child the walk comes from, nearest first, so that
     * a local is visible after its declaration and the latest one shadows the earlier.
     */
    @RequiredReadAction
    public static boolean processLocalScope(PsiElement scope, PsiScopeProcessor processor, ResolveState state,
                                            @Nullable PsiElement lastParent) {
        if (lastParent == null || lastParent.getParent() != scope) {
            return true;
        }
        for (PsiElement sibling = lastParent.getPrevSibling(); sibling != null; sibling = sibling.getPrevSibling()) {
            if (sibling instanceof ShaderDeclarationProvider provider && !offer(provider, processor, state)) {
                return false;
            }
        }
        return true;
    }

    /**
     * A file, a struct with methods, a namespace, a template: every declaration inside, wherever the walk comes from.
     */
    @RequiredReadAction
    public static boolean processUnorderedScope(PsiElement scope, PsiScopeProcessor processor, ResolveState state,
                                                @Nullable PsiElement lastParent) {
        if (lastParent == null) {
            return true;
        }
        for (ShaderNameKind kind : kinds(processor)) {
            if (!ShaderDeclarationContainer.processContents(scope, kind, declaration -> processor.execute(declaration, state))) {
                return false;
            }
        }
        return true;
    }

    /**
     * A function: its parameters, visible from its body only.
     */
    @RequiredReadAction
    public static boolean processFunction(ShaderFunction function, PsiScopeProcessor processor, ResolveState state,
                                          @Nullable PsiElement lastParent) {
        if (lastParent == null || lastParent != function.getBody() || processor.getHint(ShaderResolveHints.KIND) == ShaderNameKind.TYPE) {
            return true;
        }
        for (ShaderParameter parameter : function.getShaderParameters()) {
            if (parameter.getNameIdentifier() != null && !processor.execute(parameter, state)) {
                return false;
            }
        }
        return true;
    }

    @RequiredReadAction
    private static boolean offer(ShaderDeclarationProvider provider, PsiScopeProcessor processor, ResolveState state) {
        for (ShaderNameKind kind : kinds(processor)) {
            if (!provider.processShaderDeclarations(kind, declaration -> processor.execute(declaration, state))) {
                return false;
            }
        }
        return true;
    }

    private static ShaderNameKind[] kinds(PsiScopeProcessor processor) {
        ShaderNameKind kind = processor.getHint(ShaderResolveHints.KIND);
        return kind == null ? ShaderNameKind.values() : new ShaderNameKind[]{kind};
    }

    /**
     * The function a local variable or parameter belongs to, else {@code null}: what limits its usages.
     */
    @RequiredReadAction
    public static @Nullable ShaderFunction localOwner(PsiElement element) {
        if (!(element instanceof ShaderParameter) && !(element instanceof ShaderVariable)) {
            return null;
        }
        for (PsiElement parent = element.getParent(); parent != null && !(parent instanceof PsiFile); parent = parent.getParent()) {
            if (parent instanceof ShaderFunction function) {
                return function;
            }
            // a field of a struct declared in a function is still a member
            if (parent instanceof ShaderTypeDefinition) {
                return null;
            }
        }
        return null;
    }
}

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
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.PsiScopesUtilCore;
import consulo.shader.psi.ShaderNameKind;
import consulo.shader.psi.ShaderNamedElement;

import java.util.List;

/**
 * Name resolution of every shader language: a tree walk up from the reference (by context, so that injected and
 * generated code resolves in its host) in which every scope offers its declarations through
 * {@code processDeclarations}, see {@link ShaderScopes}.
 */
public final class ShaderResolver {
    private ShaderResolver() {
    }

    @RequiredReadAction
    public static void walk(PsiScopeProcessor processor, PsiElement place) {
        PsiScopesUtilCore.treeWalkUp(processor, place, null);
    }

    /**
     * The declarations visible at {@code place}, one per name, nearest first: what completion offers.
     */
    @RequiredReadAction
    public static List<ShaderNamedElement> collectVisible(PsiElement place, ShaderNameKind kind) {
        ShaderResolveProcessor processor = new ShaderResolveProcessor(null, kind);
        walk(processor, place);
        return processor.getResults();
    }
}

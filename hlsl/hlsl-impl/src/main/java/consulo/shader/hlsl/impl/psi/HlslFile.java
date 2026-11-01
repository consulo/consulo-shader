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
import consulo.language.file.FileViewProvider;
import consulo.language.impl.psi.PsiFileBase;
import consulo.language.psi.PsiElement;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import consulo.shader.hlsl.HlslLanguage;
import consulo.shader.impl.resolve.ShaderScopes;
import org.jspecify.annotations.Nullable;

public class HlslFile extends PsiFileBase {
    public HlslFile(FileViewProvider viewProvider) {
        super(viewProvider, HlslLanguage.INSTANCE);
    }

    /**
     * Top-level declarations are visible anywhere in the file, also before them.
     */
    @RequiredReadAction
    @Override
    public boolean processDeclarations(PsiScopeProcessor processor, ResolveState state, @Nullable PsiElement lastParent, PsiElement place) {
        return ShaderScopes.processUnorderedScope(this, processor, state, lastParent);
    }
}

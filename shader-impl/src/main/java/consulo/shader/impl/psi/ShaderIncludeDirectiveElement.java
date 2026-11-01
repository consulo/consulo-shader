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
import consulo.language.impl.psi.ASTWrapperPsiElement;
import consulo.language.psi.PsiFile;
import org.jspecify.annotations.Nullable;

/**
 * {@code #include "path"} or {@code #include <path>} in any shader language.
 */
public class ShaderIncludeDirectiveElement extends ASTWrapperPsiElement {
    public ShaderIncludeDirectiveElement(ASTNode node) {
        super(node);
    }

    @RequiredReadAction
    public @Nullable ShaderIncludePathElement getPath() {
        return findChildByClass(ShaderIncludePathElement.class);
    }

    /**
     * The file this directive includes, when it can be found.
     */
    @RequiredReadAction
    public @Nullable PsiFile getIncludedFile() {
        ShaderIncludePathElement path = getPath();
        return path == null ? null : path.getReference().resolve() instanceof PsiFile file ? file : null;
    }
}

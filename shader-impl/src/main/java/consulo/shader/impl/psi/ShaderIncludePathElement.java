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
import consulo.language.psi.PsiReference;
import consulo.shader.include.ShaderIncludeReference;

/**
 * The path of an {@code #include}, with its delimiters.
 */
public class ShaderIncludePathElement extends ASTWrapperPsiElement {
    public ShaderIncludePathElement(ASTNode node) {
        super(node);
    }

    @RequiredReadAction
    @Override
    public PsiReference getReference() {
        return new ShaderIncludeReference(this);
    }

    @RequiredReadAction
    @Override
    public PsiReference[] getReferences() {
        return new PsiReference[]{getReference()};
    }
}

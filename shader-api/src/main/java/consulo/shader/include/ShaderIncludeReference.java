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
package consulo.shader.include;

import consulo.annotation.access.RequiredReadAction;
import consulo.document.util.TextRange;
import consulo.language.inject.InjectedLanguageManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.language.psi.PsiReferenceBase;
import consulo.language.util.IncorrectOperationException;
import consulo.util.collection.ArrayUtil;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

/**
 * The reference of an include path element, whose whole text is the path with its delimiters. In a shader injected
 * into another file (a ShaderLab program block, a string literal) paths are resolved from the host file.
 */
public class ShaderIncludeReference extends PsiReferenceBase<PsiElement> {
    public ShaderIncludeReference(PsiElement pathElement) {
        super(pathElement, rangeOf(pathElement.getText()), false);
    }

    private static TextRange rangeOf(String text) {
        String path = ShaderIncludes.stripDelimiters(text);
        int start = path.length() == text.length() ? 0 : 1;
        return TextRange.from(start, path.length());
    }

    @RequiredReadAction
    @Override
    public @Nullable PsiElement resolve() {
        PsiElement element = getElement();
        PsiFile file = InjectedLanguageManager.getInstance(element.getProject()).getTopLevelFile(element);
        if (file == null) {
            return null;
        }
        VirtualFile includingFile = file.getOriginalFile().getVirtualFile();
        if (includingFile == null) {
            return null;
        }
        String text = element.getText();
        VirtualFile included = ShaderIncludes.resolve(element.getProject(), includingFile, ShaderIncludes.stripDelimiters(text),
            ShaderIncludeKind.of(text));
        return included == null ? null : PsiManager.getInstance(element.getProject()).findFile(included);
    }

    @Override
    public Object[] getVariants() {
        return ArrayUtil.EMPTY_OBJECT_ARRAY;
    }

    @Override
    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        // include paths are not rewritten on rename yet
        return getElement();
    }
}

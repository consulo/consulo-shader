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
package consulo.shader.psi;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.psi.PsiComment;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiWhiteSpace;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Structural helpers for the default methods of the shader PSI interfaces, which see generated PSI only as a tree.
 */
public final class ShaderPsiUtil {
    private ShaderPsiUtil() {
    }

    /**
     * The composite children of {@code element}: elements, not tokens, whitespace or comments.
     */
    @RequiredReadAction
    public static List<PsiElement> compositeChildren(PsiElement element) {
        List<PsiElement> children = new ArrayList<>();
        for (PsiElement child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getFirstChild() != null && !(child instanceof PsiComment) && !(child instanceof PsiWhiteSpace)) {
                children.add(child);
            }
        }
        return children;
    }

    @RequiredReadAction
    public static @Nullable PsiElement firstCompositeChild(PsiElement element) {
        List<PsiElement> children = compositeChildren(element);
        return children.isEmpty() ? null : children.get(0);
    }

    @RequiredReadAction
    public static <T> @Nullable T findChild(PsiElement element, Class<T> type) {
        for (PsiElement child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (type.isInstance(child)) {
                return type.cast(child);
            }
        }
        return null;
    }

    @RequiredReadAction
    public static int countChildren(PsiElement element, Class<?> type) {
        int count = 0;
        for (PsiElement child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (type.isInstance(child)) {
                count++;
            }
        }
        return count;
    }
}

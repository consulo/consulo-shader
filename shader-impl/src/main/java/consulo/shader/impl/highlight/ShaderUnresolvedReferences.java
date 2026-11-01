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
package consulo.shader.impl.highlight;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.psi.PsiComment;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiPolyVariantReference;
import consulo.language.psi.PsiReference;
import consulo.language.psi.util.LanguageCachedValueUtil;
import consulo.shader.impl.ShaderLanguageSupport;
import consulo.shader.psi.ShaderMemberAccess;
import consulo.shader.psi.ShaderReferenceElement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Which unresolved names are reported. Without a preprocessor and without resolve across includes, a name is reported
 * only where nothing else can declare it: not in a file with an {@code #include} (until included files are searched),
 * not a name the file {@code #define}s, not in a program block injected into another file (its blocks share names),
 * not a member (members of unknown types and swizzles are soft), not a name reserved for the compiler ({@code __LINE__}),
 * and not a name the language knows implicitly.
 */
public final class ShaderUnresolvedReferences {
    private static final Pattern DEFINE = Pattern.compile("^#\\s*define\\s+([A-Za-z_]\\w*)");
    private static final Pattern INCLUDE = Pattern.compile("^#\\s*include\\b");

    private ShaderUnresolvedReferences() {
    }

    private record Preprocessing(boolean includes, Set<String> defines) {
    }

    @RequiredReadAction
    public static boolean isReported(ShaderReferenceElement reference) {
        if (reference instanceof ShaderMemberAccess) {
            return false;
        }
        PsiReference psiReference = reference.getReference();
        if (psiReference == null || psiReference.isSoft()) {
            return false;
        }
        boolean resolved = psiReference instanceof PsiPolyVariantReference poly ? poly.multiResolve(false).length > 0 : psiReference.resolve() != null;
        if (resolved) {
            return false;
        }
        PsiFile file = reference.getContainingFile();
        // an injected file has its host as context
        if (file == null || file.getContext() != null) {
            return false;
        }
        Preprocessing preprocessing = preprocessing(file);
        String name = reference.getReferenceNameElement().getText();
        // __LINE__, __HLSL_VERSION and the other names reserved for the compiler
        return !preprocessing.includes() && !preprocessing.defines().contains(name) && !name.startsWith("__")
            && !ShaderLanguageSupport.forLanguage(reference.getLanguage()).isImplicitlyDeclared(reference, name, reference.getNameKind());
    }

    @RequiredReadAction
    private static Preprocessing preprocessing(PsiFile file) {
        return LanguageCachedValueUtil.getProjectPsiDependentCache(file, ShaderUnresolvedReferences::computePreprocessing);
    }

    @RequiredReadAction
    private static Preprocessing computePreprocessing(PsiFile file) {
        boolean includes = false;
        Set<String> defines = new HashSet<>();
        List<PsiComment> comments = new ArrayList<>();
        collectComments(file, comments);
        for (PsiComment comment : comments) {
            String text = comment.getText();
            if (!text.startsWith("#")) {
                continue;
            }
            Matcher define = DEFINE.matcher(text);
            if (define.find()) {
                defines.add(define.group(1));
            }
            else if (INCLUDE.matcher(text).find()) {
                includes = true;
            }
        }
        return new Preprocessing(includes, Set.copyOf(defines));
    }

    @RequiredReadAction
    private static void collectComments(PsiElement element, List<PsiComment> comments) {
        for (PsiElement child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof PsiComment comment) {
                comments.add(comment);
            }
            else {
                collectComments(child, comments);
            }
        }
    }
}

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
package consulo.shader.glsl.impl.version;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.version.LanguageVersion;
import consulo.language.version.LanguageVersionResolver;
import consulo.language.version.LanguageVersionWithDefinition;
import consulo.project.Project;
import consulo.shader.glsl.GlslLanguage;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * The dialect of a GLSL file: the first version that claims it ({@link LanguageVersionWithDefinition}), otherwise
 * desktop OpenGL.
 */
@ExtensionImpl
public class GlslLanguageVersionResolver implements LanguageVersionResolver {
    @RequiredReadAction
    @Override
    public LanguageVersion getLanguageVersion(Language language, @Nullable PsiElement element) {
        if (element != null) {
            PsiFile file = element.getContainingFile();
            VirtualFile virtualFile = file == null ? null : file.getOriginalFile().getVirtualFile();
            if (virtualFile != null) {
                return getLanguageVersion(language, element.getProject(), virtualFile);
            }
        }
        for (LanguageVersion version : language.getVersions()) {
            if (version instanceof LanguageVersionWithDefinition definition && definition.isMyElement(element)) {
                return version;
            }
        }
        return defaultVersion();
    }

    @RequiredReadAction
    @Override
    public LanguageVersion getLanguageVersion(Language language, @Nullable Project project, @Nullable VirtualFile virtualFile) {
        for (LanguageVersion version : language.getVersions()) {
            if (version instanceof LanguageVersionWithDefinition definition && definition.isMyFile(project, virtualFile)) {
                return version;
            }
        }
        return defaultVersion();
    }

    @Override
    public Language getLanguage() {
        return GlslLanguage.INSTANCE;
    }

    public static LanguageVersion defaultVersion() {
        return Objects.requireNonNull(GlslLanguage.INSTANCE.findVersionByClass(GlslOpenGlLanguageVersion.class));
    }
}

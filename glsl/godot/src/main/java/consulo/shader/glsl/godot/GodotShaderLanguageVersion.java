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
package consulo.shader.glsl.godot;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.version.LanguageVersionWithDefinition;
import consulo.project.Project;
import consulo.shader.glsl.GlslDialect;
import consulo.shader.glsl.GlslLanguageVersion;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

/**
 * The Godot shading language, the dialect of {@code .gdshader} and {@code .gdshaderinc} files.
 */
@ExtensionImpl
public class GodotShaderLanguageVersion extends GlslLanguageVersion implements LanguageVersionWithDefinition {
    public GodotShaderLanguageVersion() {
        super("GLSL_GODOT", "Godot", GlslDialect.GODOT);
    }

    @RequiredReadAction
    @Override
    public boolean isMyElement(@Nullable PsiElement element) {
        if (element == null) {
            return false;
        }
        PsiFile file = element.getContainingFile();
        return file != null && isMyFile(element.getProject(), file.getOriginalFile().getVirtualFile());
    }

    @Override
    public boolean isMyFile(@Nullable Project project, @Nullable VirtualFile virtualFile) {
        if (virtualFile == null) {
            return false;
        }
        String extension = virtualFile.getExtension();
        return GodotShaderFileType.INSTANCE.getDefaultExtension().equals(extension)
            || GodotShaderIncludeFileType.INSTANCE.getDefaultExtension().equals(extension);
    }
}

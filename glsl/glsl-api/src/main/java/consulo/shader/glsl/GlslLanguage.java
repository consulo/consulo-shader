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
package consulo.shader.glsl;

import consulo.language.Language;
import consulo.language.file.LanguageFileType;
import consulo.localize.LocalizeValue;
import consulo.shader.glsl.localize.GlslLocalize;

/**
 * The OpenGL Shading Language. Target environments (desktop OpenGL, OpenGL ES / WebGL, Vulkan) are language versions.
 */
public final class GlslLanguage extends Language {
    public static final GlslLanguage INSTANCE = new GlslLanguage();

    private GlslLanguage() {
        super("GLSL");
    }

    @Override
    public LocalizeValue getDisplayName() {
        return GlslLocalize.glslLanguageName();
    }

    @Override
    public LanguageFileType getAssociatedFileType() {
        return GlslFileType.INSTANCE;
    }
}

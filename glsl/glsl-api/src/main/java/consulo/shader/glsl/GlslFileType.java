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

import consulo.language.file.LanguageFileType;
import consulo.localize.LocalizeValue;
import consulo.shader.glsl.icon.GlslIconGroup;
import consulo.shader.glsl.localize.GlslLocalize;
import consulo.ui.image.Image;

public final class GlslFileType extends LanguageFileType {
    public static final GlslFileType INSTANCE = new GlslFileType();

    private GlslFileType() {
        super(GlslLanguage.INSTANCE);
    }

    @Override
    public String getId() {
        return "GLSL";
    }

    @Override
    public LocalizeValue getDescription() {
        return GlslLocalize.glslFileTypeDescription();
    }

    @Override
    public String getDefaultExtension() {
        return "glsl";
    }

    @Override
    public Image getIcon() {
        return GlslIconGroup.glsl();
    }
}

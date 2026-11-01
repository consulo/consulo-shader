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

import consulo.language.file.LanguageFileType;
import consulo.localize.LocalizeValue;
import consulo.shader.glsl.GlslLanguage;
import consulo.shader.glsl.godot.icon.GodotShaderIconGroup;
import consulo.shader.glsl.godot.localize.GodotShaderLocalize;
import consulo.ui.image.Image;

/**
 * A Godot shader include ({@code .gdshaderinc}), included with {@code #include "res://..."}.
 */
public final class GodotShaderIncludeFileType extends LanguageFileType {
    public static final GodotShaderIncludeFileType INSTANCE = new GodotShaderIncludeFileType();

    private GodotShaderIncludeFileType() {
        super(GlslLanguage.INSTANCE);
    }

    @Override
    public String getId() {
        return "GODOT_SHADER_INCLUDE";
    }

    @Override
    public LocalizeValue getDescription() {
        return GodotShaderLocalize.godotShaderIncludeFileTypeDescription();
    }

    @Override
    public String getDefaultExtension() {
        return "gdshaderinc";
    }

    @Override
    public Image getIcon() {
        return GodotShaderIconGroup.gdshaderinc();
    }
}

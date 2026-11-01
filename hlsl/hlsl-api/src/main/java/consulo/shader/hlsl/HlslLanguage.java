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
package consulo.shader.hlsl;

import consulo.language.Language;
import consulo.language.file.LanguageFileType;
import consulo.localize.LocalizeValue;
import consulo.shader.hlsl.localize.HlslLocalize;

/**
 * The High-Level Shading Language of Direct3D, with its relatives (Cg, Unreal shaders) as dialects.
 */
public final class HlslLanguage extends Language {
    public static final HlslLanguage INSTANCE = new HlslLanguage();

    private HlslLanguage() {
        super("HLSL");
    }

    @Override
    public LocalizeValue getDisplayName() {
        return HlslLocalize.hlslLanguageName();
    }

    @Override
    public LanguageFileType getAssociatedFileType() {
        return HlslFileType.INSTANCE;
    }
}

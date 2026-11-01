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
package consulo.shader.glsl.impl.refactoring;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.shader.glsl.GlslLanguage;
import consulo.shader.glsl.impl.lexer.GlslKeywords;
import consulo.shader.glsl.impl.psi.GlslTypes;
import consulo.shader.impl.refactoring.ShaderNamesValidator;

/**
 * A new name must be an identifier in plain GLSL and in Godot.
 */
@ExtensionImpl
public class GlslNamesValidator extends ShaderNamesValidator {
    @Override
    protected boolean isPlainIdentifier(String name) {
        return GlslKeywords.tokenOf(name, false) == GlslTypes.IDENTIFIER && GlslKeywords.tokenOf(name, true) == GlslTypes.IDENTIFIER;
    }

    @Override
    public Language getLanguage() {
        return GlslLanguage.INSTANCE;
    }
}

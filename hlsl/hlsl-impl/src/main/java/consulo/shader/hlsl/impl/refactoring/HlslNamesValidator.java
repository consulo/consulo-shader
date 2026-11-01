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
package consulo.shader.hlsl.impl.refactoring;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.shader.hlsl.HlslLanguage;
import consulo.shader.hlsl.impl.lexer.HlslKeywords;
import consulo.shader.hlsl.impl.psi.HlslTypes;
import consulo.shader.impl.refactoring.ShaderNamesValidator;

/**
 * A new name must be an identifier, not a keyword or built-in type.
 */
@ExtensionImpl
public class HlslNamesValidator extends ShaderNamesValidator {
    @Override
    protected boolean isPlainIdentifier(String name) {
        return HlslKeywords.tokenOf(name) == HlslTypes.IDENTIFIER;
    }

    @Override
    public Language getLanguage() {
        return HlslLanguage.INSTANCE;
    }
}

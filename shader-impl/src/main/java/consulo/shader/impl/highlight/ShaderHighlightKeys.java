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

import consulo.codeEditor.DefaultLanguageHighlighterColors;
import consulo.colorScheme.TextAttributesKey;

/**
 * Colors of names by what they declare or refer to, shared by every shader language.
 */
public interface ShaderHighlightKeys {
    TextAttributesKey PARAMETER = TextAttributesKey.of("SHADER_PARAMETER", DefaultLanguageHighlighterColors.PARAMETER);
    TextAttributesKey LOCAL_VARIABLE = TextAttributesKey.of("SHADER_LOCAL_VARIABLE", DefaultLanguageHighlighterColors.LOCAL_VARIABLE);
    TextAttributesKey GLOBAL_VARIABLE = TextAttributesKey.of("SHADER_GLOBAL_VARIABLE", DefaultLanguageHighlighterColors.GLOBAL_VARIABLE);
    TextAttributesKey FIELD = TextAttributesKey.of("SHADER_FIELD", DefaultLanguageHighlighterColors.INSTANCE_FIELD);
    TextAttributesKey FUNCTION_DECLARATION =
        TextAttributesKey.of("SHADER_FUNCTION_DECLARATION", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
    TextAttributesKey FUNCTION_CALL = TextAttributesKey.of("SHADER_FUNCTION_CALL", DefaultLanguageHighlighterColors.FUNCTION_CALL);
    TextAttributesKey TYPE = TextAttributesKey.of("SHADER_TYPE", DefaultLanguageHighlighterColors.CLASS_NAME);
    TextAttributesKey BUILTIN = TextAttributesKey.of("SHADER_BUILTIN", DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL);
}

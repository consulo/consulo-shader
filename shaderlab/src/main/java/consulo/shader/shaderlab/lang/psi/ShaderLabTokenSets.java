/*
 * Copyright 2013-2016 consulo.io
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

package consulo.shader.shaderlab.lang.psi;

import consulo.language.ast.TokenSet;

/**
 * @author VISTALL
 * @since 08.05.2015
 */
public interface ShaderLabTokenSets
{
	TokenSet COMMENTS = TokenSet.create(ShaderLabTokens.LINE_COMMENT, ShaderLabTokens.BLOCK_COMMENT, ShaderLabTokens.PREPROCESSOR_LINE);

	TokenSet STRINGS = TokenSet.create(ShaderLabTokens.STRING_LITERAL, ShaderLabTokens.UNQUOTED_STRING_LITERAL);

	TokenSet WHITESPACES = TokenSet.create(ShaderLabTokens.WHITE_SPACE);

	TokenSet KEYWORDS = TokenSet.create(ShaderLabTokens.CGPROGRAM_KEYWORD, ShaderLabTokens.ENDCG_KEYWORD, ShaderLabTokens.CGINCLUDE_KEYWORD,
			ShaderLabTokens.HLSLPROGRAM_KEYWORD, ShaderLabTokens.ENDHLSL_KEYWORD, ShaderLabTokens.HLSLINCLUDE_KEYWORD,
			ShaderLabTokens.GLSLPROGRAM_KEYWORD, ShaderLabTokens.ENDGLSL_KEYWORD, ShaderLabTokens.GLSLINCLUDE_KEYWORD);

	TokenSet GLSL_PROGRAM_START = TokenSet.create(ShaderLabTokens.GLSLPROGRAM_KEYWORD, ShaderLabTokens.GLSLINCLUDE_KEYWORD);
}

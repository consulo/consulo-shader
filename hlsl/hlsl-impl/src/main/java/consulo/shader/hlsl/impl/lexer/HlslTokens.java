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
package consulo.shader.hlsl.impl.lexer;

import consulo.language.ast.ElementTypeAsPsiFactory;
import consulo.language.ast.IElementType;
import consulo.language.ast.StandardTokenTypes;
import consulo.language.ast.TokenSet;
import consulo.shader.hlsl.HlslLanguage;
import consulo.shader.hlsl.impl.psi.HlslPreprocessorLineElementType;
import consulo.shader.impl.psi.ShaderIncludeDirectiveElement;
import consulo.shader.impl.psi.ShaderIncludePathElement;

/**
 * Tokens the grammar never sees: comments and preprocessor lines, and what a preprocessor line is made of. The grammar
 * tokens are generated into {@link consulo.shader.hlsl.impl.psi.HlslTypes}.
 */
public interface HlslTokens extends StandardTokenTypes {
    IElementType LINE_COMMENT = new IElementType("HLSL_LINE_COMMENT", HlslLanguage.INSTANCE);
    IElementType BLOCK_COMMENT = new IElementType("HLSL_BLOCK_COMMENT", HlslLanguage.INSTANCE);

    /**
     * A whole preprocessor line, parsed lazily into {@link #PREPROCESSOR_DIRECTIVE}, {@link #INCLUDE_DIRECTIVE} and
     * {@link #PREPROCESSOR_TEXT}.
     */
    IElementType PREPROCESSOR_LINE = new HlslPreprocessorLineElementType();

    IElementType PREPROCESSOR_DIRECTIVE = new IElementType("HLSL_PREPROCESSOR_DIRECTIVE", HlslLanguage.INSTANCE);
    IElementType PREPROCESSOR_TEXT = new IElementType("HLSL_PREPROCESSOR_TEXT", HlslLanguage.INSTANCE);
    /**
     * The path of an {@code #include}, with its quotes or angle brackets.
     */
    IElementType INCLUDE_PATH = new IElementType("HLSL_INCLUDE_PATH", HlslLanguage.INSTANCE);

    IElementType INCLUDE_DIRECTIVE =
        new ElementTypeAsPsiFactory("HLSL_INCLUDE_DIRECTIVE", HlslLanguage.INSTANCE, ShaderIncludeDirectiveElement::new);
    IElementType INCLUDE_PATH_ELEMENT =
        new ElementTypeAsPsiFactory("HLSL_INCLUDE_PATH_ELEMENT", HlslLanguage.INSTANCE, ShaderIncludePathElement::new);

    TokenSet COMMENTS = TokenSet.create(LINE_COMMENT, BLOCK_COMMENT, PREPROCESSOR_LINE);
}

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
package consulo.shader.glsl.impl.lexer;

import consulo.language.ast.ElementTypeAsPsiFactory;
import consulo.language.ast.IElementType;
import consulo.language.ast.StandardTokenTypes;
import consulo.language.ast.TokenSet;
import consulo.shader.glsl.GlslLanguage;
import consulo.shader.glsl.impl.psi.GlslPreprocessorLineElementType;
import consulo.shader.impl.psi.ShaderIncludeDirectiveElement;
import consulo.shader.impl.psi.ShaderIncludePathElement;

/**
 * Tokens the grammar never sees: comments and preprocessor lines, and what a preprocessor line is made of. The grammar
 * tokens are generated into {@link consulo.shader.glsl.impl.psi.GlslTypes}.
 */
public interface GlslTokens extends StandardTokenTypes {
    IElementType LINE_COMMENT = new IElementType("GLSL_LINE_COMMENT", GlslLanguage.INSTANCE);
    IElementType BLOCK_COMMENT = new IElementType("GLSL_BLOCK_COMMENT", GlslLanguage.INSTANCE);

    /**
     * A whole preprocessor line, parsed lazily into {@link #PREPROCESSOR_DIRECTIVE}, {@link #INCLUDE_DIRECTIVE} and
     * {@link #PREPROCESSOR_TEXT}.
     */
    IElementType PREPROCESSOR_LINE = new GlslPreprocessorLineElementType();

    IElementType PREPROCESSOR_DIRECTIVE = new IElementType("GLSL_PREPROCESSOR_DIRECTIVE", GlslLanguage.INSTANCE);
    IElementType PREPROCESSOR_TEXT = new IElementType("GLSL_PREPROCESSOR_TEXT", GlslLanguage.INSTANCE);
    /**
     * The path of an {@code #include}, with its quotes or angle brackets.
     */
    IElementType INCLUDE_PATH = new IElementType("GLSL_INCLUDE_PATH", GlslLanguage.INSTANCE);

    IElementType INCLUDE_DIRECTIVE =
        new ElementTypeAsPsiFactory("GLSL_INCLUDE_DIRECTIVE", GlslLanguage.INSTANCE, ShaderIncludeDirectiveElement::new);
    IElementType INCLUDE_PATH_ELEMENT =
        new ElementTypeAsPsiFactory("GLSL_INCLUDE_PATH_ELEMENT", GlslLanguage.INSTANCE, ShaderIncludePathElement::new);

    TokenSet COMMENTS = TokenSet.create(LINE_COMMENT, BLOCK_COMMENT, PREPROCESSOR_LINE);
}

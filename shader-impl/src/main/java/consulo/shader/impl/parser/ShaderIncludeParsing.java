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
package consulo.shader.impl.parser;

import consulo.language.ast.IElementType;
import consulo.language.parser.PsiBuilder;

/**
 * Parses {@code #include} directives for languages whose lexer emits the directive keyword and the delimited path as
 * two tokens.
 */
public final class ShaderIncludeParsing {
    private ShaderIncludeParsing() {
    }

    /**
     * When the builder is at {@code directiveToken} followed by {@code pathToken}, wraps the path into
     * {@code pathElement} and both into {@code directiveElement}.
     *
     * @return whether an include directive was parsed
     */
    public static boolean parseInclude(PsiBuilder builder,
                                       IElementType directiveToken,
                                       IElementType pathToken,
                                       IElementType directiveElement,
                                       IElementType pathElement) {
        if (builder.getTokenType() != directiveToken || builder.lookAhead(1) != pathToken) {
            return false;
        }
        PsiBuilder.Marker directive = builder.mark();
        builder.advanceLexer();
        PsiBuilder.Marker path = builder.mark();
        builder.advanceLexer();
        path.done(pathElement);
        directive.done(directiveElement);
        return true;
    }
}

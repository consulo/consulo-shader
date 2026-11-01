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
package consulo.shader.glsl.impl;

import consulo.codeEditor.DefaultLanguageHighlighterColors;
import consulo.colorScheme.TextAttributesKey;
import consulo.language.ast.IElementType;
import consulo.language.editor.highlight.SyntaxHighlighterBase;
import consulo.language.lexer.LayeredLexer;
import consulo.language.lexer.Lexer;
import consulo.shader.glsl.impl.lexer.GlslTokens;
import consulo.shader.glsl.impl.lexer._GlslDirectiveLexer;
import consulo.shader.glsl.impl.lexer._GlslLexer;
import consulo.shader.glsl.impl.psi.GlslTypes;

import java.util.HashMap;
import java.util.Map;

public class GlslSyntaxHighlighter extends SyntaxHighlighterBase {
    private static final Map<IElementType, TextAttributesKey> ATTRIBUTES = new HashMap<>();

    static {
        ATTRIBUTES.put(GlslTokens.LINE_COMMENT, DefaultLanguageHighlighterColors.LINE_COMMENT);
        ATTRIBUTES.put(GlslTokens.BLOCK_COMMENT, DefaultLanguageHighlighterColors.BLOCK_COMMENT);
        ATTRIBUTES.put(GlslTokens.PREPROCESSOR_DIRECTIVE, DefaultLanguageHighlighterColors.MACRO_KEYWORD);
        ATTRIBUTES.put(GlslTokens.INCLUDE_PATH, DefaultLanguageHighlighterColors.STRING);
        ATTRIBUTES.put(GlslTypes.STRING_LITERAL, DefaultLanguageHighlighterColors.STRING);
        ATTRIBUTES.put(GlslTypes.INTEGER_LITERAL, DefaultLanguageHighlighterColors.NUMBER);
        ATTRIBUTES.put(GlslTypes.FLOAT_LITERAL, DefaultLanguageHighlighterColors.NUMBER);
        ATTRIBUTES.put(GlslTypes.LEFT_PAREN, DefaultLanguageHighlighterColors.PARENTHESES);
        ATTRIBUTES.put(GlslTypes.RIGHT_PAREN, DefaultLanguageHighlighterColors.PARENTHESES);
        ATTRIBUTES.put(GlslTypes.LEFT_BRACE, DefaultLanguageHighlighterColors.BRACES);
        ATTRIBUTES.put(GlslTypes.RIGHT_BRACE, DefaultLanguageHighlighterColors.BRACES);
        ATTRIBUTES.put(GlslTypes.LEFT_BRACKET, DefaultLanguageHighlighterColors.BRACKETS);
        ATTRIBUTES.put(GlslTypes.RIGHT_BRACKET, DefaultLanguageHighlighterColors.BRACKETS);
        ATTRIBUTES.put(GlslTypes.SEMICOLON, DefaultLanguageHighlighterColors.SEMICOLON);
        ATTRIBUTES.put(GlslTypes.COMMA, DefaultLanguageHighlighterColors.COMMA);
        ATTRIBUTES.put(GlslTypes.DOT, DefaultLanguageHighlighterColors.DOT);
        for (IElementType keyword : new IElementType[]{
            GlslTypes.TYPE, GlslTypes.BOOL_LITERAL, GlslTypes.STRUCT, GlslTypes.LAYOUT, GlslTypes.PRECISION,
            GlslTypes.PRECISION_QUALIFIER, GlslTypes.SUBROUTINE, GlslTypes.QUALIFIER, GlslTypes.IF, GlslTypes.ELSE,
            GlslTypes.SWITCH, GlslTypes.CASE, GlslTypes.DEFAULT, GlslTypes.WHILE, GlslTypes.DO, GlslTypes.FOR, GlslTypes.BREAK,
            GlslTypes.CONTINUE, GlslTypes.RETURN, GlslTypes.DISCARD, GlslTypes.EXTENSION_JUMP, GlslTypes.SHADER_TYPE,
            GlslTypes.RENDER_MODE, GlslTypes.STENCIL_MODE, GlslTypes.GROUP_UNIFORMS, GlslTypes.GLOBAL, GlslTypes.INSTANCE,
            GlslTypes.HINT}) {
            ATTRIBUTES.put(keyword, DefaultLanguageHighlighterColors.KEYWORD);
        }
        for (IElementType operator : new IElementType[]{
            GlslTypes.EQUAL, GlslTypes.LEFT_ASSIGN, GlslTypes.RIGHT_ASSIGN, GlslTypes.ADD_ASSIGN, GlslTypes.SUB_ASSIGN,
            GlslTypes.MUL_ASSIGN, GlslTypes.DIV_ASSIGN, GlslTypes.MOD_ASSIGN, GlslTypes.AND_ASSIGN, GlslTypes.XOR_ASSIGN,
            GlslTypes.OR_ASSIGN, GlslTypes.INC_OP, GlslTypes.DEC_OP, GlslTypes.AND_OP, GlslTypes.OR_OP, GlslTypes.XOR_OP,
            GlslTypes.LEFT_OP, GlslTypes.RIGHT_OP, GlslTypes.LE_OP, GlslTypes.GE_OP, GlslTypes.EQ_OP, GlslTypes.NE_OP,
            GlslTypes.BANG, GlslTypes.DASH, GlslTypes.TILDE, GlslTypes.PLUS, GlslTypes.STAR, GlslTypes.SLASH, GlslTypes.PERCENT,
            GlslTypes.LEFT_ANGLE, GlslTypes.RIGHT_ANGLE, GlslTypes.VERTICAL_BAR, GlslTypes.CARET, GlslTypes.AMPERSAND,
            GlslTypes.QUESTION, GlslTypes.COLON}) {
            ATTRIBUTES.put(operator, DefaultLanguageHighlighterColors.OPERATION_SIGN);
        }
    }

    private final boolean myGodot;

    public GlslSyntaxHighlighter(boolean godot) {
        myGodot = godot;
    }

    @Override
    public Lexer getHighlightingLexer() {
        LayeredLexer lexer = new LayeredLexer(new _GlslLexer(myGodot));
        lexer.registerLayer(new _GlslDirectiveLexer(), GlslTokens.PREPROCESSOR_LINE);
        return lexer;
    }

    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        return pack(ATTRIBUTES.get(tokenType));
    }
}

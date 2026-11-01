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
package consulo.shader.hlsl.impl;

import consulo.codeEditor.DefaultLanguageHighlighterColors;
import consulo.colorScheme.TextAttributesKey;
import consulo.language.ast.IElementType;
import consulo.language.editor.highlight.SyntaxHighlighterBase;
import consulo.language.lexer.LayeredLexer;
import consulo.language.lexer.Lexer;
import consulo.shader.hlsl.impl.lexer.HlslTokens;
import consulo.shader.hlsl.impl.lexer._HlslDirectiveLexer;
import consulo.shader.hlsl.impl.lexer._HlslLexer;
import consulo.shader.hlsl.impl.psi.HlslTypes;

import java.util.HashMap;
import java.util.Map;

public class HlslSyntaxHighlighter extends SyntaxHighlighterBase {
    private static final Map<IElementType, TextAttributesKey> ATTRIBUTES = new HashMap<>();

    static {
        ATTRIBUTES.put(HlslTokens.LINE_COMMENT, DefaultLanguageHighlighterColors.LINE_COMMENT);
        ATTRIBUTES.put(HlslTokens.BLOCK_COMMENT, DefaultLanguageHighlighterColors.BLOCK_COMMENT);
        ATTRIBUTES.put(HlslTokens.PREPROCESSOR_DIRECTIVE, DefaultLanguageHighlighterColors.MACRO_KEYWORD);
        ATTRIBUTES.put(HlslTokens.INCLUDE_PATH, DefaultLanguageHighlighterColors.STRING);
        ATTRIBUTES.put(HlslTypes.STRING_LITERAL, DefaultLanguageHighlighterColors.STRING);
        ATTRIBUTES.put(HlslTypes.INTEGER_LITERAL, DefaultLanguageHighlighterColors.NUMBER);
        ATTRIBUTES.put(HlslTypes.FLOAT_LITERAL, DefaultLanguageHighlighterColors.NUMBER);
        ATTRIBUTES.put(HlslTypes.LEFT_PAREN, DefaultLanguageHighlighterColors.PARENTHESES);
        ATTRIBUTES.put(HlslTypes.RIGHT_PAREN, DefaultLanguageHighlighterColors.PARENTHESES);
        ATTRIBUTES.put(HlslTypes.LEFT_BRACE, DefaultLanguageHighlighterColors.BRACES);
        ATTRIBUTES.put(HlslTypes.RIGHT_BRACE, DefaultLanguageHighlighterColors.BRACES);
        ATTRIBUTES.put(HlslTypes.LEFT_BRACKET, DefaultLanguageHighlighterColors.BRACKETS);
        ATTRIBUTES.put(HlslTypes.RIGHT_BRACKET, DefaultLanguageHighlighterColors.BRACKETS);
        ATTRIBUTES.put(HlslTypes.SEMICOLON, DefaultLanguageHighlighterColors.SEMICOLON);
        ATTRIBUTES.put(HlslTypes.COMMA, DefaultLanguageHighlighterColors.COMMA);
        ATTRIBUTES.put(HlslTypes.DOT, DefaultLanguageHighlighterColors.DOT);
        for (IElementType keyword : new IElementType[]{
            HlslTypes.TYPE, HlslTypes.BOOL_LITERAL, HlslTypes.QUALIFIER, HlslTypes.SOFT_QUALIFIER, HlslTypes.STRUCT,
            HlslTypes.CBUFFER, HlslTypes.TYPEDEF, HlslTypes.NAMESPACE, HlslTypes.ENUM, HlslTypes.TEMPLATE, HlslTypes.TYPENAME,
            HlslTypes.REGISTER, HlslTypes.PACKOFFSET, HlslTypes.THIS, HlslTypes.SIZEOF, HlslTypes.TECHNIQUE, HlslTypes.PASS,
            HlslTypes.COMPILE, HlslTypes.SAMPLER_STATE, HlslTypes.OPERATOR, HlslTypes.IF, HlslTypes.ELSE, HlslTypes.SWITCH, HlslTypes.CASE,
            HlslTypes.DEFAULT, HlslTypes.WHILE, HlslTypes.DO, HlslTypes.FOR, HlslTypes.BREAK, HlslTypes.CONTINUE,
            HlslTypes.RETURN, HlslTypes.DISCARD}) {
            ATTRIBUTES.put(keyword, DefaultLanguageHighlighterColors.KEYWORD);
        }
        for (IElementType operator : new IElementType[]{
            HlslTypes.EQUAL, HlslTypes.LEFT_ASSIGN, HlslTypes.RIGHT_ASSIGN, HlslTypes.ADD_ASSIGN, HlslTypes.SUB_ASSIGN,
            HlslTypes.MUL_ASSIGN, HlslTypes.DIV_ASSIGN, HlslTypes.MOD_ASSIGN, HlslTypes.AND_ASSIGN, HlslTypes.XOR_ASSIGN,
            HlslTypes.OR_ASSIGN, HlslTypes.INC_OP, HlslTypes.DEC_OP, HlslTypes.AND_OP, HlslTypes.OR_OP, HlslTypes.LEFT_OP,
            HlslTypes.RIGHT_OP, HlslTypes.LE_OP, HlslTypes.GE_OP, HlslTypes.EQ_OP, HlslTypes.NE_OP, HlslTypes.BANG,
            HlslTypes.DASH, HlslTypes.TILDE, HlslTypes.PLUS, HlslTypes.STAR, HlslTypes.SLASH, HlslTypes.PERCENT,
            HlslTypes.LEFT_ANGLE, HlslTypes.RIGHT_ANGLE, HlslTypes.VERTICAL_BAR, HlslTypes.CARET, HlslTypes.AMPERSAND,
            HlslTypes.QUESTION, HlslTypes.COLON, HlslTypes.COLON_COLON}) {
            ATTRIBUTES.put(operator, DefaultLanguageHighlighterColors.OPERATION_SIGN);
        }
    }

    @Override
    public Lexer getHighlightingLexer() {
        LayeredLexer lexer = new LayeredLexer(new _HlslLexer());
        lexer.registerLayer(new _HlslDirectiveLexer(), HlslTokens.PREPROCESSOR_LINE);
        return lexer;
    }

    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        return pack(ATTRIBUTES.get(tokenType));
    }
}

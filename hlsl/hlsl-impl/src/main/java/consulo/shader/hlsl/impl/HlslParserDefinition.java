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

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IFileElementType;
import consulo.language.ast.TokenSet;
import consulo.language.file.FileViewProvider;
import consulo.language.lexer.Lexer;
import consulo.language.parser.ParserDefinition;
import consulo.language.parser.PsiParser;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.version.LanguageVersion;
import consulo.shader.hlsl.HlslLanguage;
import consulo.shader.hlsl.impl.lexer.HlslTokens;
import consulo.shader.hlsl.impl.lexer._HlslLexer;
import consulo.shader.hlsl.impl.parser.HlslParser;
import consulo.shader.hlsl.impl.psi.HlslFile;
import consulo.shader.hlsl.impl.psi.HlslTypes;
import consulo.shader.hlsl.impl.psi.impl.HlslTypesFactory;

@ExtensionImpl
public class HlslParserDefinition implements ParserDefinition {
    private static final IFileElementType FILE_ELEMENT_TYPE = new IFileElementType(HlslLanguage.INSTANCE);

    @Override
    public Language getLanguage() {
        return HlslLanguage.INSTANCE;
    }

    @Override
    public Lexer createLexer(LanguageVersion languageVersion) {
        return new _HlslLexer();
    }

    @Override
    public PsiParser createParser(LanguageVersion languageVersion) {
        return new HlslParser();
    }

    @Override
    public IFileElementType getFileNodeType() {
        return FILE_ELEMENT_TYPE;
    }

    @Override
    public TokenSet getWhitespaceTokens(LanguageVersion languageVersion) {
        return TokenSet.create(HlslTokens.WHITE_SPACE);
    }

    @Override
    public TokenSet getCommentTokens(LanguageVersion languageVersion) {
        return HlslTokens.COMMENTS;
    }

    @Override
    public TokenSet getStringLiteralElements(LanguageVersion languageVersion) {
        return TokenSet.create(HlslTypes.STRING_LITERAL);
    }

    @Override
    public PsiElement createElement(ASTNode node) {
        return HlslTypesFactory.createElement(node);
    }

    @Override
    public PsiFile createFile(FileViewProvider viewProvider) {
        return new HlslFile(viewProvider);
    }
}

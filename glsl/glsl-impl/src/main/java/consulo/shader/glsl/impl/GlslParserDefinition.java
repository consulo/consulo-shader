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
import consulo.shader.glsl.GlslDialect;
import consulo.shader.glsl.GlslLanguage;
import consulo.shader.glsl.GlslLanguageVersion;
import consulo.shader.glsl.impl.lexer.GlslTokens;
import consulo.shader.glsl.impl.lexer._GlslLexer;
import consulo.shader.glsl.impl.parser.GlslParser;
import consulo.shader.glsl.impl.psi.GlslFile;
import consulo.shader.glsl.impl.psi.GlslTypes;
import consulo.shader.glsl.impl.psi.impl.GlslTypesFactory;

@ExtensionImpl
public class GlslParserDefinition implements ParserDefinition {
    private static final IFileElementType FILE_ELEMENT_TYPE = new IFileElementType(GlslLanguage.INSTANCE);

    public static boolean isGodot(LanguageVersion languageVersion) {
        return languageVersion instanceof GlslLanguageVersion version && version.getDialect() == GlslDialect.GODOT;
    }

    @Override
    public Language getLanguage() {
        return GlslLanguage.INSTANCE;
    }

    @Override
    public Lexer createLexer(LanguageVersion languageVersion) {
        return new _GlslLexer(isGodot(languageVersion));
    }

    @Override
    public PsiParser createParser(LanguageVersion languageVersion) {
        return new GlslParser();
    }

    @Override
    public IFileElementType getFileNodeType() {
        return FILE_ELEMENT_TYPE;
    }

    @Override
    public TokenSet getWhitespaceTokens(LanguageVersion languageVersion) {
        return TokenSet.create(GlslTokens.WHITE_SPACE);
    }

    @Override
    public TokenSet getCommentTokens(LanguageVersion languageVersion) {
        return GlslTokens.COMMENTS;
    }

    @Override
    public TokenSet getStringLiteralElements(LanguageVersion languageVersion) {
        return TokenSet.create(GlslTypes.STRING_LITERAL);
    }

    @Override
    public PsiElement createElement(ASTNode node) {
        return GlslTypesFactory.createElement(node);
    }

    @Override
    public PsiFile createFile(FileViewProvider viewProvider) {
        return new GlslFile(viewProvider);
    }
}

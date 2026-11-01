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

package consulo.shader.shaderlab.lang;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IFileElementType;
import consulo.language.ast.TokenSet;
import consulo.language.file.FileViewProvider;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import consulo.language.lexer.Lexer;
import consulo.language.parser.ParserDefinition;
import consulo.language.parser.PsiParser;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.version.LanguageVersion;
import consulo.shader.shaderlab.lang.lexer.ShaderLabLexer;
import consulo.shader.shaderlab.lang.parser.ShaderLabParser;
import consulo.shader.shaderlab.lang.psi.ShaderLabFile;
import consulo.shader.shaderlab.lang.psi.ShaderLabStubElements;
import consulo.shader.shaderlab.lang.psi.ShaderLabTokenSets;


/**
 * @author VISTALL
 * @since 08.05.2015
 */
@ExtensionImpl
public class ShaderLabParserDefinition implements ParserDefinition
{
	@Override
	public Language getLanguage()
	{
		return ShaderLabLanguage.INSTANCE;
	}

	@Override
	public Lexer createLexer(LanguageVersion languageVersion)
	{
		return new ShaderLabLexer();
	}

	@Override
	public PsiParser createParser(LanguageVersion languageVersion)
	{
		return new ShaderLabParser();
	}

	@Override
	public IFileElementType getFileNodeType()
	{
		return ShaderLabStubElements.FILE;
	}

	@Override
	public TokenSet getWhitespaceTokens(LanguageVersion languageVersion)
	{
		return ShaderLabTokenSets.WHITESPACES;
	}

	@Override
	public TokenSet getCommentTokens(LanguageVersion languageVersion)
	{
		return ShaderLabTokenSets.COMMENTS;
	}

	@Override
	public TokenSet getStringLiteralElements(LanguageVersion languageVersion)
	{
		return TokenSet.EMPTY;
	}

	@Override
	public PsiElement createElement(ASTNode node)
	{
		System.out.println(node.getElementType());
		return new ASTWrapperPsiElement(node);
	}

	@Override
	public PsiFile createFile(FileViewProvider viewProvider)
	{
		return new ShaderLabFile(viewProvider);
	}

	@Override
	public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right)
	{
		return SpaceRequirements.MAY;
	}
}

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

import consulo.annotation.access.RequiredReadAction;
import consulo.language.ast.ASTNode;
import consulo.language.psi.LiteralTextEscaper;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiLanguageInjectionHost;

import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 11.10.2015
 */
public class ShaderCGScript extends ShaderLabElement implements PsiLanguageInjectionHost
{
	public ShaderCGScript(ASTNode node)
	{
		super(node);
	}

	@Nullable
	@RequiredReadAction
	public PsiElement getScriptBlock()
	{
		return findChildByType(ShaderLabTokens.SHADERSCRIPT);
	}

	/**
	 * Whether this is a {@code GLSLPROGRAM} or {@code GLSLINCLUDE} block; the other program blocks are HLSL.
	 */
	@RequiredReadAction
	public boolean isGlsl()
	{
		PsiElement first = getFirstChild();
		return first != null && ShaderLabTokenSets.GLSL_PROGRAM_START.contains(first.getNode().getElementType());
	}

	@Override
	public void accept(SharpLabElementVisitor visitor)
	{
		visitor.visitCGScript(this);
	}

	@Override
	@RequiredReadAction
	public boolean isValidHost()
	{
		return getScriptBlock() != null;
	}

	@Override
	public PsiLanguageInjectionHost updateText(String text)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public LiteralTextEscaper<? extends PsiLanguageInjectionHost> createLiteralTextEscaper()
	{
		return LiteralTextEscaper.createSimple(this);
	}
}

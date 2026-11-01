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

package consulo.shader.shaderlab.lang.parser.roles;

import consulo.language.ast.IElementType;
import consulo.language.parser.PsiBuilder;
import consulo.shader.shaderlab.lang.parser.ShaderLabParserBuilder;
import consulo.shader.shaderlab.lang.psi.ShaderLabElements;
import consulo.shader.shaderlab.lang.psi.ShaderLabTokens;
import consulo.util.collection.ArrayUtil;

import org.jspecify.annotations.Nullable;

import static consulo.shader.shaderlab.lang.parser.ShaderLabParser.doneError;

/**
 * @author VISTALL
 * @since 09.05.2015
 */
public abstract class ShaderLabRole
{
	private String myName;

	public ShaderLabRole()
	{
	}

	public abstract PsiBuilder.Marker parseAndDone(ShaderLabParserBuilder builder, PsiBuilder.Marker mark);

	public ShaderLabRole setName(String name)
	{
		if(myName != null)
		{
			return this;
		}
		myName = name;
		return this;
	}

	public String getName()
	{
		return myName;
	}

	public String[] getValues()
	{
		return ArrayUtil.EMPTY_STRING_ARRAY;
	}

	@Nullable
	public String getDefaultInsertValue()
	{
		return null;
	}

	public boolean tryParse(ShaderLabParserBuilder builder)
	{
		if(builder.is(this))
		{
			parseImpl(builder);
			return true;
		}
		return false;
	}

	protected void doneWithErrorSafe(ShaderLabParserBuilder builder, String error)
	{
		IElementType tokenType = builder.getTokenType();
		if(tokenType == ShaderLabTokens.LBRACE || tokenType == ShaderLabTokens.RBRACE)
		{
			builder.error("Expected value");
		}
		else
		{
			doneError(builder, error);
		}
	}

	public PsiBuilder.Marker parseImpl(ShaderLabParserBuilder builder)
	{
		PsiBuilder.Marker mark = builder.mark();

		builder.advanceLexer();

		if(parseAndDone(builder, mark) == null)
		{
			builder.error("Expected value");
			mark.done(ShaderLabElements.SIMPLE_VALUE);
		}

		return mark;
	}
}

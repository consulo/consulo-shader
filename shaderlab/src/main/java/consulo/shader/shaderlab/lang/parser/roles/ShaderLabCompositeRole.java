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
import consulo.language.parser.PsiBuilderUtil;
import consulo.shader.shaderlab.lang.parser.ShaderLabParser;
import consulo.shader.shaderlab.lang.parser.ShaderLabParserBuilder;
import consulo.shader.shaderlab.lang.psi.ShaderLabElements;
import consulo.shader.shaderlab.lang.psi.ShaderLabTokens;
import org.jspecify.annotations.Nullable;


/**
 * @author VISTALL
 * @since 09.05.2015
 */
public class ShaderLabCompositeRole extends ShaderLabRole
{
	private IElementType myDoneElement;
	private ShaderLabRole[] myRoles;

	public ShaderLabCompositeRole(IElementType doneElement, ShaderLabRole... roles)
	{
		myDoneElement = doneElement;
		myRoles = roles;
	}

	public ShaderLabRole[] getRoles()
	{
		return myRoles;
	}

	public void parseBefore(ShaderLabParserBuilder builder)
	{
	}

	@Override
	public PsiBuilder.Marker parseAndDone(ShaderLabParserBuilder builder, PsiBuilder.Marker mark)
	{
		parseBefore(builder);

		if(PsiBuilderUtil.expect(builder, ShaderLabTokens.LBRACE))
		{
			//TODO [VISTALL] hack until full syntax parse
			int count = 0;
			loop:
			while(!builder.eof())
			{
				for(ShaderLabRole role : myRoles)
				{
					if(role.tryParse(builder))
					{
						continue loop;
					}
				}

				IElementType tokenType = builder.getTokenType();
				if(tokenType == ShaderLabTokens.LBRACE)
				{
					count++;
				}

				if(tokenType == ShaderLabTokens.RBRACE)
				{
					if(count == 0)
					{
						break;
					}

					count--;
				}

				if(!tryParseProgramBlock(builder))
				{
					builder.advanceLexer();
				}
			}

			if(!PsiBuilderUtil.expect(builder, ShaderLabTokens.RBRACE))
			{
				builder.error("'}' expected");
			}
		}
		else
		{
			builder.error("'{' expected");
		}
		mark.done(myDoneElement);
		return mark;
	}

	/**
	 * Parses a CGPROGRAM/CGINCLUDE/HLSLPROGRAM/HLSLINCLUDE/GLSLPROGRAM/GLSLINCLUDE block when the builder is at one.
	 * Unity accepts the INCLUDE blocks anywhere in the file, the top level included.
	 *
	 * @return whether a program block was parsed
	 */
	public static boolean tryParseProgramBlock(PsiBuilder builder)
	{
		IElementType programEnd = programEndOf(builder.getTokenType());
		if(programEnd == null)
		{
			return false;
		}
		PsiBuilder.Marker marker = builder.mark();
		builder.advanceLexer();
		if(builder.getTokenType() == ShaderLabTokens.SHADERSCRIPT)
		{
			builder.advanceLexer();
		}
		ShaderLabParser.expectWithError(builder, programEnd, "Expected '" + programEndText(programEnd) + "'");
		marker.done(ShaderLabElements.CG_SHADER);
		return true;
	}

	/**
	 * The keyword that closes a program block opened by {@code tokenType}, or {@code null} when it opens none.
	 */
	@Nullable
	private static IElementType programEndOf(@Nullable IElementType tokenType)
	{
		if(tokenType == ShaderLabTokens.CGINCLUDE_KEYWORD || tokenType == ShaderLabTokens.CGPROGRAM_KEYWORD)
		{
			return ShaderLabTokens.ENDCG_KEYWORD;
		}
		if(tokenType == ShaderLabTokens.HLSLINCLUDE_KEYWORD || tokenType == ShaderLabTokens.HLSLPROGRAM_KEYWORD)
		{
			return ShaderLabTokens.ENDHLSL_KEYWORD;
		}
		if(tokenType == ShaderLabTokens.GLSLINCLUDE_KEYWORD || tokenType == ShaderLabTokens.GLSLPROGRAM_KEYWORD)
		{
			return ShaderLabTokens.ENDGLSL_KEYWORD;
		}
		return null;
	}

	private static String programEndText(IElementType programEnd)
	{
		if(programEnd == ShaderLabTokens.ENDHLSL_KEYWORD)
		{
			return "ENDHLSL";
		}
		if(programEnd == ShaderLabTokens.ENDGLSL_KEYWORD)
		{
			return "ENDGLSL";
		}
		return "ENDCG";
	}
}

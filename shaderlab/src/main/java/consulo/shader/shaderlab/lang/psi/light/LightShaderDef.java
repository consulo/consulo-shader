/*
 * Copyright 2013-2017 consulo.io
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

package consulo.shader.shaderlab.lang.psi.light;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.language.impl.psi.LightElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiManager;
import consulo.language.util.IncorrectOperationException;
import consulo.project.Project;
import consulo.shader.shaderlab.lang.ShaderLabBuiltinShaders;
import consulo.shader.shaderlab.lang.ShaderLabLanguage;
import consulo.shader.shaderlab.lang.parser.roles.ShaderLabRole;
import consulo.shader.shaderlab.lang.parser.roles.ShaderLabRoles;
import consulo.shader.shaderlab.lang.psi.ShaderDef;
import consulo.shader.shaderlab.lang.psi.ShaderProperty;
import consulo.util.dataholder.Key;
import consulo.util.lang.StringUtil;

import org.jspecify.annotations.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author VISTALL
 * @since 21-Oct-17
 */
public class LightShaderDef extends LightElement implements ShaderDef
{
	private static final Key<Map<String, ShaderDef>> ourKey = Key.create("getDefaultShaders");

	public static Map<String, ShaderDef> getDefaultShaders(Project project)
	{
		Map<String, ShaderDef> data = project.getUserData(ourKey);
		if(data != null)
		{
			return data;
		}

		// keys are quoted (as written in Fallback/UsePass string literals); Unity compares shader names case-insensitively
		data = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		for(String key : ShaderLabBuiltinShaders.NAMES)
		{
			String quoted = StringUtil.QUOTER.apply(key);
			data.put(quoted, new LightShaderDef(project, quoted));
		}

		for(String key : ShaderLabBuiltinShaders.LEGACY_ALIASES)
		{
			String quoted = StringUtil.QUOTER.apply(key);
			data.putIfAbsent(quoted, new LightShaderDef(project, quoted));
		}

		data = Collections.unmodifiableMap(data);

		project.putUserData(ourKey, data);

		return data;
	}

	private final String myName;

	public LightShaderDef(Project project, String name)
	{
		super(PsiManager.getInstance(project), ShaderLabLanguage.INSTANCE);
		myName = name;
	}

	@Override
	public String toString()
	{
		return null;
	}

	@RequiredReadAction
	@Override
	public String getName()
	{
		return myName;
	}

	@Override
	public List<ShaderProperty> getProperties()
	{
		return Collections.emptyList();
	}

	@RequiredReadAction
	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return null;
	}

	@RequiredWriteAction
	@Override
	public PsiElement setName(String s) throws IncorrectOperationException
	{
		return null;
	}

	@Nullable
	@Override
	public PsiElement getLeftBrace()
	{
		return null;
	}

	@Nullable
	@Override
	public PsiElement getRightBrace()
	{
		return null;
	}

	@Nullable
	@Override
	public ShaderLabRole getRole()
	{
		return ShaderLabRoles.Shader;
	}
}

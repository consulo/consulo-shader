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
import consulo.annotation.access.RequiredWriteAction;
import consulo.application.AllIcons;
import consulo.document.util.TextRange;
import consulo.language.ast.ASTNode;
import consulo.language.ast.TokenSet;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiQualifiedReferenceElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.util.IncorrectOperationException;
import consulo.ui.image.Image;
import consulo.shader.shaderlab.ide.refactoring.ShaderRefactorUtil;
import consulo.shader.shaderlab.lang.ShaderMaterialAttribute;
import consulo.shader.shaderlab.lang.ShaderLabTypeResolver;
import consulo.shader.shaderlab.lang.parser.roles.ShaderLabRole;
import consulo.shader.shaderlab.lang.parser.roles.ShaderLabRoles;
import consulo.shader.shaderlab.lang.psi.light.LightShaderDef;
import consulo.shader.shaderlab.lang.psi.stub.index.ShaderDefIndex;
import consulo.util.collection.ContainerUtil;
import consulo.util.collection.SmartList;
import consulo.util.lang.StringUtil;

import org.jspecify.annotations.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 08.05.2015
 */
public class ShaderReference extends ShaderLabElement implements PsiQualifiedReferenceElement
{
	public static enum ResolveKind
	{
		ATTRIBUTE,
		ANOTHER_SHADER,
		SHADER_GUI,
		PROPERTY,
	}

	private static final TokenSet ourTokens = TokenSet.create(ShaderLabTokens.IDENTIFIER, ShaderLabTokens.STRING_LITERAL);

	public ShaderReference(ASTNode node)
	{
		super(node);
	}

	public ResolveKind kind()
	{
		PsiElement parent = getParent();
		if(parent instanceof ShaderPropertyAttribute)
		{
			return ResolveKind.ATTRIBUTE;
		}
		else if(parent instanceof ShaderSetTexture)
		{
			return ResolveKind.PROPERTY;
		}
		else if(parent instanceof ShaderSimpleValue)
		{
			ShaderLabRole role = ((ShaderSimpleValue) parent).getRole();
			if(role == ShaderLabRoles.CustomEditor)
			{
				return ResolveKind.SHADER_GUI;
			}

			if(role == ShaderLabRoles.Fallback || role == ShaderLabRoles.UsePass)
			{
				return ResolveKind.ANOTHER_SHADER;
			}
		}
		return ResolveKind.PROPERTY;
	}

	@Override
	public PsiReference getReference()
	{
		return this;
	}

	@RequiredReadAction
	@Override
	public PsiElement getElement()
	{
		return this;
	}

	@RequiredReadAction
	public PsiElement getReferenceElement()
	{
		return findNotNullChildByType(ourTokens);
	}

	@RequiredReadAction
	@Override
	public TextRange getRangeInElement()
	{
		PsiElement referenceElement = getReferenceElement();
		return new TextRange(0, referenceElement.getTextLength());
	}

	@Nullable
	@Override
	@RequiredReadAction
	public PsiElement resolve()
	{
		GlobalSearchScope scope = GlobalSearchScope.allScope(getProject());
		ResolveKind kind = kind();
		String referenceName = getReferenceName();
		switch(kind)
		{
			case ATTRIBUTE:
				try
				{
					ShaderMaterialAttribute attribute = ShaderMaterialAttribute.valueOf(referenceName);
					PsiElement type = findType(attribute.getType(), scope);
					if(type != null)
					{
						return type;
					}
				}
				catch(IllegalArgumentException ignored)
				{
					//
				}
				break;
			case SHADER_GUI:
				PsiElement type = findType(StringUtil.unquoteString(referenceName), scope);
				if(type != null)
				{
					return type;
				}
				break;
			case ANOTHER_SHADER:
				PsiElement anotherShader = findShader(referenceName, scope);
				if(anotherShader == null && isUsePass())
				{
					// UsePass "Shader/Name/PASSNAME" - the last segment is the pass name
					int slash = referenceName.lastIndexOf('/');
					if(slash > 0)
					{
						anotherShader = findShader(referenceName.substring(0, slash) + "\"", scope);
					}
				}
				return anotherShader;
			case PROPERTY:
				PsiFile containingFile = getContainingFile();
				if(!(containingFile instanceof ShaderLabFile))
				{
					return null;
				}
				for(ShaderProperty shaderProperty : ((ShaderLabFile) containingFile).getProperties())
				{
					if(Objects.equals(referenceName, shaderProperty.getName()))
					{
						return shaderProperty;
					}
				}
				break;
		}
		return null;
	}

	@RequiredReadAction
	private @Nullable PsiElement findShader(String quotedName, GlobalSearchScope scope)
	{
		ShaderDef shaderDef = LightShaderDef.getDefaultShaders(getProject()).get(quotedName);
		if(shaderDef != null)
		{
			return shaderDef;
		}
		Collection<ShaderDef> shaderDefs = ShaderDefIndex.getInstance().get(quotedName, getProject(), scope);
		return ContainerUtil.getFirstItem(shaderDefs);
	}

	private boolean isUsePass()
	{
		PsiElement parent = getParent();
		return parent instanceof ShaderSimpleValue && ((ShaderSimpleValue) parent).getRole() == ShaderLabRoles.UsePass;
	}

	@RequiredReadAction
	private @Nullable PsiElement findType(String qualifiedName, GlobalSearchScope scope)
	{
		return getProject().getExtensionPoint(ShaderLabTypeResolver.class).computeSafeIfAny(resolver -> resolver.findType(qualifiedName, scope));
	}

	@RequiredReadAction
	@Override
	public String getCanonicalText()
	{
		return getText();
	}

	@RequiredWriteAction
	@Override
	public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException
	{
		ShaderRefactorUtil.replaceIdentifier(getReferenceElement(), newElementName);
		return this;
	}

	@RequiredWriteAction
	@Override
	public PsiElement bindToElement(PsiElement element) throws IncorrectOperationException
	{
		return null;
	}

	@Override
	@RequiredReadAction
	public boolean isReferenceTo(PsiElement element)
	{
		return element.isEquivalentTo(resolve());
	}

	@RequiredReadAction
	@Override
	public Object[] getVariants()
	{
		ResolveKind kind = kind();
		final List<LookupElement> values = new SmartList<>();
		switch(kind)
		{
			case ATTRIBUTE:
				for(ShaderMaterialAttribute attribute : ShaderMaterialAttribute.values())
				{
					LookupElementBuilder builder = LookupElementBuilder.create(attribute.name());
					builder = builder.withIcon((Image) AllIcons.Nodes.Class);
					builder = builder.withTypeText(attribute.getType(), true);
					values.add(builder);
				}
				break;
			case PROPERTY:
				PsiFile containingFile = getContainingFile();
				if(containingFile instanceof ShaderLabFile)
				{
					consumeProperties((ShaderLabFile) containingFile, values::add);
				}
				break;
		}
		return values.toArray();
	}

	@RequiredReadAction
	public static void consumeProperties(ShaderLabFile file, Consumer<LookupElement> consumer)
	{
		for(ShaderProperty shaderProperty : file.getProperties())
		{
			String name = shaderProperty.getName();
			if(name == null)
			{
				continue;
			}
			LookupElementBuilder builder = LookupElementBuilder.create(name);
			builder = builder.withIcon((Image) AllIcons.Nodes.Property);
			ShaderPropertyType type = shaderProperty.getType();
			if(type != null)
			{
				builder = builder.withTypeText(type.getTargetText(), true);
			}
			consumer.accept(builder);
		}
	}

	@RequiredReadAction
	@Override
	public boolean isSoft()
	{
		// script types can only be checked when an engine plugin resolves them
		ResolveKind kind = kind();
		if(kind == ResolveKind.ATTRIBUTE || kind == ResolveKind.SHADER_GUI)
		{
			return !getProject().getExtensionPoint(ShaderLabTypeResolver.class).hasAnyExtensions();
		}
		return false;
	}

	@Override
	public void accept(SharpLabElementVisitor visitor)
	{
		visitor.visitReference(this);
	}

	@Nullable
	@Override
	public PsiElement getQualifier()
	{
		return findChildByClass(ShaderReference.class);
	}

	@Nullable
	@Override
	@RequiredReadAction
	public String getReferenceName()
	{
		PsiElement referenceElement = getReferenceElement();
		return referenceElement.getText();
	}
}

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

package consulo.shader.shaderlab.ide.completion;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.AllIcons;
import consulo.application.progress.ProgressManager;
import consulo.codeEditor.Caret;
import consulo.language.Language;
import consulo.language.editor.completion.*;
import consulo.language.editor.completion.lookup.*;
import consulo.language.pattern.StandardPatterns;
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.ProcessingContext;
import consulo.project.Project;
import consulo.ui.image.ImageEffects;
import consulo.shader.shaderlab.lang.ShaderLabFileType;
import consulo.shader.shaderlab.lang.ShaderLabKeywords;
import consulo.shader.shaderlab.lang.ShaderLabLanguage;
import consulo.shader.shaderlab.lang.ShaderLabPropertyType;
import consulo.shader.shaderlab.lang.ShaderLabTypeResolver;
import consulo.shader.shaderlab.lang.parser.roles.ShaderLabCompositeRole;
import consulo.shader.shaderlab.lang.parser.roles.ShaderLabRole;
import consulo.shader.shaderlab.lang.parser.roles.ShaderLabRoles;
import consulo.shader.shaderlab.lang.psi.*;
import consulo.shader.shaderlab.lang.psi.light.LightShaderDef;
import consulo.shader.shaderlab.lang.psi.stub.index.ShaderDefIndex;
import consulo.util.lang.StringUtil;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * @author VISTALL
 * @since 08.05.2015
 */
@ExtensionImpl
public class ShaderLabCompletionContributor extends CompletionContributor
{
	public ShaderLabCompletionContributor()
	{
		extend(CompletionType.BASIC, StandardPatterns.psiElement(ShaderLabTokens.IDENTIFIER).withParent(ShaderPropertyTypeElement.class), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(CompletionParameters parameters, ProcessingContext context, CompletionResultSet result)
			{
				for(ShaderLabPropertyType shaderLabPropertyType : ShaderLabPropertyType.values())
				{
					LookupElementBuilder builder = LookupElementBuilder.create(shaderLabPropertyType.getPresentableName());
					builder = builder.bold();

					result.addElement(builder);
				}
			}
		});

		extend(CompletionType.BASIC, StandardPatterns.psiElement().withParent(ShaderBraceOwner.class), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(CompletionParameters parameters, ProcessingContext context, CompletionResultSet result)
			{
				ShaderBraceOwner braceOwner = PsiTreeUtil.getParentOfType(parameters.getPosition(), ShaderBraceOwner.class);
				if(braceOwner == null)
				{
					return;
				}
				ShaderLabRole role = braceOwner.getRole();
				if(!(role instanceof ShaderLabCompositeRole))
				{
					return;
				}

				for(final ShaderLabRole labRole : ((ShaderLabCompositeRole) role).getRoles())
				{
					LookupElementBuilder builder = LookupElementBuilder.create(labRole.getName());
					if(labRole instanceof ShaderLabCompositeRole)
					{
						builder = builder.withInsertHandler(BracesInsertHandler.INSTANCE);
					}
					else
					{
						final String defaultInsertValue = labRole.getDefaultInsertValue();
						if(defaultInsertValue == null)
						{
							builder = builder.withInsertHandler(AddSpaceInsertHandler.INSTANCE);
						}
						else
						{
							builder = builder.withInsertHandler(new InsertHandler<LookupElement>()
							{
								@Override
								public void handleInsert(InsertionContext context, LookupElement item)
								{
									int offset = context.getTailOffset();
									offset = TailType.insertChar(context.getEditor(), offset, ' ');
									context.getDocument().insertString(offset, defaultInsertValue);

									Caret currentCaret = context.getEditor().getCaretModel().getCurrentCaret();
									currentCaret.setSelection(offset, offset + defaultInsertValue.length());
								}
							});
						}
					}

					result.addElement(builder);
				}

				// commands from the keyword table which are not modeled as roles yet
				Set<String> roleNames = new HashSet<>();
				for(ShaderLabRole labRole : ((ShaderLabCompositeRole) role).getRoles())
				{
					roleNames.add(labRole.getName().toLowerCase(Locale.ROOT));
				}

				for(String command : ShaderLabKeywords.getCommandsInBlock(role.getName()))
				{
					if(!roleNames.add(command.toLowerCase(Locale.ROOT)))
					{
						continue;
					}

					LookupElementBuilder builder = LookupElementBuilder.create(command);
					if(ShaderLabKeywords.getKind(command) == ShaderLabKeywords.Kind.BLOCK_COMMAND)
					{
						builder = builder.withInsertHandler(BracesInsertHandler.INSTANCE);
					}
					else if(ShaderLabKeywords.getKind(command) != ShaderLabKeywords.Kind.PROGRAM_DELIMITER)
					{
						builder = builder.withInsertHandler(AddSpaceInsertHandler.INSTANCE);
					}
					result.addElement(builder);
				}
			}
		});

		// keyword arguments of commands which are not modeled as roles yet, e.g. 'BlendOp <caret>'
		extend(CompletionType.BASIC, StandardPatterns.psiElement().afterLeaf(StandardPatterns.psiElement().withElementType(ShaderLabTokens.IDENTIFIER)), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(CompletionParameters parameters, ProcessingContext context, final CompletionResultSet result)
			{
				PsiElement prevLeaf = PsiTreeUtil.prevVisibleLeaf(parameters.getPosition());
				if(prevLeaf == null)
				{
					return;
				}

				for(String argument : ShaderLabKeywords.getArgumentKeywords(prevLeaf.getText()))
				{
					result.addElement(LookupElementBuilder.create(argument).bold());
				}
			}
		});

		extend(CompletionType.BASIC, StandardPatterns.psiElement().afterLeaf(StandardPatterns.psiElement().withElementType(ShaderLabKeyTokens.START_KEYWORD)), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(CompletionParameters parameters, ProcessingContext context, final CompletionResultSet result)
			{
				ShaderSimpleValue simpleValue = PsiTreeUtil.getParentOfType(parameters.getPosition(), ShaderSimpleValue.class);
				if(simpleValue == null)
				{
					return;
				}
				ShaderLabRole role = simpleValue.getRole();
				if(role == null || role != ShaderLabRoles.Fallback && role != ShaderLabRoles.UsePass)
				{
					return;
				}

				Project project = parameters.getPosition().getProject();
				Collection<String> allKeys = ShaderDefIndex.getInstance().getAllKeys(project);
				for(String key : allKeys)
				{
					ProgressManager.checkCanceled();

					Collection<ShaderDef> shaderDefs = ShaderDefIndex.getInstance().get(key, project, GlobalSearchScope.projectScope(project));
					if(shaderDefs.isEmpty())
					{
						continue;
					}
					LookupElementBuilder builder = LookupElementBuilder.create(key);
					builder = builder.withIcon(ShaderLabFileType.INSTANCE.getIcon());
					result.addElement(builder);
				}

				for(ShaderDef shaderDef : LightShaderDef.getDefaultShaders(project).values())
				{
					ProgressManager.checkCanceled();

					LookupElementBuilder builder = LookupElementBuilder.create(shaderDef.getName());
					builder = builder.withIcon(ImageEffects.layered(ShaderLabFileType.INSTANCE.getIcon(), AllIcons.Nodes.FinalMark));
					result.addElement(builder);
				}

				String defaultInsertValue = role.getDefaultInsertValue();
				if(defaultInsertValue != null)
				{
					result.addElement(LookupElementBuilder.create(defaultInsertValue).bold());
				}
			}
		});

		extend(CompletionType.BASIC, StandardPatterns.psiElement().afterLeaf(StandardPatterns.psiElement().withElementType(ShaderLabKeyTokens.START_KEYWORD)), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(CompletionParameters parameters, ProcessingContext context, final CompletionResultSet result)
			{
				ShaderSimpleValue simpleValue = PsiTreeUtil.getParentOfType(parameters.getPosition(), ShaderSimpleValue.class);
				if(simpleValue == null)
				{
					return;
				}
				ShaderLabRole role = simpleValue.getRole();

				for(String value : role.getValues())
				{
					LookupElementBuilder builder = LookupElementBuilder.create(value);
					builder = builder.bold();
					result.addElement(builder);
				}
			}
		});

		extend(CompletionType.BASIC, StandardPatterns.psiElement().afterLeaf(StandardPatterns.psiElement().withElementType(ShaderLabKeyTokens.START_KEYWORD)), new CompletionProvider()
		{
			@RequiredReadAction
			@Override
			public void addCompletions(CompletionParameters parameters, ProcessingContext context, final CompletionResultSet result)
			{
				ShaderSimpleValue simpleValue = PsiTreeUtil.getParentOfType(parameters.getPosition(), ShaderSimpleValue.class);
				if(simpleValue == null)
				{
					return;
				}
				ShaderLabRole role = simpleValue.getRole();
				if(role == null || role != ShaderLabRoles.CustomEditor)
				{
					return;
				}

				parameters.getPosition().getProject().getExtensionPoint(ShaderLabTypeResolver.class)
					.forEachExtensionSafe(resolver -> resolver.collectInheritors("UnityEditor.ShaderGUI", result::addElement));
			}
		});
	}

	@Override
	public Language getLanguage()
	{
		return ShaderLabLanguage.INSTANCE;
	}
}

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
package consulo.shader.glsl.impl.godot;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.completion.CompletionContributor;
import consulo.language.editor.completion.CompletionParameters;
import consulo.language.editor.completion.CompletionResultSet;
import consulo.language.editor.completion.CompletionType;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.editor.completion.lookup.ParenthesesInsertHandler;
import consulo.language.pattern.StandardPatterns;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.shader.glsl.GlslLanguage;
import consulo.shader.glsl.godot.model.GodotBuiltinFunction;
import consulo.shader.glsl.godot.model.GodotBuiltinVariable;
import consulo.shader.glsl.godot.model.GodotBuiltins;
import consulo.shader.glsl.godot.model.GodotDataType;
import consulo.shader.glsl.impl.psi.GlslCompoundStatement;
import consulo.shader.glsl.impl.psi.GlslHintList;
import consulo.shader.glsl.impl.psi.GlslRenderModeDeclaration;
import consulo.shader.glsl.impl.psi.GlslStencilModeDeclaration;
import consulo.shader.glsl.impl.psi.GlslTypeSpecifier;
import consulo.shader.glsl.impl.psi.GlslVariableDeclaration;
import consulo.shader.psi.ShaderMemberAccess;

import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Godot built-ins where they are valid: the functions and variables of the shader type and processing function in
 * function bodies, render and stencil modes in their declarations, and the hints of a uniform type after its {@code :}.
 */
@ExtensionImpl
public class GodotCompletionContributor extends CompletionContributor {
    public GodotCompletionContributor() {
        extend(CompletionType.BASIC, StandardPatterns.psiElement().withLanguage(GlslLanguage.INSTANCE), (parameters, context, result) -> {
            if (parameters.getPosition().getParent() instanceof ShaderMemberAccess) {
                // after a dot only the members, which the reference completes
                return;
            }
            addCompletions(parameters, result);
        });
    }

    @RequiredReadAction
    private static void addCompletions(CompletionParameters parameters, CompletionResultSet result) {
        PsiElement position = parameters.getPosition();
        GodotShaderContext shader = GodotShaderContext.of(position);
        if (shader == null) {
            return;
        }
        GodotBuiltins builtins = GodotBuiltins.getInstance();

        if (PsiTreeUtil.getParentOfType(position, GlslRenderModeDeclaration.class) != null) {
            if (shader.shaderType() != null) {
                addWords(result, builtins.getRenderModes(shader.shaderType()));
            }
            return;
        }
        if (PsiTreeUtil.getParentOfType(position, GlslStencilModeDeclaration.class) != null) {
            if (shader.shaderType() != null) {
                addWords(result, builtins.getStencilModes(shader.shaderType()));
            }
            return;
        }
        GlslHintList hints = PsiTreeUtil.getParentOfType(position, GlslHintList.class);
        if (hints != null) {
            GodotDataType type = uniformTypeOf(hints);
            addWords(result, type == null ? builtins.getAllUniformHints() : builtins.getUniformHints(type));
            return;
        }
        if (PsiTreeUtil.getParentOfType(position, GlslCompoundStatement.class) == null) {
            return;
        }

        Collection<GodotBuiltinVariable> variables = shader.shaderType() == null ? builtins.getAllVariables()
            : builtins.getVariables(shader.shaderType(), shader.processingFunction());
        Set<String> seen = new HashSet<>();
        for (GodotBuiltinVariable variable : variables) {
            if (seen.add(variable.name())) {
                result.addElement(LookupElementBuilder.create(variable.name())
                    .withIcon(PlatformIconGroup.nodesVariable())
                    .withTypeText(variable.type().getName()));
            }
        }
        List<GodotBuiltinFunction> functions = shader.shaderType() == null ? builtins.getAllFunctions()
            : builtins.getFunctions(shader.shaderType(), shader.processingFunction());
        for (GodotBuiltinFunction function : functions) {
            result.addElement(LookupElementBuilder.create(function, function.name())
                .withIcon(PlatformIconGroup.nodesFunction())
                .withTailText(function.getPresentableText().substring(function.getPresentableText().indexOf('(')), true)
                .withTypeText(function.returnType().getName())
                .withInsertHandler(ParenthesesInsertHandler.getInstance(!function.parameters().isEmpty())));
        }
    }

    @RequiredReadAction
    private static @Nullable GodotDataType uniformTypeOf(GlslHintList hints) {
        GlslVariableDeclaration declaration = PsiTreeUtil.getParentOfType(hints, GlslVariableDeclaration.class);
        GlslTypeSpecifier type = declaration == null ? null : declaration.getTypeSpecifier();
        return type == null ? null : GodotDataType.findByName(type.getText());
    }

    private static void addWords(CompletionResultSet result, Collection<String> words) {
        for (String word : words) {
            result.addElement(LookupElementBuilder.create(word));
        }
    }

    @Override
    public Language getLanguage() {
        return GlslLanguage.INSTANCE;
    }
}

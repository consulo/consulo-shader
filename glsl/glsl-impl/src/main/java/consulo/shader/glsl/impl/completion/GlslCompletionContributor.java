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
package consulo.shader.glsl.impl.completion;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.completion.CompletionContributor;
import consulo.language.editor.completion.CompletionType;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.editor.completion.CompletionResultSet;
import consulo.language.editor.completion.lookup.ParenthesesInsertHandler;
import consulo.language.pattern.StandardPatterns;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.shader.ShaderStage;
import consulo.shader.glsl.impl.builtins.GlslBuiltin;
import consulo.shader.glsl.impl.builtins.GlslBuiltins;
import consulo.shader.glsl.impl.psi.GlslCompoundStatement;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import consulo.shader.glsl.GlslLanguage;
import consulo.shader.glsl.impl.GlslParserDefinition;
import consulo.shader.glsl.impl.lexer.GlslKeywords;
import consulo.shader.impl.completion.ShaderReferenceCompletion;

/**
 * Keywords and built-in types of the file's dialect, and in function bodies of plain GLSL the built-in variables and
 * functions of the file's stage. Godot built-ins are completed by GodotCompletionContributor.
 */
@ExtensionImpl
public class GlslCompletionContributor extends CompletionContributor {
    public GlslCompletionContributor() {
        extend(CompletionType.BASIC, StandardPatterns.psiElement().withLanguage(GlslLanguage.INSTANCE), (parameters, context, result) -> {
            if (ShaderReferenceCompletion.addVariants(parameters, result)) {
                // after a dot only the members
                return;
            }
            boolean godot = GlslParserDefinition.isGodot(parameters.getOriginalFile().getLanguageVersion());
            for (String word : GlslKeywords.getWords(godot)) {
                result.addElement(LookupElementBuilder.create(word).bold());
            }
            if (!godot && PsiTreeUtil.getParentOfType(parameters.getPosition(), GlslCompoundStatement.class) != null) {
                addBuiltins(parameters.getOriginalFile().getVirtualFile(), result);
            }
        });
    }

    /**
     * The built-ins of the stage the file is for, or of every stage when its extension does not tell.
     */
    private static void addBuiltins(@Nullable VirtualFile file, CompletionResultSet result) {
        ShaderStage stage = GlslBuiltins.stageOf(file == null ? null : file.getExtension());
        Set<String> seen = new HashSet<>();
        for (GlslBuiltin builtin : GlslBuiltins.getAll()) {
            if (stage != null && !builtin.isAvailableIn(stage)) {
                continue;
            }
            if (builtin.function()) {
                result.addElement(LookupElementBuilder.create(builtin, builtin.name())
                    .withIcon(PlatformIconGroup.nodesFunction())
                    .withTailText(builtin.declaration().substring(builtin.declaration().indexOf('(')), true)
                    .withTypeText(builtin.declaration().substring(0, builtin.declaration().indexOf(' ')))
                    .withInsertHandler(ParenthesesInsertHandler.getInstance(!builtin.declaration().endsWith("()"))));
            }
            else if (seen.add(builtin.name())) {
                result.addElement(LookupElementBuilder.create(builtin.name())
                    .withIcon(PlatformIconGroup.nodesVariable())
                    .withTypeText(builtin.declaration().substring(0, builtin.declaration().lastIndexOf(' '))));
            }
        }
    }

    @Override
    public Language getLanguage() {
        return GlslLanguage.INSTANCE;
    }
}

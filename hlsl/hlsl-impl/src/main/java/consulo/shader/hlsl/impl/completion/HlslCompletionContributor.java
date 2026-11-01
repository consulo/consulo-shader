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
package consulo.shader.hlsl.impl.completion;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.completion.CompletionContributor;
import consulo.language.editor.completion.CompletionType;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.editor.completion.lookup.ParenthesesInsertHandler;
import consulo.language.pattern.StandardPatterns;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.shader.hlsl.HlslLanguage;
import consulo.shader.hlsl.impl.builtins.HlslBuiltins;
import consulo.shader.hlsl.impl.lexer.HlslKeywords;
import consulo.shader.impl.builtins.GeneratedDeclarationsFile;
import consulo.shader.impl.completion.ShaderReferenceCompletion;

import consulo.shader.psi.ShaderNameKind;

/**
 * Keyword and intrinsic function completion; names the file declares come from the reference.
 */
@ExtensionImpl
public class HlslCompletionContributor extends CompletionContributor {
    public HlslCompletionContributor() {
        extend(CompletionType.BASIC, StandardPatterns.psiElement().withLanguage(HlslLanguage.INSTANCE), (parameters, context, result) -> {
            if (ShaderReferenceCompletion.addVariants(parameters, result)) {
                // after a dot only the members
                return;
            }
            for (String keyword : HlslKeywords.getKeywords()) {
                result.addElement(LookupElementBuilder.create(keyword).bold());
            }
            GeneratedDeclarationsFile builtins = HlslBuiltins.getFile(parameters.getPosition().getProject());
            for (String intrinsic : builtins.getNames(ShaderNameKind.VALUE)) {
                result.addElement(LookupElementBuilder.create(intrinsic)
                    .withIcon(PlatformIconGroup.nodesMethod())
                    .withTailText("()", true)
                    .withInsertHandler(ParenthesesInsertHandler.getInstance(true)));
            }
        });
    }

    @Override
    public Language getLanguage() {
        return HlslLanguage.INSTANCE;
    }
}

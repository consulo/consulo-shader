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
package consulo.shader.impl.completion;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.editor.completion.CompletionParameters;
import consulo.language.editor.completion.CompletionResultSet;
import consulo.language.editor.completion.CompletionUtilCore;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.psi.PsiElement;
import consulo.shader.impl.resolve.ShaderResolver;
import consulo.shader.impl.type.ShaderType;
import consulo.shader.impl.type.ShaderTypeEvaluator;
import consulo.shader.psi.ShaderMemberAccess;
import consulo.shader.psi.ShaderNamedElement;
import consulo.shader.psi.ShaderReferenceElement;

import java.util.HashSet;
import java.util.Set;

/**
 * Completion of the names a reference may mean, by the same walk as resolve: the declarations visible at the caret,
 * one per name so that shadowed ones are left out, or after a dot the members of the qualifier's type. Completion runs
 * on a copy of the file; the declarations offered are the originals.
 */
public final class ShaderReferenceCompletion {
    private ShaderReferenceCompletion() {
    }

    /**
     * Adds the names; returns whether the caret is after a dot, where nothing else belongs.
     */
    @RequiredReadAction
    public static boolean addVariants(CompletionParameters parameters, CompletionResultSet result) {
        PsiElement parent = parameters.getPosition().getParent();
        if (parent instanceof ShaderMemberAccess access) {
            ShaderType type = ShaderTypeEvaluator.typeOf(access.getQualifier());
            if (type != null) {
                Set<String> names = new HashSet<>();
                ShaderTypeEvaluator.processMembers(type, member -> {
                    add(member, names, result);
                    return true;
                });
            }
            return true;
        }
        if (parent instanceof ShaderReferenceElement reference) {
            Set<String> names = new HashSet<>();
            for (ShaderNamedElement declaration : ShaderResolver.collectVisible(reference, reference.getNameKind())) {
                add(declaration, names, result);
            }
        }
        return false;
    }

    @RequiredReadAction
    private static void add(ShaderNamedElement declaration, Set<String> names, CompletionResultSet result) {
        String name = declaration.getReferencedName();
        if (name != null && names.add(name)) {
            result.addElement(LookupElementBuilder.create(CompletionUtilCore.getOriginalOrSelf(declaration), name));
        }
    }
}

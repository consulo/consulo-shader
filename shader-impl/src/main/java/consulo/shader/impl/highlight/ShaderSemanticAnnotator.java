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
package consulo.shader.impl.highlight;

import consulo.annotation.access.RequiredReadAction;
import consulo.colorScheme.TextAttributesKey;
import consulo.language.editor.annotation.AnnotationHolder;
import consulo.language.editor.annotation.Annotator;
import consulo.language.editor.annotation.HighlightSeverity;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiPolyVariantReference;
import consulo.language.psi.PsiReference;
import consulo.shader.impl.builtins.GeneratedDeclarationsFile;
import consulo.shader.impl.localize.ShaderImplLocalize;
import consulo.shader.impl.resolve.ShaderOverloads;
import consulo.shader.psi.ShaderFunction;
import consulo.shader.psi.ShaderNamedElement;
import consulo.shader.psi.ShaderParameter;
import consulo.shader.psi.ShaderReferenceElement;
import consulo.shader.psi.ShaderTypeDefinition;
import consulo.shader.psi.ShaderVariable;
import org.jspecify.annotations.Nullable;

/**
 * Colors names by what they declare or refer to: parameters, local and global variables, fields, functions, types and
 * built-ins; reports names that resolve to nothing, see {@link ShaderUnresolvedReferences}.
 */
public class ShaderSemanticAnnotator implements Annotator {
    @RequiredReadAction
    @Override
    public void annotate(PsiElement element, AnnotationHolder holder) {
        if (element instanceof ShaderReferenceElement reference) {
            PsiReference psiReference = reference.getReference();
            // of overloads, color by any of them
            PsiElement target = psiReference instanceof PsiPolyVariantReference poly ? ShaderOverloads.bestTarget(poly.multiResolve(false))
                : psiReference == null ? null : psiReference.resolve();
            TextAttributesKey key = target instanceof ShaderNamedElement declaration ? keyOf(declaration, true) : null;
            if (key != null) {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(reference.getReferenceNameElement()).textAttributes(key).create();
            }
            else if (target == null && ShaderUnresolvedReferences.isReported(reference)) {
                String name = reference.getReferenceNameElement().getText();
                holder.newAnnotation(HighlightSeverity.ERROR, ShaderImplLocalize.unresolvedSymbol(name))
                    .range(reference.getReferenceNameElement())
                    .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                    .create();
            }
        }
        else if (element instanceof ShaderNamedElement declaration) {
            PsiElement name = declaration.getNameIdentifier();
            TextAttributesKey key = keyOf(declaration, false);
            if (name != null && key != null) {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(name).textAttributes(key).create();
            }
        }
    }

    @RequiredReadAction
    private static @Nullable TextAttributesKey keyOf(ShaderNamedElement declaration, boolean usage) {
        if (usage && GeneratedDeclarationsFile.isBuiltin(declaration)) {
            return ShaderHighlightKeys.BUILTIN;
        }
        if (declaration instanceof ShaderParameter) {
            return ShaderHighlightKeys.PARAMETER;
        }
        if (declaration instanceof ShaderFunction) {
            return usage ? ShaderHighlightKeys.FUNCTION_CALL : ShaderHighlightKeys.FUNCTION_DECLARATION;
        }
        if (declaration instanceof ShaderTypeDefinition) {
            return ShaderHighlightKeys.TYPE;
        }
        if (declaration instanceof ShaderVariable) {
            for (PsiElement parent = declaration.getParent(); parent != null; parent = parent.getParent()) {
                if (parent instanceof ShaderFunction) {
                    return ShaderHighlightKeys.LOCAL_VARIABLE;
                }
                if (parent instanceof ShaderTypeDefinition) {
                    return ShaderHighlightKeys.FIELD;
                }
            }
            return ShaderHighlightKeys.GLOBAL_VARIABLE;
        }
        return null;
    }
}

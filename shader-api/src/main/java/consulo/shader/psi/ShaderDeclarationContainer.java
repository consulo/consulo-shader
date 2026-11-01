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
package consulo.shader.psi;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.psi.PsiElement;

import java.util.function.Predicate;

/**
 * A declaration provider whose declarations are what it contains: a variable declaration its variables and struct, a
 * declaration statement its declaration, a typedef its names, an enum itself and its constants, a constant buffer or a
 * namespace its members. Being an interface with a default method, generated PSI only has to implement it.
 */
public interface ShaderDeclarationContainer extends ShaderDeclarationProvider {
    @RequiredReadAction
    @Override
    default boolean processShaderDeclarations(ShaderNameKind kind, Predicate<? super ShaderNamedElement> processor) {
        // an enum is a type of its own; a constant buffer or namespace name is no value
        if ((this instanceof ShaderTypeDefinition || this instanceof ShaderVariable) && this instanceof ShaderNamedElement self
            && !ShaderDeclarationProvider.processIf(self, kind, processor)) {
            return false;
        }
        return processContents(this, kind, processor);
    }

    /**
     * Feeds the declarations inside {@code parent}: declaration providers and named declarations among its descendants,
     * not looking into named declarations (a variable's initializer) or nested scopes (a function's body).
     */
    @RequiredReadAction
    static boolean processContents(PsiElement parent, ShaderNameKind kind, Predicate<? super ShaderNamedElement> processor) {
        for (PsiElement child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof ShaderDeclarationProvider provider) {
                if (!provider.processShaderDeclarations(kind, processor)) {
                    return false;
                }
            }
            else if (child instanceof ShaderNamedElement declaration) {
                if (!ShaderDeclarationProvider.processIf(declaration, kind, processor)) {
                    return false;
                }
            }
            else if (!(child instanceof ShaderLocalScope) && !(child instanceof ShaderUnorderedScope)
                && !processContents(child, kind, processor)) {
                return false;
            }
        }
        return true;
    }
}

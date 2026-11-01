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
 * An element that declares names into the scope it is in: a declaration declares its variables and struct, an anonymous
 * interface block or a constant buffer its members, a function itself.
 */
public interface ShaderDeclarationProvider extends PsiElement {
    /**
     * Feeds the declarations of {@code kind} to {@code processor} until it returns false.
     *
     * @return false if the processor stopped
     */
    @RequiredReadAction
    boolean processShaderDeclarations(ShaderNameKind kind, Predicate<? super ShaderNamedElement> processor);

    /**
     * Feeds {@code declaration} if it is of {@code kind}.
     */
    static boolean processIf(ShaderNamedElement declaration, ShaderNameKind kind, Predicate<? super ShaderNamedElement> processor) {
        boolean type = declaration instanceof ShaderTypeDefinition;
        return type != (kind == ShaderNameKind.TYPE) || processor.test(declaration);
    }
}

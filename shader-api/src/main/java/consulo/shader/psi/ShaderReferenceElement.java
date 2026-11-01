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
import org.jspecify.annotations.Nullable;

/**
 * A name that refers to a declaration: a name in an expression or a type name.
 */
public interface ShaderReferenceElement extends PsiElement {
    ShaderNameKind getNameKind();

    /**
     * The identifier the reference is on: of a qualified name ({@code Light::Count}) the last part.
     */
    @RequiredReadAction
    PsiElement getReferenceNameElement();

    /**
     * The name before the last {@code ::} of a qualified name ({@code Light} of {@code Light::Count}), else
     * {@code null}.
     */
    @RequiredReadAction
    default @Nullable PsiElement getQualifierNameElement() {
        return null;
    }
}

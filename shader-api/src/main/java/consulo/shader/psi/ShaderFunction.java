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

import java.util.List;

/**
 * A function definition or prototype.
 */
public interface ShaderFunction extends ShaderNamedElement {
    @RequiredReadAction
    List<? extends ShaderParameter> getShaderParameters();

    /**
     * The body of a definition; {@code null} for a prototype.
     */
    @RequiredReadAction
    @Nullable
    PsiElement getBody();

    @RequiredReadAction
    default boolean isDefinition() {
        return getBody() != null;
    }
}

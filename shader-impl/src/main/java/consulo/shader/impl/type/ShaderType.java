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
package consulo.shader.impl.type;

import consulo.shader.psi.ShaderNamedElement;
import org.jspecify.annotations.Nullable;

/**
 * The type of an expression or declaration, as far as member access needs it.
 *
 * @param name        the type name: a built-in type ({@code float4}, {@code vec3}, {@code Texture2D}) or the name of
 *                    {@code declaration}
 * @param declaration what declares the members: a struct, an interface block, the class of a built-in type; a template
 *                    parameter before substitution; {@code null} for vectors, matrices and scalars
 * @param element     the template argument: the {@code float4} of {@code Texture2D<float4>}, the {@code Light} of
 *                    {@code StructuredBuffer<Light>}
 * @param arrayDepth  the number of array dimensions
 */
public record ShaderType(@Nullable String name, @Nullable ShaderNamedElement declaration, @Nullable ShaderType element, int arrayDepth) {
    public static ShaderType named(String name) {
        return new ShaderType(name, null, null, 0);
    }

    public ShaderType withArrayDepth(int arrayDepth) {
        return new ShaderType(name, declaration, element, arrayDepth);
    }
}

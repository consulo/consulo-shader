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

import org.jspecify.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Vectors and matrices of GLSL ({@code vec3}, {@code ivec2}, {@code mat4x3}) and HLSL ({@code float3}, {@code half2},
 * {@code float4x4}): their components, swizzles ({@code .xyz}, {@code .rgb}) and indexing.
 */
public final class ShaderVectorTypes {
    private static final Pattern GLSL_VECTOR = Pattern.compile("([biud]?)vec([234])");
    private static final Pattern GLSL_MATRIX = Pattern.compile("(d?)mat([234])(?:x([234]))?");
    private static final Pattern HLSL_NUMERIC = Pattern.compile(
        "(bool|int|uint|dword|half|float|double|min16float|min10float|min16int|min12int|min16uint|fixed"
            + "|int16_t|uint16_t|int32_t|uint32_t|int64_t|uint64_t|float16_t|float32_t|float64_t)([1-4])?(?:x([1-4]))?");
    private static final Pattern SWIZZLE = Pattern.compile("[xyzw]{1,4}|[rgba]{1,4}|[stpq]{1,4}");

    private ShaderVectorTypes() {
    }

    /**
     * The type of {@code type.member} when the member is a swizzle of a vector (or of an HLSL scalar), else
     * {@code null}.
     */
    public static @Nullable ShaderType swizzle(ShaderType type, String member) {
        String name = type.name();
        if (name == null || type.arrayDepth() > 0 || !SWIZZLE.matcher(member).matches()) {
            return null;
        }
        int size;
        Matcher glsl = GLSL_VECTOR.matcher(name);
        Matcher hlsl = HLSL_NUMERIC.matcher(name);
        if (glsl.matches()) {
            size = Integer.parseInt(glsl.group(2));
        }
        else if (hlsl.matches() && hlsl.group(3) == null) {
            size = hlsl.group(2) == null ? 1 : Integer.parseInt(hlsl.group(2));
        }
        else {
            return null;
        }
        for (char component : member.toCharArray()) {
            if (componentIndex(component) >= size) {
                return null;
            }
        }
        int count = member.length();
        if (glsl.matches()) {
            return ShaderType.named(count == 1 ? glslScalar(glsl.group(1)) : glsl.group(1) + "vec" + count);
        }
        return ShaderType.named(count == 1 ? hlsl.group(1) : hlsl.group(1) + count);
    }

    /**
     * The type of {@code v[i]} for a vector (a component) or a matrix (a GLSL column, an HLSL row), else {@code null}.
     */
    public static @Nullable ShaderType index(ShaderType type) {
        String name = type.name();
        if (name == null) {
            return null;
        }
        Matcher glslVector = GLSL_VECTOR.matcher(name);
        if (glslVector.matches()) {
            return ShaderType.named(glslScalar(glslVector.group(1)));
        }
        Matcher glslMatrix = GLSL_MATRIX.matcher(name);
        if (glslMatrix.matches()) {
            String rows = glslMatrix.group(3) != null ? glslMatrix.group(3) : glslMatrix.group(2);
            return ShaderType.named(glslMatrix.group(1) + "vec" + rows);
        }
        Matcher hlsl = HLSL_NUMERIC.matcher(name);
        if (hlsl.matches() && hlsl.group(2) != null) {
            return ShaderType.named(hlsl.group(3) != null ? hlsl.group(1) + hlsl.group(3) : hlsl.group(1));
        }
        return null;
    }

    private static int componentIndex(char component) {
        return switch (component) {
            case 'x', 'r', 's' -> 0;
            case 'y', 'g', 't' -> 1;
            case 'z', 'b', 'p' -> 2;
            default -> 3;
        };
    }

    private static String glslScalar(String prefix) {
        return switch (prefix) {
            case "b" -> "bool";
            case "i" -> "int";
            case "u" -> "uint";
            case "d" -> "double";
            default -> "float";
        };
    }
}

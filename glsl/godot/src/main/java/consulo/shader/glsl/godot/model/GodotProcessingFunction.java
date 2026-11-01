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
package consulo.shader.glsl.godot.model;

import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;

/**
 * A Godot processing function: the entry points ({@code void vertex()}, {@code void fragment()}, ...) the engine
 * calls, each allowed only in some shader types.
 * <p>
 * Built-in data derived from gdshader-support (https://github.com/akghxhs55/gdshader-support), MIT License, Copyright (c) 2025-2026 Jaeho Yi.
 */
public enum GodotProcessingFunction {
    VERTEX("vertex", GodotShaderType.SPATIAL, GodotShaderType.CANVAS_ITEM),
    FRAGMENT("fragment", GodotShaderType.SPATIAL, GodotShaderType.CANVAS_ITEM),
    LIGHT("light", GodotShaderType.SPATIAL, GodotShaderType.CANVAS_ITEM),
    START("start", GodotShaderType.PARTICLES),
    PROCESS("process", GodotShaderType.PARTICLES),
    SKY("sky", GodotShaderType.SKY),
    FOG("fog", GodotShaderType.FOG);

    private final String myName;
    private final Set<GodotShaderType> myShaderTypes;

    GodotProcessingFunction(String name, GodotShaderType first, GodotShaderType... rest) {
        myName = name;
        myShaderTypes = EnumSet.of(first, rest);
    }

    /**
     * The function name as written in the shader.
     */
    public String getName() {
        return myName;
    }

    public boolean isAllowedIn(GodotShaderType shaderType) {
        return myShaderTypes.contains(shaderType);
    }

    public static @Nullable GodotProcessingFunction findByName(String name) {
        for (GodotProcessingFunction function : values()) {
            if (function.myName.equals(name)) {
                return function;
            }
        }
        return null;
    }

    /**
     * The processing function called {@code name} in a shader of {@code shaderType}, or null when {@code name} is
     * an ordinary user function there.
     */
    public static @Nullable GodotProcessingFunction findByName(GodotShaderType shaderType, String name) {
        GodotProcessingFunction function = findByName(name);
        return function != null && function.isAllowedIn(shaderType) ? function : null;
    }
}

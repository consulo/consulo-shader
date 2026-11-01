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

import java.util.List;

/**
 * One overload of a built-in Godot shading language function, with the contexts it is available in. Generic
 * types ({@link GodotDataType#isGeneric()}) in the signature are resolved per call.
 * <p>
 * Built-in data derived from gdshader-support (https://github.com/akghxhs55/gdshader-support), MIT License, Copyright (c) 2025-2026 Jaeho Yi.
 */
public record GodotBuiltinFunction(String name,
                                   GodotDataType returnType,
                                   List<GodotParameter> parameters,
                                   List<GodotBuiltinContext> availability) {
    public GodotBuiltinFunction {
        parameters = List.copyOf(parameters);
        availability = List.copyOf(availability);
    }

    /**
     * The minimal number of arguments a call needs.
     */
    public int getRequiredParameterCount() {
        int count = 0;
        for (GodotParameter parameter : parameters) {
            if (!parameter.optional()) {
                count++;
            }
        }
        return count;
    }

    /**
     * The signature as written in Godot documentation, e.g. {@code vec_type mix(vec_type a, vec_type b, float c)}.
     */
    public String getPresentableText() {
        StringBuilder builder = new StringBuilder();
        builder.append(returnType.getName()).append(' ').append(name).append('(');
        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(parameters.get(i).getPresentableText());
        }
        return builder.append(')').toString();
    }
}

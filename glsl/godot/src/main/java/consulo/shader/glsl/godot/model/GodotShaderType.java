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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A Godot shader type, as declared by {@code shader_type <id>;}.
 * <p>
 * Built-in data derived from gdshader-support (https://github.com/akghxhs55/gdshader-support), MIT License, Copyright (c) 2025-2026 Jaeho Yi.
 */
public enum GodotShaderType {
    SPATIAL("spatial"),
    CANVAS_ITEM("canvas_item"),
    PARTICLES("particles"),
    SKY("sky"),
    FOG("fog");

    private final String myId;

    GodotShaderType(String id) {
        myId = id;
    }

    /**
     * The identifier written after {@code shader_type}.
     */
    public String getId() {
        return myId;
    }

    /**
     * The processing functions ({@code void vertex()} etc.) a shader of this type may define, in declaration order.
     */
    public List<GodotProcessingFunction> getProcessingFunctions() {
        List<GodotProcessingFunction> result = new ArrayList<>();
        for (GodotProcessingFunction function : GodotProcessingFunction.values()) {
            if (function.isAllowedIn(this)) {
                result.add(function);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public static @Nullable GodotShaderType findById(String id) {
        for (GodotShaderType type : values()) {
            if (type.myId.equals(id)) {
                return type;
            }
        }
        return null;
    }
}

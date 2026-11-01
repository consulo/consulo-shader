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
 * A built-in Godot shading language variable ({@code TIME}, {@code VERTEX}, {@code ALBEDO}, ...) with the contexts
 * it is available in. The same name may have several entries, e.g. {@code COLOR} differs in type or access between
 * processing functions.
 * <p>
 * Built-in data derived from gdshader-support (https://github.com/akghxhs55/gdshader-support), MIT License, Copyright (c) 2025-2026 Jaeho Yi.
 */
public record GodotBuiltinVariable(String name,
                                   GodotDataType type,
                                   GodotVariableAccess access,
                                   List<GodotBuiltinContext> availability) {
    public GodotBuiltinVariable {
        availability = List.copyOf(availability);
    }
}

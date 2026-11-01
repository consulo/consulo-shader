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

/**
 * A parameter of a built-in function or constructor; {@code optional} parameters may be left out of a call.
 * <p>
 * Built-in data derived from gdshader-support (https://github.com/akghxhs55/gdshader-support), MIT License, Copyright (c) 2025-2026 Jaeho Yi.
 */
public record GodotParameter(String name, GodotDataType type, GodotParameterQualifier qualifier, boolean optional) {
    public GodotParameter(String name, GodotDataType type) {
        this(name, type, GodotParameterQualifier.IN, false);
    }

    /**
     * The parameter as written in a signature, e.g. {@code out vec_type i}.
     */
    public String getPresentableText() {
        String prefix = qualifier == GodotParameterQualifier.IN ? "" : qualifier.getText() + " ";
        return prefix + type.getName() + " " + name;
    }
}

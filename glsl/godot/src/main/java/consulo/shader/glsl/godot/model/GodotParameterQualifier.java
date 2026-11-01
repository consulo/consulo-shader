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

/**
 * The direction qualifier of a built-in function parameter.
 * <p>
 * Built-in data derived from gdshader-support (https://github.com/akghxhs55/gdshader-support), MIT License, Copyright (c) 2025-2026 Jaeho Yi.
 */
public enum GodotParameterQualifier {
    IN("in"),
    OUT("out"),
    INOUT("inout");

    private final String myText;

    GodotParameterQualifier(String text) {
        myText = text;
    }

    /**
     * The qualifier keyword.
     */
    public String getText() {
        return myText;
    }

    public static @Nullable GodotParameterQualifier findByText(String text) {
        for (GodotParameterQualifier qualifier : values()) {
            if (qualifier.myText.equals(text)) {
                return qualifier;
            }
        }
        return null;
    }
}

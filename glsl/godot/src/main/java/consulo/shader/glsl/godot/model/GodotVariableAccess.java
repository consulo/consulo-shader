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
 * How shader code may use a built-in variable, written {@code in}, {@code out} and {@code inout} in the Godot
 * documentation.
 * <p>
 * Built-in data derived from gdshader-support (https://github.com/akghxhs55/gdshader-support), MIT License, Copyright (c) 2025-2026 Jaeho Yi.
 */
public enum GodotVariableAccess {
    READ("in"),
    WRITE("out"),
    READ_WRITE("inout");

    private final String myText;

    GodotVariableAccess(String text) {
        myText = text;
    }

    /**
     * The documentation notation: {@code in}, {@code out} or {@code inout}.
     */
    public String getText() {
        return myText;
    }

    public boolean isReadable() {
        return this != WRITE;
    }

    public boolean isWritable() {
        return this != READ;
    }

    public static @Nullable GodotVariableAccess findByText(String text) {
        for (GodotVariableAccess access : values()) {
            if (access.myText.equals(text)) {
                return access;
            }
        }
        return null;
    }
}

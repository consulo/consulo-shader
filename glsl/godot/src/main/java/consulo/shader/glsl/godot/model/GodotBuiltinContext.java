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
 * Where a built-in is available: a shader type plus either one processing function or, when
 * {@code processingFunction} is null, anywhere in a shader of that type.
 * <p>
 * Built-in data derived from gdshader-support (https://github.com/akghxhs55/gdshader-support), MIT License, Copyright (c) 2025-2026 Jaeho Yi.
 */
public record GodotBuiltinContext(GodotShaderType shaderType, @Nullable GodotProcessingFunction processingFunction) {
    public GodotBuiltinContext {
        if (processingFunction != null && !processingFunction.isAllowedIn(shaderType)) {
            throw new IllegalArgumentException(processingFunction.getName() + " is not a processing function of " + shaderType.getId());
        }
    }

    public boolean isCommon() {
        return processingFunction == null;
    }

    @Override
    public String toString() {
        GodotProcessingFunction function = processingFunction;
        return shaderType.getId() + ":" + (function == null ? "common" : function.getName());
    }
}

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
package consulo.shader.glsl.impl;

import consulo.shader.glsl.godot.model.GodotBuiltins;
import consulo.shader.glsl.godot.model.GodotDataType;
import consulo.shader.glsl.godot.model.GodotProcessingFunction;
import consulo.shader.glsl.godot.model.GodotShaderType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;

/**
 * The Godot built-in data loads and answers the lookups the editor uses.
 */
public class GodotBuiltinsTest {
    @Test
    public void loads() {
        GodotBuiltins builtins = GodotBuiltins.getInstance();
        Assertions.assertTrue(builtins.getAllFunctions().size() > 150, "functions");
        Assertions.assertTrue(builtins.getAllVariables().size() > 200, "variables");
    }

    @Test
    public void variablesDependOnProcessingFunction() {
        GodotBuiltins builtins = GodotBuiltins.getInstance();
        GodotProcessingFunction fragment = Objects.requireNonNull(GodotProcessingFunction.findByName(GodotShaderType.SPATIAL, "fragment"));
        GodotProcessingFunction vertex = Objects.requireNonNull(GodotProcessingFunction.findByName(GodotShaderType.SPATIAL, "vertex"));
        Assertions.assertNotNull(builtins.getVariable(GodotShaderType.SPATIAL, fragment, "ALBEDO"));
        Assertions.assertNull(builtins.getVariable(GodotShaderType.SPATIAL, vertex, "ALBEDO"));
        Assertions.assertNotNull(builtins.getVariable(GodotShaderType.SPATIAL, vertex, "VERTEX"));
    }

    @Test
    public void functionsModesAndHints() {
        GodotBuiltins builtins = GodotBuiltins.getInstance();
        Assertions.assertTrue(builtins.isFunctionName("texture"));
        Assertions.assertTrue(builtins.getRenderModes(GodotShaderType.SPATIAL).contains("unshaded"));
        Assertions.assertTrue(builtins.getUniformHints(Objects.requireNonNull(GodotDataType.findByName("sampler2D"))).contains("source_color"));
        Assertions.assertTrue(builtins.getAllUniformHints().contains("filter_linear_mipmap_anisotropic"));
    }
}

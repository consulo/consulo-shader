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
package consulo.shader.glsl.impl.builtins;

import consulo.shader.ShaderStage;

import java.util.Map;
import java.util.Set;

/**
 * A built-in variable or function of GLSL: its declaration, and the stages it exists in per target ({@code gl},
 * {@code es}, {@code vulkan}).
 */
public record GlslBuiltin(boolean function, String name, String declaration, Map<String, Set<ShaderStage>> stages) {
    public boolean isAvailable(String target, ShaderStage stage) {
        Set<ShaderStage> targetStages = stages.get(target);
        return targetStages != null && targetStages.contains(stage);
    }

    public boolean isAvailableIn(ShaderStage stage) {
        for (Set<ShaderStage> targetStages : stages.values()) {
            if (targetStages.contains(stage)) {
                return true;
            }
        }
        return false;
    }
}

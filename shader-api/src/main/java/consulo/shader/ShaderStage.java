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
package consulo.shader;

/**
 * A programmable stage of the graphics, compute or ray tracing pipeline, independent of the shading language.
 * Languages name the stages differently (a GLSL fragment shader is an HLSL pixel shader), but the stages are the same.
 */
public enum ShaderStage {
    VERTEX,
    /**
     * Tessellation control stage, the hull shader in HLSL.
     */
    TESSELLATION_CONTROL,
    /**
     * Tessellation evaluation stage, the domain shader in HLSL.
     */
    TESSELLATION_EVALUATION,
    GEOMETRY,
    /**
     * Fragment stage, the pixel shader in HLSL.
     */
    FRAGMENT,
    COMPUTE,
    /**
     * Task stage of the mesh pipeline, the amplification shader in HLSL.
     */
    TASK,
    MESH,
    RAY_GENERATION,
    INTERSECTION,
    ANY_HIT,
    CLOSEST_HIT,
    MISS,
    CALLABLE
}

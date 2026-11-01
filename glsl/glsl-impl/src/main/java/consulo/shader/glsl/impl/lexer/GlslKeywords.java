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
package consulo.shader.glsl.impl.lexer;

import consulo.language.ast.IElementType;
import consulo.shader.glsl.impl.psi.GlslTypes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Keywords and built-in type names of GLSL 4.60, GLSL ES 3.20, Vulkan GLSL with the common extensions (ray tracing,
 * mesh shaders, explicit arithmetic types), and of the Godot shading language. Each dialect's lexer only knows its own
 * words; the others are identifiers there.
 */
public final class GlslKeywords {
    private static final List<String> QUALIFIERS = List.of(
        "const", "in", "out", "inout", "attribute", "uniform", "varying", "buffer", "shared", "centroid", "sample", "patch",
        "flat", "smooth", "noperspective", "invariant", "precise",
        "coherent", "volatile", "restrict", "readonly", "writeonly",
        "devicecoherent", "queuefamilycoherent", "workgroupcoherent", "subgroupcoherent", "nonprivate",
        "rayPayloadEXT", "rayPayloadInEXT", "hitAttributeEXT", "callableDataEXT", "callableDataInEXT", "shaderRecordEXT",
        "taskPayloadSharedEXT", "perprimitiveEXT", "pervertexEXT", "nonuniformEXT"
    );

    private static final List<String> GODOT_QUALIFIERS = List.of("const", "in", "out", "inout", "uniform", "varying", "flat", "smooth");

    private static final List<String> PRECISION_QUALIFIERS = List.of("lowp", "mediump", "highp");

    private static final List<String> SCALAR_TYPES = List.of(
        "void", "bool", "int", "uint", "float", "double", "atomic_uint",
        "int8_t", "uint8_t", "int16_t", "uint16_t", "int32_t", "uint32_t", "int64_t", "uint64_t", "float16_t", "float32_t",
        "float64_t"
    );

    private static final List<String> VECTOR_PREFIXES = List.of(
        "", "b", "i", "u", "d", "i8", "u8", "i16", "u16", "i32", "u32", "i64", "u64", "f16", "f32", "f64"
    );

    private static final List<String> MATRIX_PREFIXES = List.of("", "d", "f16", "f32", "f64");

    private static final List<String> RESOURCE_DIMENSIONS = List.of(
        "1D", "2D", "3D", "Cube", "2DRect", "1DArray", "2DArray", "CubeArray", "Buffer", "2DMS", "2DMSArray"
    );

    private static final List<String> OTHER_TYPES = List.of(
        "sampler1DShadow", "sampler2DShadow", "samplerCubeShadow", "sampler2DRectShadow", "sampler1DArrayShadow",
        "sampler2DArrayShadow", "samplerCubeArrayShadow",
        "sampler", "samplerShadow", "subpassInput", "isubpassInput", "usubpassInput", "subpassInputMS", "isubpassInputMS",
        "usubpassInputMS",
        "samplerExternalOES",
        "accelerationStructureEXT", "rayQueryEXT"
    );

    private static final List<String> GODOT_TYPES = List.of(
        "void", "bool", "bvec2", "bvec3", "bvec4", "int", "ivec2", "ivec3", "ivec4", "uint", "uvec2", "uvec3", "uvec4",
        "float", "vec2", "vec3", "vec4", "mat2", "mat3", "mat4",
        "sampler2D", "isampler2D", "usampler2D", "sampler2DArray", "isampler2DArray", "usampler2DArray",
        "sampler3D", "isampler3D", "usampler3D", "samplerCube", "samplerCubeArray", "samplerExternalOES"
    );

    private static final Map<String, IElementType> STATEMENT_KEYWORDS = Map.ofEntries(
        Map.entry("struct", GlslTypes.STRUCT),
        Map.entry("if", GlslTypes.IF),
        Map.entry("else", GlslTypes.ELSE),
        Map.entry("switch", GlslTypes.SWITCH),
        Map.entry("case", GlslTypes.CASE),
        Map.entry("default", GlslTypes.DEFAULT),
        Map.entry("while", GlslTypes.WHILE),
        Map.entry("do", GlslTypes.DO),
        Map.entry("for", GlslTypes.FOR),
        Map.entry("break", GlslTypes.BREAK),
        Map.entry("continue", GlslTypes.CONTINUE),
        Map.entry("return", GlslTypes.RETURN),
        Map.entry("discard", GlslTypes.DISCARD),
        Map.entry("true", GlslTypes.BOOL_LITERAL),
        Map.entry("false", GlslTypes.BOOL_LITERAL)
    );

    private static final Map<String, IElementType> GLSL_ONLY_KEYWORDS = Map.of(
        "layout", GlslTypes.LAYOUT,
        "precision", GlslTypes.PRECISION,
        "subroutine", GlslTypes.SUBROUTINE,
        "demote", GlslTypes.EXTENSION_JUMP,
        "terminateInvocation", GlslTypes.EXTENSION_JUMP,
        "ignoreIntersectionEXT", GlslTypes.EXTENSION_JUMP,
        "terminateRayEXT", GlslTypes.EXTENSION_JUMP
    );

    private static final Map<String, IElementType> GODOT_ONLY_KEYWORDS = Map.of(
        "shader_type", GlslTypes.SHADER_TYPE,
        "render_mode", GlslTypes.RENDER_MODE,
        "stencil_mode", GlslTypes.STENCIL_MODE,
        "group_uniforms", GlslTypes.GROUP_UNIFORMS,
        "global", GlslTypes.GLOBAL,
        "instance", GlslTypes.INSTANCE
    );

    private static final Map<String, IElementType> GLSL_WORDS = createGlslWords();

    private static final Map<String, IElementType> GODOT_WORDS = createGodotWords();

    private GlslKeywords() {
    }

    /**
     * The token type of a word in the given dialect: a keyword, a built-in type, a boolean literal or an identifier.
     */
    public static IElementType tokenOf(CharSequence word, boolean godot) {
        String text = word.toString();
        IElementType type = (godot ? GODOT_WORDS : GLSL_WORDS).get(text);
        if (type != null) {
            return type;
        }
        if (godot && isGodotHint(text)) {
            return GlslTypes.HINT;
        }
        return GlslTypes.IDENTIFIER;
    }

    /**
     * Uniform hints of Godot: {@code source_color}, {@code hint_range}, {@code filter_linear_mipmap},
     * {@code repeat_enable}, {@code instance_index}, ...
     */
    public static boolean isGodotHint(String word) {
        return word.startsWith("hint_") || word.startsWith("filter_") || word.startsWith("repeat_") || word.equals("source_color")
            || word.equals("instance_index") || word.equals("color_conversion_disabled");
    }

    /**
     * Every keyword and type name of a dialect, for completion.
     */
    public static Set<String> getWords(boolean godot) {
        return (godot ? GODOT_WORDS : GLSL_WORDS).keySet();
    }

    private static Map<String, IElementType> createGlslWords() {
        Map<String, IElementType> words = new HashMap<>(STATEMENT_KEYWORDS);
        words.putAll(GLSL_ONLY_KEYWORDS);
        for (String qualifier : QUALIFIERS) {
            words.put(qualifier, GlslTypes.QUALIFIER);
        }
        for (String qualifier : PRECISION_QUALIFIERS) {
            words.put(qualifier, GlslTypes.PRECISION_QUALIFIER);
        }
        for (String type : createGlslTypes()) {
            words.put(type, GlslTypes.TYPE);
        }
        return Map.copyOf(words);
    }

    private static Map<String, IElementType> createGodotWords() {
        Map<String, IElementType> words = new HashMap<>(STATEMENT_KEYWORDS);
        words.putAll(GODOT_ONLY_KEYWORDS);
        for (String qualifier : GODOT_QUALIFIERS) {
            words.put(qualifier, GlslTypes.QUALIFIER);
        }
        for (String qualifier : PRECISION_QUALIFIERS) {
            words.put(qualifier, GlslTypes.PRECISION_QUALIFIER);
        }
        for (String type : GODOT_TYPES) {
            words.put(type, GlslTypes.TYPE);
        }
        return Map.copyOf(words);
    }

    private static Set<String> createGlslTypes() {
        Set<String> types = new HashSet<>(SCALAR_TYPES);
        types.addAll(OTHER_TYPES);
        for (String prefix : VECTOR_PREFIXES) {
            for (int size = 2; size <= 4; size++) {
                types.add(prefix + "vec" + size);
            }
        }
        for (String prefix : MATRIX_PREFIXES) {
            for (int columns = 2; columns <= 4; columns++) {
                types.add(prefix + "mat" + columns);
                for (int rows = 2; rows <= 4; rows++) {
                    types.add(prefix + "mat" + columns + "x" + rows);
                }
            }
        }
        for (String kind : List.of("", "i", "u")) {
            for (String dimension : RESOURCE_DIMENSIONS) {
                types.add(kind + "sampler" + dimension);
                types.add(kind + "image" + dimension);
                // the Vulkan texture types (texture2D) stay identifiers: legacy GLSL has functions of the same names
                // (texture2D(sampler2D, vec2)), and an identifier still parses as a type name
            }
        }
        return types;
    }
}

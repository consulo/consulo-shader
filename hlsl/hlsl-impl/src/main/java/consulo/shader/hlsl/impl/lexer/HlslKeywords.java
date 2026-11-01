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
package consulo.shader.hlsl.impl.lexer;

import consulo.language.ast.IElementType;
import consulo.shader.hlsl.impl.psi.HlslTypes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Keywords and built-in type names of HLSL (Shader Model 5 and 6, HLSL 2021), of the Direct3D effect framework and of
 * the Cg dialect.
 */
public final class HlslKeywords {
    /**
     * Scalar types that also exist as vectors ({@code float3}) and matrices ({@code float4x4}).
     */
    private static final List<String> NUMERIC_TYPES = List.of(
        "bool", "int", "uint", "dword", "half", "float", "double",
        "min16float", "min10float", "min16int", "min12int", "min16uint",
        "int16_t", "uint16_t", "int32_t", "uint32_t", "int64_t", "uint64_t", "float16_t", "float32_t", "float64_t",
        // Cg
        "fixed"
    );

    private static final List<String> OTHER_TYPES = List.of(
        "void", "string", "vector", "matrix",
        // resources
        "Buffer", "RWBuffer", "ByteAddressBuffer", "RWByteAddressBuffer", "StructuredBuffer", "RWStructuredBuffer",
        "AppendStructuredBuffer", "ConsumeStructuredBuffer", "ConstantBuffer", "TextureBuffer",
        "Texture1D", "Texture1DArray", "Texture2D", "Texture2DArray", "Texture2DMS", "Texture2DMSArray", "Texture3D",
        "TextureCube", "TextureCubeArray", "RWTexture1D", "RWTexture1DArray", "RWTexture2D", "RWTexture2DArray", "RWTexture3D",
        "RWTexture2DMS", "RWTexture2DMSArray", "FeedbackTexture2D", "FeedbackTexture2DArray",
        "RasterizerOrderedBuffer", "RasterizerOrderedByteAddressBuffer", "RasterizerOrderedStructuredBuffer",
        "RasterizerOrderedTexture1D", "RasterizerOrderedTexture1DArray", "RasterizerOrderedTexture2D",
        "RasterizerOrderedTexture2DArray", "RasterizerOrderedTexture3D", "RaytracingAccelerationStructure", "RayQuery",
        "SamplerState", "SamplerComparisonState", "sampler", "sampler1D", "sampler2D", "sampler3D", "samplerCUBE",
        "texture", "texture1D", "texture2D", "texture3D", "textureCUBE",
        "SubpassInput", "SubpassInputMS",
        "InputPatch", "OutputPatch", "PointStream", "LineStream", "TriangleStream",
        // effect framework
        // (not PixelShader, VertexShader, GeometryShader, ...: code names functions so, and as types they are plain names)
        "BlendState", "DepthStencilState", "RasterizerState", "pixelshader", "vertexshader", "pixelfragment", "vertexfragment",
        "stateblock",
        // Cg
        "samplerRECT", "sampler1DARRAY", "sampler2DARRAY", "samplerCUBEARRAY", "textureRECT"
    );

    private static final List<String> QUALIFIERS = List.of(
        "const", "static", "uniform", "extern", "volatile", "inline", "export", "in", "out", "inout", "row_major", "column_major",
        "groupshared", "shared", "nointerpolation", "noperspective", "centroid", "globallycoherent", "unsigned", "signed",
        "snorm", "unorm",
        // Cg
        "varying"
    );

    /**
     * Qualifiers that real code also uses as names: {@code float4 sample = t.Sample(s, uv);}.
     */
    private static final List<String> SOFT_QUALIFIERS = List.of(
        "precise", "linear", "sample", "center",
        // geometry shader input primitives
        "point", "line", "lineadj", "triangle", "triangleadj",
        // mesh shader outputs
        "vertices", "indices", "primitives", "payload"
    );

    private static final Map<String, IElementType> OTHER_KEYWORDS = Map.ofEntries(
        Map.entry("struct", HlslTypes.STRUCT),
        Map.entry("class", HlslTypes.STRUCT),
        Map.entry("interface", HlslTypes.STRUCT),
        Map.entry("cbuffer", HlslTypes.CBUFFER),
        Map.entry("tbuffer", HlslTypes.CBUFFER),
        Map.entry("typedef", HlslTypes.TYPEDEF),
        Map.entry("namespace", HlslTypes.NAMESPACE),
        Map.entry("enum", HlslTypes.ENUM),
        Map.entry("template", HlslTypes.TEMPLATE),
        Map.entry("typename", HlslTypes.TYPENAME),
        Map.entry("register", HlslTypes.REGISTER),
        Map.entry("packoffset", HlslTypes.PACKOFFSET),
        Map.entry("this", HlslTypes.THIS),
        Map.entry("sizeof", HlslTypes.SIZEOF),
        Map.entry("technique", HlslTypes.TECHNIQUE),
        Map.entry("Technique", HlslTypes.TECHNIQUE),
        Map.entry("technique10", HlslTypes.TECHNIQUE),
        Map.entry("technique11", HlslTypes.TECHNIQUE),
        Map.entry("pass", HlslTypes.PASS),
        Map.entry("Pass", HlslTypes.PASS),
        Map.entry("compile", HlslTypes.COMPILE),
        Map.entry("sampler_state", HlslTypes.SAMPLER_STATE),
        Map.entry("operator", HlslTypes.OPERATOR),
        Map.entry("if", HlslTypes.IF),
        Map.entry("else", HlslTypes.ELSE),
        Map.entry("switch", HlslTypes.SWITCH),
        Map.entry("case", HlslTypes.CASE),
        Map.entry("default", HlslTypes.DEFAULT),
        Map.entry("while", HlslTypes.WHILE),
        Map.entry("do", HlslTypes.DO),
        Map.entry("for", HlslTypes.FOR),
        Map.entry("break", HlslTypes.BREAK),
        Map.entry("continue", HlslTypes.CONTINUE),
        Map.entry("return", HlslTypes.RETURN),
        Map.entry("discard", HlslTypes.DISCARD),
        Map.entry("true", HlslTypes.BOOL_LITERAL),
        Map.entry("false", HlslTypes.BOOL_LITERAL)
    );

    private static final Map<String, IElementType> WORDS = createWords();

    private HlslKeywords() {
    }

    /**
     * The token type of a word: a keyword, a built-in type, a boolean literal or an identifier.
     */
    public static IElementType tokenOf(CharSequence word) {
        IElementType type = WORDS.get(word.toString());
        return type == null ? HlslTypes.IDENTIFIER : type;
    }

    /**
     * Every keyword and type name, for completion.
     */
    public static Set<String> getKeywords() {
        return WORDS.keySet();
    }

    private static Map<String, IElementType> createWords() {
        Map<String, IElementType> words = new HashMap<>(OTHER_KEYWORDS);
        for (String qualifier : QUALIFIERS) {
            words.put(qualifier, HlslTypes.QUALIFIER);
        }
        for (String qualifier : SOFT_QUALIFIERS) {
            words.put(qualifier, HlslTypes.SOFT_QUALIFIER);
        }
        for (String type : OTHER_TYPES) {
            words.put(type, HlslTypes.TYPE);
        }
        for (String type : NUMERIC_TYPES) {
            words.put(type, HlslTypes.TYPE);
            for (int rows = 1; rows <= 4; rows++) {
                words.put(type + rows, HlslTypes.TYPE);
                for (int columns = 1; columns <= 4; columns++) {
                    words.put(type + rows + "x" + columns, HlslTypes.TYPE);
                }
            }
        }
        return Map.copyOf(words);
    }
}

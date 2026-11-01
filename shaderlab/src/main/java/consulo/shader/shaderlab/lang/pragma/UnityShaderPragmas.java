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

package consulo.shader.shaderlab.lang.pragma;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Unity specific {@code #pragma} directives of shader programs (CGPROGRAM/HLSLPROGRAM blocks, .compute files) and the
 * well-known shader define symbols. Pure data, no IDE dependencies.
 * <p>
 * Ported from JetBrains resharper-unity (src/Unity.Shaders/ShaderLab/Language/UnityHlslPragmas.cs,
 * UnityComputeHlslPragmas.cs, ShaderFeatureType.cs, ShaderLabPragmaInfo.cs, HlslSupport/HlslConstants.cs and
 * HlslSupport/ShaderVariants/ShaderApiDefineSymbolDescriptor.cs, ShaderPlatformDefineSymbolDescriptor.cs,
 * UrtCompilationModeDefineSymbolDescriptor.cs), Apache License 2.0.
 */
public final class UnityShaderPragmas
{
    public static final int SHADER_TARGET_25 = 25;
    public static final int SHADER_TARGET_35 = 35;
    public static final int SHADER_TARGET_40 = 40;
    public static final int SHADER_TARGET_46 = 46;

    /**
     * How a pragma contributes shader keywords (variants)
     */
    public enum FeatureType
    {
        NONE,
        /**
         * {@code multi_compile A B C} - explicit keyword list, {@code _} (or {@code __}) declares the "no keyword" variant
         */
        KEYWORD_LIST,
        /**
         * {@code shader_feature A} - like {@link #KEYWORD_LIST}, but a single keyword implies the disabled variant
         */
        KEYWORD_LIST_WITH_DISABLED_VARIANT_FOR_SINGLE_KEYWORD,
        /**
         * {@code multi_compile_fwdadd} - a built-in keyword set
         */
        IMPLICIT_KEYWORDS,
        /**
         * {@code multi_compile_fog} - a built-in keyword set which also has the "no keyword" variant
         */
        IMPLICIT_KEYWORDS_WITH_DISABLED_VARIANT
    }

    /**
     * @param name                 pragma name, e.g. {@code multi_compile_local}
     * @param hasRequiredSpec      the pragma requires arguments
     * @param hasFunctionReference the first argument references an HLSL function ({@code vertex vert})
     * @param featureType          how the pragma declares shader keywords
     * @param implicitKeywords     keywords declared by built-in {@code multi_compile_*} shortcuts
     * @param impliedShaderTarget  minimal shader target implied by the pragma, {@code 0} when none
     */
    public record Pragma(String name,
                         boolean hasRequiredSpec,
                         boolean hasFunctionReference,
                         FeatureType featureType,
                         List<String> implicitKeywords,
                         int impliedShaderTarget)
    {
        public boolean declaresKeywords()
        {
            return featureType != FeatureType.NONE;
        }

        Pragma withSuffix(String suffix)
        {
            return new Pragma(name + suffix, hasRequiredSpec, hasFunctionReference, featureType, implicitKeywords, impliedShaderTarget);
        }
    }

    public static final Pragma MULTI_COMPILE = keywordList("multi_compile", FeatureType.KEYWORD_LIST);
    public static final Pragma MULTI_COMPILE_LOCAL = MULTI_COMPILE.withSuffix("_local");
    public static final Pragma SHADER_FEATURE = keywordList("shader_feature", FeatureType.KEYWORD_LIST_WITH_DISABLED_VARIANT_FOR_SINGLE_KEYWORD);
    public static final Pragma SHADER_FEATURE_LOCAL = SHADER_FEATURE.withSuffix("_local");
    public static final Pragma DYNAMIC_BRANCH = keywordList("dynamic_branch", FeatureType.KEYWORD_LIST);
    public static final Pragma DYNAMIC_BRANCH_LOCAL = DYNAMIC_BRANCH.withSuffix("_local");
    public static final Pragma ENABLE_D3D11_DEBUG_SYMBOLS = simple("enable_d3d11_debug_symbols", false);
    public static final Pragma ONLY_RENDERERS = simple("only_renderers", true);
    public static final Pragma EXCLUDE_RENDERERS = simple("exclude_renderers", true);
    public static final Pragma REQUIRE = simple("require", true);
    public static final Pragma SKIP_OPTIMIZATIONS = simple("skip_optimizations", true);
    public static final Pragma DISABLE_FASTMATH = simple("disable_fastmath", false);
    public static final Pragma HLSLCC_BYTECODE_DISASSEMBLY = simple("hlslcc_bytecode_disassembly", false);
    public static final Pragma KERNEL = new Pragma("kernel", true, true, FeatureType.NONE, List.of(), 0);

    /**
     * Shader stage suffixes accepted by {@code multi_compile} and {@code shader_feature} (and their {@code _local} forms)
     */
    public static final List<String> STAGE_SUFFIXES = List.of("_vertex", "_fragment", "_hull", "_domain", "_geometry", "_raytracing");

    /**
     * Pragmas of ShaderLab program blocks
     */
    public static final List<Pragma> SHADER_PRAGMAS = buildShaderPragmas();

    /**
     * Pragmas of compute shaders ({@code .compute}). {@code warning} is the standard HLSL pragma
     */
    public static final List<Pragma> COMPUTE_PRAGMAS = List.of(
        KERNEL, simple("warning", true), MULTI_COMPILE, MULTI_COMPILE_LOCAL, ENABLE_D3D11_DEBUG_SYMBOLS, REQUIRE, EXCLUDE_RENDERERS,
        ONLY_RENDERERS, HLSLCC_BYTECODE_DISASSEMBLY, DISABLE_FASTMATH
    );

    // region define symbols (HlslSupport/ShaderVariants)
    public static final String SHADER_API_D3D11 = "SHADER_API_D3D11";
    public static final String SHADER_API_GLCORE = "SHADER_API_GLCORE";
    public static final String SHADER_API_GLES = "SHADER_API_GLES";
    public static final String SHADER_API_GLES3 = "SHADER_API_GLES3";
    public static final String SHADER_API_METAL = "SHADER_API_METAL";
    public static final String SHADER_API_VULKAN = "SHADER_API_VULKAN";
    public static final String SHADER_API_D3D11_9X = "SHADER_API_D3D11_9X";

    /**
     * Mutually exclusive graphics API symbols, the first one is the default
     */
    public static final List<String> SHADER_API_SYMBOLS = List.of(SHADER_API_D3D11, SHADER_API_GLCORE, SHADER_API_GLES, SHADER_API_GLES3,
        SHADER_API_METAL, SHADER_API_VULKAN, SHADER_API_D3D11_9X);

    public static final String SHADER_API_DESKTOP = "SHADER_API_DESKTOP";
    public static final String SHADER_API_MOBILE = "SHADER_API_MOBILE";

    /**
     * Mutually exclusive platform symbols, the first one is the default
     */
    public static final List<String> SHADER_PLATFORM_SYMBOLS = List.of(SHADER_API_DESKTOP, SHADER_API_MOBILE);

    /**
     * Unified ray tracing backend symbols (only for URT shader files), the first one is the default
     */
    public static final List<String> URT_COMPILATION_MODE_SYMBOLS = List.of("UNIFIED_RT_BACKEND_COMPUTE", "UNIFIED_RT_BACKEND_HARDWARE");

    /**
     * Groups of mutually exclusive define symbols; the first symbol of every group is the default one
     */
    public static final List<List<String>> DEFINE_SYMBOL_GROUPS = List.of(SHADER_API_SYMBOLS, SHADER_PLATFORM_SYMBOLS, URT_COMPILATION_MODE_SYMBOLS);
    // endregion

    private static final Map<String, Pragma> ourShaderPragmas = index(SHADER_PRAGMAS);

    private static final Map<String, Pragma> ourComputePragmas = index(COMPUTE_PRAGMAS);

    private UnityShaderPragmas()
    {
    }

    /**
     * @return the pragma of a ShaderLab program block, case-sensitive like Unity
     */
    @Nullable
    public static Pragma findShaderPragma(String name)
    {
        return ourShaderPragmas.get(name);
    }

    @Nullable
    public static Pragma findComputePragma(String name)
    {
        return ourComputePragmas.get(name);
    }

    /**
     * @return the group of mutually exclusive define symbols {@code symbol} belongs to, or {@code null}
     */
    @Nullable
    public static List<String> findDefineSymbolGroup(String symbol)
    {
        for (List<String> group : DEFINE_SYMBOL_GROUPS)
        {
            if (group.contains(symbol))
            {
                return group;
            }
        }
        return null;
    }

    public static boolean isDefaultDefineSymbol(String symbol)
    {
        List<String> group = findDefineSymbolGroup(symbol);
        return group != null && group.get(0).equals(symbol);
    }

    private static Pragma simple(String name, boolean hasRequiredSpec)
    {
        return new Pragma(name, hasRequiredSpec, false, FeatureType.NONE, List.of(), 0);
    }

    private static Pragma keywordList(String name, FeatureType featureType)
    {
        return new Pragma(name, true, false, featureType, List.of(), 0);
    }

    private static Pragma implicitKeywords(String name, String... keywords)
    {
        return new Pragma(name, false, false, FeatureType.IMPLICIT_KEYWORDS, List.of(keywords), 0);
    }

    private static Pragma implicitKeywordsWithDisabledVariant(String name, String... keywords)
    {
        return new Pragma(name, false, false, FeatureType.IMPLICIT_KEYWORDS_WITH_DISABLED_VARIANT, List.of(keywords), 0);
    }

    private static Pragma function(String name, int impliedShaderTarget)
    {
        return new Pragma(name, true, true, FeatureType.NONE, List.of(), impliedShaderTarget);
    }

    private static List<Pragma> buildShaderPragmas()
    {
        List<Pragma> result = new ArrayList<>();
        result.add(simple("editor_sync_compilation", false));
        result.add(simple("enable_cbuffer", false));
        // JetBrains spells this one 'mutli_compile_fwdbase'
        result.add(implicitKeywords("multi_compile_fwdbase", "DIRECTIONAL", "LIGHTMAP_ON", "DIRLIGHTMAP_COMBINED", "DYNAMICLIGHTMAP_ON",
            "SHADOWS_SCREEN", "SHADOWS_SHADOWMASK", "LIGHTMAP_SHADOW_MIXING", "LIGHTPROBE_SH"));
        result.add(implicitKeywords("multi_compile_fwdbasealpha", "DIRECTIONAL", "LIGHTMAP_ON", "DIRLIGHTMAP_COMBINED", "DYNAMICLIGHTMAP_ON",
            "LIGHTMAP_SHADOW_MIXING", "VERTEXLIGHT_ON", "LIGHTPROBE_SH"));
        result.add(implicitKeywords("multi_compile_fwdadd", "POINT", "DIRECTIONAL", "SPOT", "POINT_COOKIE", "DIRECTIONAL_COOKIE"));
        result.add(implicitKeywords("multi_compile_fwdadd_fullshadows", "POINT", "DIRECTIONAL", "SPOT", "POINT_COOKIE", "DIRECTIONAL_COOKIE",
            "SHADOWS_DEPTH", "SHADOWS_SCREEN", "SHADOWS_CUBE", "SHADOWS_SOFT", "SHADOWS_SHADOWMASK", "LIGHTMAP_SHADOW_MIXING"));
        result.add(implicitKeywords("multi_compile_lightpass", "POINT", "DIRECTIONAL", "SPOT", "POINT_COOKIE", "DIRECTIONAL_COOKIE",
            "SHADOWS_DEPTH", "SHADOWS_SCREEN", "SHADOWS_CUBE", "SHADOWS_SOFT", "SHADOWS_SHADOWMASK", "LIGHTMAP_SHADOW_MIXING"));
        result.add(implicitKeywords("multi_compile_shadowcaster", "SHADOWS_DEPTH", "SHADOWS_CUBE"));
        result.add(implicitKeywordsWithDisabledVariant("multi_compile_shadowcollector", "SHADOWS_SPLIT_SPHERES", "SHADOWS_SINGLE_CASCADE"));
        result.add(implicitKeywordsWithDisabledVariant("multi_compile_prepassfinal", "LIGHTMAP_ON", "DIRLIGHTMAP_COMBINED", "DYNAMICLIGHTMAP_ON",
            "UNITY_HDR_ON", "SHADOWS_SHADOWMASK", "LIGHTPROBE_SH"));
        result.add(implicitKeywordsWithDisabledVariant("multi_compile_particles", "SOFTPARTICLES_ON"));
        result.add(implicitKeywordsWithDisabledVariant("multi_compile_fog", "FOG_LINEAR", "FOG_EXP", "FOG_EXP2"));
        result.add(implicitKeywordsWithDisabledVariant("multi_compile_instancing", "INSTANCING_ON", "PROCEDURAL_ON"));
        result.add(ENABLE_D3D11_DEBUG_SYMBOLS);
        result.add(HLSLCC_BYTECODE_DISASSEMBLY);
        result.add(ONLY_RENDERERS);
        result.add(EXCLUDE_RENDERERS);
        result.add(REQUIRE);
        result.add(SKIP_OPTIMIZATIONS);
        result.add(DISABLE_FASTMATH);
        result.add(simple("target", true));
        result.add(simple("hardware_tier_variants", true));
        result.add(simple("skip_variants", true));
        result.add(simple("instancing_options", true));
        result.add(DYNAMIC_BRANCH);
        result.add(DYNAMIC_BRANCH_LOCAL);
        for (Pragma pragma : List.of(MULTI_COMPILE, MULTI_COMPILE_LOCAL, SHADER_FEATURE, SHADER_FEATURE_LOCAL))
        {
            result.add(pragma);
            for (String suffix : STAGE_SUFFIXES)
            {
                result.add(pragma.withSuffix(suffix));
            }
        }
        result.add(function("geometry", SHADER_TARGET_40));
        result.add(function("hull", SHADER_TARGET_46));
        result.add(function("domain", SHADER_TARGET_46));
        result.add(function("vertex", 0));
        result.add(function("fragment", 0));
        result.add(function("surface", 0));
        return List.copyOf(result);
    }

    private static Map<String, Pragma> index(List<Pragma> pragmas)
    {
        Map<String, Pragma> result = new LinkedHashMap<>();
        for (Pragma pragma : pragmas)
        {
            result.putIfAbsent(pragma.name(), pragma);
        }
        return Collections.unmodifiableMap(result);
    }
}

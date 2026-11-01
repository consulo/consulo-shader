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

package consulo.shader.shaderlab.lang;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The full ShaderLab keyword table. ShaderLab keywords are case-insensitive, the lexer emits them as identifiers and
 * the parser/highlighter/completion classify them with this table.
 * <p>
 * Ported from JetBrains resharper-unity (src/Unity.Shaders/ShaderLab/Psi/Parsing/ShaderLabTokenType.Tokens.xml), Apache License 2.0.
 */
@NullMarked
public final class ShaderLabKeywords
{
    public enum Kind
    {
        /**
         * {@code CGPROGRAM}, {@code ENDCG}... - delimiters of embedded HLSL/GLSL program blocks
         */
        PROGRAM_DELIMITER,
        /**
         * Commands followed by a {@code { ... }} block: {@code Shader}, {@code SubShader}, {@code Pass}...
         */
        BLOCK_COMMAND,
        /**
         * Regular (single line) commands: {@code Cull}, {@code ZTest}, {@code Fallback}...
         */
        COMMAND,
        /**
         * Property types: {@code Float}, {@code Range}, {@code 2D}...
         */
        PROPERTY_TYPE,
        /**
         * Command arguments: {@code On}, {@code Off}, {@code Back}, {@code SrcAlpha}, {@code LEqual}...
         */
        ARGUMENT
    }

    public static final List<String> PROGRAM_DELIMITERS = List.of(
        "CGPROGRAM", "CGINCLUDE", "ENDCG",
        "GLSLPROGRAM", "GLSLINCLUDE", "ENDGLSL",
        "HLSLPROGRAM", "HLSLINCLUDE", "ENDHLSL"
    );

    public static final List<String> PREPROCESSOR = List.of("#warning", "#error", "#line");

    public static final List<String> BLOCK_COMMANDS = List.of(
        "Shader", "Properties", "Category", "SubShader", "Tags", "Stencil", "Material", "SetTexture", "Fog",
        "BindChannels", "GrabPass", "Pass", "PackageRequirements"
    );

    public static final List<String> SHADER_COMMANDS = List.of("Fallback", "CustomEditor", "Dependency");

    public static final List<String> SUB_SHADER_COMMANDS = List.of("UsePass");

    public static final List<String> RENDER_STATE_COMMANDS = List.of(
        "Cull", "ZClip", "ZTest", "ZWrite", "Conservative", "Offset", "Blend", "BlendOp", "AlphaToMask", "ColorMask",
        "Name", "LOD"
    );

    public static final List<String> LEGACY_FIXED_FUNCTION_COMMANDS = List.of("Lighting", "SeparateSpecular", "ColorMaterial", "AlphaTest");

    public static final List<String> LEGACY_MATERIAL_COMMANDS = List.of("Diffuse", "Ambient", "Specular", "Shininess", "Emission");

    public static final List<String> LEGACY_TEXTURE_COMMANDS = List.of("Combine", "ConstantColor", "Matrix", "LightmapMode", "TexGen");

    public static final List<String> LEGACY_BIND_CHANNELS_COMMANDS = List.of("Bind");

    public static final List<String> LEGACY_FOG_COMMANDS = List.of("Mode", "Density");

    /**
     * {@code Color} is both a command and a property type
     */
    public static final List<String> MULTIPURPOSE_COMMANDS = List.of("Color");

    public static final List<String> STENCIL_COMMANDS = List.of(
        "Ref", "ReadMask", "WriteMask", "Pass", "PassFront", "PassBack", "Comp", "CompFront", "CompBack", "Fail", "FailFront",
        "FailBack", "ZFail", "ZFailFront", "ZFailBack"
    );

    public static final List<String> PROPERTY_TYPES = List.of(
        "Float", "Int", "Integer", "Vector", "Color", "Range",
        "Any", "Cube", "CubeArray", "Rect", "2D", "2DArray", "3D"
    );

    public static final List<String> CULL_ORIENTATIONS = List.of("Back", "Front", "Off");

    public static final List<String> BLEND_FACTORS = List.of(
        "One", "Zero", "SrcColor", "SrcAlpha", "SrcAlphaSaturate", "DstColor", "DstAlpha", "OneMinusSrcColor", "OneMinusSrcAlpha",
        "OneMinusDstColor", "OneMinusDstAlpha"
    );

    public static final List<String> BLEND_OPERATIONS = List.of(
        "Add", "Sub", "RevSub", "Min", "Max",
        "LogicalClear", "LogicalSet", "LogicalCopy", "LogicalCopyInverted", "LogicalNoop", "LogicalInvert", "LogicalAnd",
        "LogicalNand", "LogicalOr", "LogicalNor", "LogicalXor", "LogicalEquiv", "LogicalAndReverse", "LogicalAndInverted",
        "LogicalOrReverse", "LogicalOrInverted",
        "Multiply", "Screen", "Overlay", "Darken", "Lighten", "ColorDodge", "ColorBurn", "HardLight", "SoftLight",
        "Difference", "Exclusion", "HSLHue", "HSLSaturation", "HSLColor", "HSLLuminosity"
    );

    public static final List<String> TEXTURE_COMBINERS = List.of(
        "Previous", "Primary", "Texture", "Constant", "Double", "Quad", "Lerp", "One", "Alpha", "Dot3", "Dot3Rgba"
    );

    public static final List<String> TEX_GEN_MODES = List.of("SphereMap", "EyeLinear", "CubeReflect", "CubeNormal", "ObjectLinear");

    public static final List<String> COLOR_MATERIAL_MODES = List.of("AmbientAndDiffuse", "Emission");

    public static final List<String> FOG_MODES = List.of("Off", "Global", "Linear", "Exp", "Exp2");

    public static final List<String> COMPARISON_FUNCTIONS = List.of("Greater", "GEqual", "Less", "LEqual", "Equal", "NotEqual", "Always", "Never");

    public static final List<String> STENCIL_OPERATIONS = List.of("Keep", "Zero", "Replace", "IncrSat", "DecrSat", "Invert", "IncrWrap", "DecrWrap");

    public static final List<String> BOOLEANS = List.of("True", "False", "On", "Off");

    public static final List<String> ON_OFF = List.of("On", "Off");

    private static final Map<String, Kind> ourKinds = buildKinds();

    private static final Map<String, List<String>> ourArguments = buildArguments();

    private ShaderLabKeywords()
    {
    }

    /**
     * @return the kind of the keyword (command kinds win over argument kinds, e.g. {@code Emission}), or {@code null}
     * when {@code text} is not a ShaderLab keyword. Case-insensitive
     */
    @Nullable
    public static Kind getKind(String text)
    {
        return ourKinds.get(text.toLowerCase(Locale.ROOT));
    }

    public static boolean isKeyword(String text)
    {
        return getKind(text) != null;
    }

    public static boolean isCommand(String text)
    {
        Kind kind = getKind(text);
        return kind == Kind.BLOCK_COMMAND || kind == Kind.COMMAND;
    }

    /**
     * @return all commands (block and regular), in table order without duplicates
     */
    public static List<String> getAllCommands()
    {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, Kind> entry : canonicalNames().entrySet())
        {
            if (entry.getValue() == Kind.BLOCK_COMMAND || entry.getValue() == Kind.COMMAND)
            {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /**
     * @return keyword arguments accepted by {@code command} (case-insensitive), empty when unknown or when the command takes
     * no keyword arguments
     */
    public static List<String> getArgumentKeywords(String command)
    {
        List<String> arguments = ourArguments.get(command.toLowerCase(Locale.ROOT));
        return arguments == null ? List.of() : arguments;
    }

    /**
     * @param blockCommand the enclosing block command ({@code Shader}, {@code SubShader}, {@code Pass}...), case-insensitive
     * @return commands which may appear directly inside that block, empty when unknown
     */
    public static List<String> getCommandsInBlock(String blockCommand)
    {
        List<String> result = new ArrayList<>();
        switch (blockCommand.toLowerCase(Locale.ROOT))
        {
            case "shader":
                result.addAll(List.of("Properties", "Category", "SubShader", "Fallback", "CustomEditor", "Dependency"));
                result.addAll(List.of("CGINCLUDE", "HLSLINCLUDE", "GLSLINCLUDE"));
                break;
            case "category":
            case "subshader":
                result.addAll(List.of("Tags", "Pass", "GrabPass", "UsePass", "PackageRequirements", "Stencil", "Fog", "Material"));
                result.addAll(RENDER_STATE_COMMANDS);
                result.addAll(LEGACY_FIXED_FUNCTION_COMMANDS);
                result.addAll(List.of("CGINCLUDE", "HLSLINCLUDE", "GLSLINCLUDE"));
                if (blockCommand.equalsIgnoreCase("category"))
                {
                    result.add("SubShader");
                }
                break;
            case "pass":
                result.addAll(List.of("Tags", "Stencil", "Material", "SetTexture", "Fog", "BindChannels", "PackageRequirements", "Color"));
                result.addAll(RENDER_STATE_COMMANDS);
                result.addAll(LEGACY_FIXED_FUNCTION_COMMANDS);
                result.addAll(List.of("CGPROGRAM", "HLSLPROGRAM", "GLSLPROGRAM", "CGINCLUDE", "HLSLINCLUDE", "GLSLINCLUDE"));
                break;
            case "grabpass":
                result.addAll(List.of("Name", "Tags"));
                break;
            case "stencil":
                result.addAll(STENCIL_COMMANDS);
                break;
            case "material":
                result.addAll(LEGACY_MATERIAL_COMMANDS);
                break;
            case "settexture":
                result.addAll(LEGACY_TEXTURE_COMMANDS);
                break;
            case "fog":
                result.addAll(List.of("Color", "Mode", "Density"));
                break;
            case "bindchannels":
                result.addAll(LEGACY_BIND_CHANNELS_COMMANDS);
                break;
            default:
                break;
        }
        return result;
    }

    private static Map<String, Kind> buildKinds()
    {
        Map<String, Kind> result = new HashMap<>();
        for (Map.Entry<String, Kind> entry : canonicalNames().entrySet())
        {
            result.put(entry.getKey().toLowerCase(Locale.ROOT), entry.getValue());
        }
        return Collections.unmodifiableMap(result);
    }

    private static Map<String, Kind> canonicalNames()
    {
        Map<String, Kind> result = new LinkedHashMap<>();
        // order matters - first registration wins
        putAll(result, PROGRAM_DELIMITERS, Kind.PROGRAM_DELIMITER);
        putAll(result, BLOCK_COMMANDS, Kind.BLOCK_COMMAND);
        putAll(result, SHADER_COMMANDS, Kind.COMMAND);
        putAll(result, SUB_SHADER_COMMANDS, Kind.COMMAND);
        putAll(result, RENDER_STATE_COMMANDS, Kind.COMMAND);
        putAll(result, LEGACY_FIXED_FUNCTION_COMMANDS, Kind.COMMAND);
        putAll(result, LEGACY_MATERIAL_COMMANDS, Kind.COMMAND);
        putAll(result, LEGACY_TEXTURE_COMMANDS, Kind.COMMAND);
        putAll(result, LEGACY_BIND_CHANNELS_COMMANDS, Kind.COMMAND);
        putAll(result, LEGACY_FOG_COMMANDS, Kind.COMMAND);
        putAll(result, MULTIPURPOSE_COMMANDS, Kind.COMMAND);
        putAll(result, STENCIL_COMMANDS, Kind.COMMAND);
        putAll(result, PROPERTY_TYPES, Kind.PROPERTY_TYPE);
        putAll(result, CULL_ORIENTATIONS, Kind.ARGUMENT);
        putAll(result, BLEND_FACTORS, Kind.ARGUMENT);
        putAll(result, BLEND_OPERATIONS, Kind.ARGUMENT);
        putAll(result, TEXTURE_COMBINERS, Kind.ARGUMENT);
        putAll(result, TEX_GEN_MODES, Kind.ARGUMENT);
        putAll(result, COLOR_MATERIAL_MODES, Kind.ARGUMENT);
        putAll(result, FOG_MODES, Kind.ARGUMENT);
        putAll(result, COMPARISON_FUNCTIONS, Kind.ARGUMENT);
        putAll(result, STENCIL_OPERATIONS, Kind.ARGUMENT);
        putAll(result, BOOLEANS, Kind.ARGUMENT);
        return result;
    }

    private static void putAll(Map<String, Kind> map, List<String> names, Kind kind)
    {
        for (String name : names)
        {
            map.putIfAbsent(name, kind);
        }
    }

    private static Map<String, List<String>> buildArguments()
    {
        Map<String, List<String>> result = new HashMap<>();
        put(result, CULL_ORIENTATIONS, "Cull");
        put(result, BOOLEANS, "ZClip", "Conservative");
        put(result, ON_OFF, "ZWrite", "AlphaToMask", "Lighting", "SeparateSpecular");
        put(result, COMPARISON_FUNCTIONS, "ZTest", "Comp", "CompFront", "CompBack");
        put(result, concat(List.of("Off"), BLEND_FACTORS), "Blend");
        put(result, BLEND_OPERATIONS, "BlendOp");
        put(result, concat(List.of("Off"), COMPARISON_FUNCTIONS), "AlphaTest");
        put(result, COLOR_MATERIAL_MODES, "ColorMaterial");
        put(result, FOG_MODES, "Mode");
        put(result, TEXTURE_COMBINERS, "Combine");
        put(result, TEX_GEN_MODES, "TexGen");
        put(result, STENCIL_OPERATIONS, "PassFront", "PassBack", "Fail", "FailFront", "FailBack", "ZFail", "ZFailFront", "ZFailBack");
        put(result, List.of("Off"), "Fallback");
        return Collections.unmodifiableMap(result);
    }

    private static void put(Map<String, List<String>> map, List<String> arguments, String... commands)
    {
        for (String command : commands)
        {
            map.put(command.toLowerCase(Locale.ROOT), arguments);
        }
    }

    private static List<String> concat(List<String> first, List<String> second)
    {
        List<String> result = new ArrayList<>(first);
        for (String value : second)
        {
            if (!result.contains(value))
            {
                result.add(value);
            }
        }
        return List.copyOf(result);
    }
}

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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads the line-based built-in table ({@code builtins.txt} next to this class) into a {@link GodotBuiltins}.
 * <p>
 * Format: {@code #} starts a comment line, blank lines are ignored. A section header
 * {@code [<kind> [<name>] @ <target> ...]} applies to the lines below it:
 * <ul>
 * <li>{@code functions}, targets {@code <shader_type>:<context>}: lines {@code <return type> <name>(<params>)},
 * parameters {@code [in|out|inout] <type> <name>[?]}, {@code ?} marking an optional parameter;</li>
 * <li>{@code variables}, targets {@code <shader_type>:<context>}: lines {@code <in|out|inout> <type> <NAME>};</li>
 * <li>{@code render_modes} and {@code stencil_modes}, targets shader types: one mode per line;</li>
 * <li>{@code hints}, targets uniform types: one hint per line.</li>
 * </ul>
 * A context is {@code common} (anywhere in the shader type) or a processing function name.
 * <p>
 * Built-in data derived from gdshader-support (https://github.com/akghxhs55/gdshader-support), MIT License, Copyright (c) 2025-2026 Jaeho Yi.
 */
final class GodotBuiltinsParser {
    private enum Section {
        FUNCTIONS,
        VARIABLES,
        RENDER_MODES,
        STENCIL_MODES,
        HINTS
    }

    private static final String COMMON_CONTEXT = "common";

    private final String mySourceName;
    private int myLineNumber;
    private @Nullable Section mySection;
    private List<GodotBuiltinContext> myContexts = List.of();
    private List<GodotShaderType> myShaderTypes = List.of();
    private List<GodotDataType> myHintTypes = List.of();

    private final List<GodotBuiltinFunction> myFunctions = new ArrayList<>();
    private final List<GodotBuiltinVariable> myVariables = new ArrayList<>();
    private final Map<GodotShaderType, List<String>> myRenderModes = new EnumMap<>(GodotShaderType.class);
    private final Map<GodotShaderType, List<String>> myStencilModes = new EnumMap<>(GodotShaderType.class);
    private final Map<GodotDataType, List<String>> myHints = new LinkedHashMap<>();

    private GodotBuiltinsParser(String sourceName) {
        mySourceName = sourceName;
    }

    static GodotBuiltins parse(BufferedReader reader, String sourceName) throws IOException {
        GodotBuiltinsParser parser = new GodotBuiltinsParser(sourceName);
        String line;
        while ((line = reader.readLine()) != null) {
            parser.myLineNumber++;
            parser.parseLine(line.strip());
        }
        return new GodotBuiltins(parser.myFunctions, parser.myVariables, parser.myRenderModes, parser.myStencilModes, parser.myHints);
    }

    private void parseLine(String line) {
        if (line.isEmpty() || line.startsWith("#")) {
            return;
        }
        if (line.startsWith("[")) {
            parseHeader(line);
            return;
        }
        Section section = mySection;
        if (section == null) {
            throw error("entry before the first section header");
        }
        switch (section) {
            case FUNCTIONS -> myFunctions.add(parseFunction(line));
            case VARIABLES -> myVariables.add(parseVariable(line));
            case RENDER_MODES -> addModes(myRenderModes, parseWord(line));
            case STENCIL_MODES -> addModes(myStencilModes, parseWord(line));
            case HINTS -> {
                String hint = parseWord(line);
                for (GodotDataType type : myHintTypes) {
                    myHints.computeIfAbsent(type, key -> new ArrayList<>()).add(hint);
                }
            }
        }
    }

    private void parseHeader(String line) {
        if (!line.endsWith("]")) {
            throw error("unterminated section header");
        }
        String inner = line.substring(1, line.length() - 1);
        int at = inner.indexOf('@');
        if (at < 0 || inner.indexOf('@', at + 1) >= 0) {
            throw error("section header needs exactly one '@'");
        }
        String[] head = split(inner.substring(0, at));
        String[] targets = split(inner.substring(at + 1));
        if (head.length < 1 || head.length > 2) {
            throw error("section header needs a kind and an optional name before '@'");
        }
        if (targets.length == 0) {
            throw error("section header needs at least one target after '@'");
        }

        Section section = switch (head[0]) {
            case "functions" -> Section.FUNCTIONS;
            case "variables" -> Section.VARIABLES;
            case "render_modes" -> Section.RENDER_MODES;
            case "stencil_modes" -> Section.STENCIL_MODES;
            case "hints" -> Section.HINTS;
            default -> throw error("unknown section kind '" + head[0] + "'");
        };
        mySection = section;

        List<GodotBuiltinContext> contexts = new ArrayList<>();
        List<GodotShaderType> shaderTypes = new ArrayList<>();
        List<GodotDataType> hintTypes = new ArrayList<>();
        for (String target : targets) {
            switch (section) {
                case FUNCTIONS, VARIABLES -> contexts.add(parseContext(target));
                case RENDER_MODES, STENCIL_MODES -> shaderTypes.add(parseShaderType(target));
                case HINTS -> hintTypes.add(parseType(target));
            }
        }
        myContexts = List.copyOf(contexts);
        myShaderTypes = List.copyOf(shaderTypes);
        myHintTypes = List.copyOf(hintTypes);
    }

    private GodotBuiltinContext parseContext(String text) {
        int colon = text.indexOf(':');
        if (colon < 0) {
            throw error("context '" + text + "' must be <shader_type>:<context>");
        }
        GodotShaderType shaderType = parseShaderType(text.substring(0, colon));
        String functionName = text.substring(colon + 1);
        if (functionName.equals(COMMON_CONTEXT)) {
            return new GodotBuiltinContext(shaderType, null);
        }
        GodotProcessingFunction function = GodotProcessingFunction.findByName(shaderType, functionName);
        if (function == null) {
            throw error("'" + functionName + "' is not a processing function of " + shaderType.getId());
        }
        return new GodotBuiltinContext(shaderType, function);
    }

    private GodotShaderType parseShaderType(String text) {
        GodotShaderType shaderType = GodotShaderType.findById(text);
        if (shaderType == null) {
            throw error("unknown shader type '" + text + "'");
        }
        return shaderType;
    }

    private GodotDataType parseType(String text) {
        GodotDataType type = GodotDataType.findByName(text);
        if (type == null) {
            throw error("unknown type '" + text + "'");
        }
        return type;
    }

    private GodotBuiltinFunction parseFunction(String line) {
        int open = line.indexOf('(');
        if (open < 0 || !line.endsWith(")")) {
            throw error("function must be '<return type> <name>(<parameters>)'");
        }
        String[] head = split(line.substring(0, open));
        if (head.length != 2) {
            throw error("function must be '<return type> <name>(<parameters>)'");
        }
        GodotDataType returnType = parseType(head[0]);
        List<GodotParameter> parameters = new ArrayList<>();
        String parameterList = line.substring(open + 1, line.length() - 1).strip();
        if (!parameterList.isEmpty()) {
            for (String parameterText : parameterList.split(",")) {
                parameters.add(parseParameter(parameterText));
            }
        }
        return new GodotBuiltinFunction(head[1], returnType, parameters, myContexts);
    }

    private GodotParameter parseParameter(String text) {
        String[] parts = split(text);
        if (parts.length != 2 && parts.length != 3) {
            throw error("parameter must be '[in|out|inout] <type> <name>[?]': '" + text.strip() + "'");
        }
        GodotParameterQualifier qualifier = GodotParameterQualifier.IN;
        if (parts.length == 3) {
            GodotParameterQualifier explicit = GodotParameterQualifier.findByText(parts[0]);
            if (explicit == null) {
                throw error("unknown parameter qualifier '" + parts[0] + "'");
            }
            qualifier = explicit;
        }
        GodotDataType type = parseType(parts[parts.length - 2]);
        String name = parts[parts.length - 1];
        boolean optional = name.endsWith("?");
        if (optional) {
            name = name.substring(0, name.length() - 1);
        }
        return new GodotParameter(name, type, qualifier, optional);
    }

    private GodotBuiltinVariable parseVariable(String line) {
        String[] parts = split(line);
        if (parts.length != 3) {
            throw error("variable must be '<in|out|inout> <type> <NAME>'");
        }
        GodotVariableAccess access = GodotVariableAccess.findByText(parts[0]);
        if (access == null) {
            throw error("unknown variable access '" + parts[0] + "'");
        }
        return new GodotBuiltinVariable(parts[2], parseType(parts[1]), access, myContexts);
    }

    private String parseWord(String line) {
        if (split(line).length != 1) {
            throw error("expected a single identifier");
        }
        return line;
    }

    private void addModes(Map<GodotShaderType, List<String>> target, String mode) {
        for (GodotShaderType shaderType : myShaderTypes) {
            target.computeIfAbsent(shaderType, key -> new ArrayList<>()).add(mode);
        }
    }

    private static String[] split(String text) {
        String stripped = text.strip();
        return stripped.isEmpty() ? new String[0] : stripped.split("\\s+");
    }

    private IllegalStateException error(String message) {
        return new IllegalStateException(mySourceName + ":" + myLineNumber + ": " + message);
    }
}

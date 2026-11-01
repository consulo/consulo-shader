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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Registry of the Godot shading language built-ins: functions, variables, constructors, render modes, stencil
 * modes and uniform hints. Loaded lazily, once, from the {@code builtins.txt} resource next to this class (format
 * in {@link GodotBuiltinsParser}); the instance is immutable and safe to share between threads.
 * <p>
 * Lookups take the shader type and the enclosing processing function, null meaning code outside the processing
 * functions (user functions, global initializers); built-ins of the shader type's {@code common} context are
 * visible everywhere in it.
 * <p>
 * Built-in data derived from gdshader-support (https://github.com/akghxhs55/gdshader-support), MIT License, Copyright (c) 2025-2026 Jaeho Yi.
 */
public final class GodotBuiltins {
    private static final String RESOURCE = "builtins.txt";

    private static final class Holder {
        private static final GodotBuiltins INSTANCE = load();
    }

    private final List<GodotBuiltinFunction> myFunctions;
    private final List<GodotBuiltinVariable> myVariables;
    private final Map<GodotBuiltinContext, Map<String, List<GodotBuiltinFunction>>> myFunctionsByContext = new HashMap<>();
    private final Map<GodotBuiltinContext, Map<String, GodotBuiltinVariable>> myVariablesByContext = new HashMap<>();
    private final Set<String> myFunctionNames;
    private final Set<String> myVariableNames;
    private final Map<GodotShaderType, List<String>> myRenderModes;
    private final Map<GodotShaderType, List<String>> myStencilModes;
    private final Map<GodotDataType, List<String>> myHints;
    private final Set<String> myAllHints;

    GodotBuiltins(List<GodotBuiltinFunction> functions,
                  List<GodotBuiltinVariable> variables,
                  Map<GodotShaderType, List<String>> renderModes,
                  Map<GodotShaderType, List<String>> stencilModes,
                  Map<GodotDataType, List<String>> hints) {
        myFunctions = List.copyOf(functions);
        myVariables = List.copyOf(variables);

        Set<String> functionNames = new TreeSet<>();
        for (GodotBuiltinFunction function : myFunctions) {
            functionNames.add(function.name());
            for (GodotBuiltinContext context : function.availability()) {
                myFunctionsByContext.computeIfAbsent(context, key -> new HashMap<>())
                    .computeIfAbsent(function.name(), key -> new ArrayList<>())
                    .add(function);
            }
        }
        myFunctionNames = Collections.unmodifiableSet(functionNames);

        Set<String> variableNames = new TreeSet<>();
        for (GodotBuiltinVariable variable : myVariables) {
            variableNames.add(variable.name());
            for (GodotBuiltinContext context : variable.availability()) {
                myVariablesByContext.computeIfAbsent(context, key -> new HashMap<>()).putIfAbsent(variable.name(), variable);
            }
        }
        myVariableNames = Collections.unmodifiableSet(variableNames);

        myRenderModes = copyLists(renderModes, new EnumMap<>(GodotShaderType.class));
        myStencilModes = copyLists(stencilModes, new EnumMap<>(GodotShaderType.class));
        myHints = copyLists(hints, new EnumMap<>(GodotDataType.class));

        Set<String> allHints = new LinkedHashSet<>();
        for (List<String> list : myHints.values()) {
            allHints.addAll(list);
        }
        myAllHints = Collections.unmodifiableSet(allHints);
    }

    public static GodotBuiltins getInstance() {
        return Holder.INSTANCE;
    }

    private static GodotBuiltins load() {
        InputStream stream = GodotBuiltins.class.getResourceAsStream(RESOURCE);
        if (stream == null) {
            throw new IllegalStateException("Missing resource " + RESOURCE + " next to " + GodotBuiltins.class.getName());
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            return GodotBuiltinsParser.parse(reader, RESOURCE);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static <K> Map<K, List<String>> copyLists(Map<K, List<String>> source, Map<K, List<String>> target) {
        for (Map.Entry<K, List<String>> entry : source.entrySet()) {
            target.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return Collections.unmodifiableMap(target);
    }

    // ---- functions ----

    /**
     * Every built-in function overload, in table order.
     */
    public List<GodotBuiltinFunction> getAllFunctions() {
        return myFunctions;
    }

    /**
     * Names of all built-in functions of any shader type, sorted.
     */
    public Set<String> getAllFunctionNames() {
        return myFunctionNames;
    }

    public boolean isFunctionName(String name) {
        return myFunctionNames.contains(name);
    }

    /**
     * The overloads of {@code name} callable in {@code processingFunction} (null: outside processing functions) of
     * a {@code shaderType} shader; empty when there is none.
     */
    public List<GodotBuiltinFunction> getFunctions(GodotShaderType shaderType, @Nullable GodotProcessingFunction processingFunction, String name) {
        List<GodotBuiltinFunction> result = new ArrayList<>();
        for (GodotBuiltinContext context : visibleContexts(shaderType, processingFunction)) {
            Map<String, List<GodotBuiltinFunction>> byName = myFunctionsByContext.get(context);
            List<GodotBuiltinFunction> overloads = byName == null ? null : byName.get(name);
            if (overloads != null) {
                result.addAll(overloads);
            }
        }
        return result;
    }

    /**
     * All overloads callable in {@code processingFunction} (null: outside processing functions) of a
     * {@code shaderType} shader.
     */
    public List<GodotBuiltinFunction> getFunctions(GodotShaderType shaderType, @Nullable GodotProcessingFunction processingFunction) {
        List<GodotBuiltinFunction> result = new ArrayList<>();
        for (GodotBuiltinContext context : visibleContexts(shaderType, processingFunction)) {
            Map<String, List<GodotBuiltinFunction>> byName = myFunctionsByContext.get(context);
            if (byName != null) {
                for (List<GodotBuiltinFunction> overloads : byName.values()) {
                    result.addAll(overloads);
                }
            }
        }
        return result;
    }

    /**
     * Names of the functions callable in {@code processingFunction} of a {@code shaderType} shader, for completion.
     */
    public Set<String> getFunctionNames(GodotShaderType shaderType, @Nullable GodotProcessingFunction processingFunction) {
        Set<String> result = new TreeSet<>();
        for (GodotBuiltinContext context : visibleContexts(shaderType, processingFunction)) {
            Map<String, List<GodotBuiltinFunction>> byName = myFunctionsByContext.get(context);
            if (byName != null) {
                result.addAll(byName.keySet());
            }
        }
        return result;
    }

    // ---- variables ----

    /**
     * Every built-in variable entry, in table order (a name may occur once per context).
     */
    public List<GodotBuiltinVariable> getAllVariables() {
        return myVariables;
    }

    /**
     * Names of all built-in variables of any shader type, sorted.
     */
    public Set<String> getAllVariableNames() {
        return myVariableNames;
    }

    public boolean isVariableName(String name) {
        return myVariableNames.contains(name);
    }

    /**
     * The built-in variable {@code name} as seen in {@code processingFunction} (null: outside processing
     * functions) of a {@code shaderType} shader; an entry of the processing function wins over a common one.
     */
    public @Nullable GodotBuiltinVariable getVariable(GodotShaderType shaderType, @Nullable GodotProcessingFunction processingFunction, String name) {
        List<GodotBuiltinContext> contexts = visibleContexts(shaderType, processingFunction);
        for (int i = contexts.size() - 1; i >= 0; i--) {
            Map<String, GodotBuiltinVariable> byName = myVariablesByContext.get(contexts.get(i));
            GodotBuiltinVariable variable = byName == null ? null : byName.get(name);
            if (variable != null) {
                return variable;
            }
        }
        return null;
    }

    /**
     * The built-in variables visible in {@code processingFunction} (null: outside processing functions) of a
     * {@code shaderType} shader, one per name.
     */
    public Collection<GodotBuiltinVariable> getVariables(GodotShaderType shaderType, @Nullable GodotProcessingFunction processingFunction) {
        Map<String, GodotBuiltinVariable> result = new LinkedHashMap<>();
        for (GodotBuiltinContext context : visibleContexts(shaderType, processingFunction)) {
            Map<String, GodotBuiltinVariable> byName = myVariablesByContext.get(context);
            if (byName != null) {
                result.putAll(byName);
            }
        }
        return result.values();
    }

    // ---- constructors, modes and hints ----

    /**
     * The constructors of a scalar, vector or matrix type; empty for other types.
     */
    public List<GodotConstructor> getConstructors(GodotDataType type) {
        return GodotConstructor.of(type);
    }

    /**
     * The {@code render_mode} values a {@code shaderType} shader accepts.
     */
    public List<String> getRenderModes(GodotShaderType shaderType) {
        List<String> modes = myRenderModes.get(shaderType);
        return modes == null ? List.of() : modes;
    }

    /**
     * The {@code stencil_mode} values a {@code shaderType} shader accepts (empty when it has no stencil modes).
     */
    public List<String> getStencilModes(GodotShaderType shaderType) {
        List<String> modes = myStencilModes.get(shaderType);
        return modes == null ? List.of() : modes;
    }

    /**
     * The hints a uniform of {@code type} accepts after {@code :}; empty for types without hints.
     */
    public List<String> getUniformHints(GodotDataType type) {
        List<String> hints = myHints.get(type);
        return hints == null ? List.of() : hints;
    }

    /**
     * The uniform types that have hints, with their hints.
     */
    public Map<GodotDataType, List<String>> getUniformHintsByType() {
        return myHints;
    }

    /**
     * Every uniform hint of any type.
     */
    public Set<String> getAllUniformHints() {
        return myAllHints;
    }

    /**
     * Common context first, then the processing function's own context, so later entries override earlier ones.
     */
    private static List<GodotBuiltinContext> visibleContexts(GodotShaderType shaderType, @Nullable GodotProcessingFunction processingFunction) {
        GodotBuiltinContext common = new GodotBuiltinContext(shaderType, null);
        if (processingFunction == null || !processingFunction.isAllowedIn(shaderType)) {
            return List.of(common);
        }
        return List.of(common, new GodotBuiltinContext(shaderType, processingFunction));
    }
}

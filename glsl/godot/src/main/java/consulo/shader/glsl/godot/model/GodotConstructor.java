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

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * One overload of the constructor of a built-in scalar, vector or matrix type, e.g. {@code vec4(vec2 xy, vec2 zw)}.
 * {@link #of(GodotDataType)} lists them per type.
 * <p>
 * Built-in data derived from gdshader-support (https://github.com/akghxhs55/gdshader-support), MIT License, Copyright (c) 2025-2026 Jaeho Yi.
 */
public record GodotConstructor(GodotDataType type, List<GodotParameter> parameters) {
    private static final String COMPONENTS = "xyzw";

    /**
     * How a vector of size 2, 3 or 4 may be assembled from smaller pieces (sizes of the consecutive arguments).
     */
    private static final int[][][] SPLITS = {
        {},
        {},
        {{1, 1}},
        {{1, 1, 1}, {2, 1}, {1, 2}},
        {{1, 1, 1, 1}, {2, 1, 1}, {1, 2, 1}, {1, 1, 2}, {2, 2}, {3, 1}, {1, 3}}
    };

    private static final List<GodotDataType> SCALARS = List.of(GodotDataType.FLOAT, GodotDataType.INT, GodotDataType.UINT, GodotDataType.BOOL);

    private static final Map<GodotDataType, List<GodotConstructor>> CONSTRUCTORS = buildAll();

    public GodotConstructor {
        parameters = List.copyOf(parameters);
    }

    /**
     * The constructors of {@code type}; empty for types without constructors (void, samplers, generic types).
     */
    public static List<GodotConstructor> of(GodotDataType type) {
        List<GodotConstructor> constructors = CONSTRUCTORS.get(type);
        return constructors == null ? List.of() : constructors;
    }

    private static Map<GodotDataType, List<GodotConstructor>> buildAll() {
        Map<GodotDataType, List<GodotConstructor>> result = new EnumMap<>(GodotDataType.class);
        for (GodotDataType type : GodotDataType.values()) {
            List<GodotConstructor> constructors = switch (type.getKind()) {
                case SCALAR -> buildScalar(type);
                case VECTOR -> buildVector(type);
                case MATRIX -> buildMatrix(type);
                default -> List.of();
            };
            if (!constructors.isEmpty()) {
                result.put(type, Collections.unmodifiableList(constructors));
            }
        }
        return result;
    }

    private static List<GodotConstructor> buildScalar(GodotDataType type) {
        List<GodotConstructor> result = new ArrayList<>();
        for (GodotDataType source : SCALARS) {
            result.add(new GodotConstructor(type, List.of(new GodotParameter("x", source))));
        }
        return result;
    }

    private static List<GodotConstructor> buildVector(GodotDataType type) {
        GodotDataType component = type.getComponentType();
        if (component == null) {
            return List.of();
        }
        int size = type.getSize();
        List<GodotConstructor> result = new ArrayList<>();
        result.add(new GodotConstructor(type, List.of(new GodotParameter("x", component))));
        for (int[] split : SPLITS[size]) {
            List<GodotParameter> parameters = new ArrayList<>();
            int offset = 0;
            for (int part : split) {
                GodotDataType partType = GodotDataType.vectorOf(component, part);
                if (partType == null) {
                    throw new IllegalStateException("No " + part + "-component vector of " + component.getName());
                }
                parameters.add(new GodotParameter(COMPONENTS.substring(offset, offset + part), partType));
                offset += part;
            }
            result.add(new GodotConstructor(type, parameters));
        }
        for (GodotDataType sourceComponent : SCALARS) {
            GodotDataType source = GodotDataType.vectorOf(sourceComponent, size);
            if (source != null) {
                result.add(new GodotConstructor(type, List.of(new GodotParameter("from", source))));
            }
        }
        return result;
    }

    private static List<GodotConstructor> buildMatrix(GodotDataType type) {
        List<GodotConstructor> result = new ArrayList<>();
        result.add(new GodotConstructor(type, List.of(new GodotParameter("scalar", GodotDataType.FLOAT))));
        for (GodotDataType other : List.of(GodotDataType.MAT2, GodotDataType.MAT3, GodotDataType.MAT4)) {
            if (other != type) {
                result.add(new GodotConstructor(type, List.of(new GodotParameter("from", other))));
            }
        }
        GodotDataType column = GodotDataType.vectorOf(GodotDataType.FLOAT, type.getSize());
        if (column != null) {
            List<GodotParameter> columns = new ArrayList<>();
            for (int i = 0; i < type.getSize(); i++) {
                columns.add(new GodotParameter("col" + i, column));
            }
            result.add(new GodotConstructor(type, columns));
        }
        return result;
    }
}

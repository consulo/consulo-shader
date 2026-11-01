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

import java.util.List;

/**
 * A built-in Godot shading language type, plus the generic placeholder types ({@code vec_type},
 * {@code gsampler2D}, ...) built-in signatures are written with; each generic type stands for the concrete types
 * of {@link #getVariants()}, chosen per call.
 * <p>
 * Built-in data derived from gdshader-support (https://github.com/akghxhs55/gdshader-support), MIT License, Copyright (c) 2025-2026 Jaeho Yi.
 */
public enum GodotDataType {
    VOID("void", Kind.VOID, 0),
    BOOL("bool", Kind.SCALAR, 1),
    BVEC2("bvec2", Kind.VECTOR, 2),
    BVEC3("bvec3", Kind.VECTOR, 3),
    BVEC4("bvec4", Kind.VECTOR, 4),
    INT("int", Kind.SCALAR, 1),
    IVEC2("ivec2", Kind.VECTOR, 2),
    IVEC3("ivec3", Kind.VECTOR, 3),
    IVEC4("ivec4", Kind.VECTOR, 4),
    UINT("uint", Kind.SCALAR, 1),
    UVEC2("uvec2", Kind.VECTOR, 2),
    UVEC3("uvec3", Kind.VECTOR, 3),
    UVEC4("uvec4", Kind.VECTOR, 4),
    FLOAT("float", Kind.SCALAR, 1),
    VEC2("vec2", Kind.VECTOR, 2),
    VEC3("vec3", Kind.VECTOR, 3),
    VEC4("vec4", Kind.VECTOR, 4),
    MAT2("mat2", Kind.MATRIX, 2),
    MAT3("mat3", Kind.MATRIX, 3),
    MAT4("mat4", Kind.MATRIX, 4),
    SAMPLER2D("sampler2D", Kind.SAMPLER, 0),
    ISAMPLER2D("isampler2D", Kind.SAMPLER, 0),
    USAMPLER2D("usampler2D", Kind.SAMPLER, 0),
    SAMPLER2DARRAY("sampler2DArray", Kind.SAMPLER, 0),
    ISAMPLER2DARRAY("isampler2DArray", Kind.SAMPLER, 0),
    USAMPLER2DARRAY("usampler2DArray", Kind.SAMPLER, 0),
    SAMPLER3D("sampler3D", Kind.SAMPLER, 0),
    ISAMPLER3D("isampler3D", Kind.SAMPLER, 0),
    USAMPLER3D("usampler3D", Kind.SAMPLER, 0),
    SAMPLERCUBE("samplerCube", Kind.SAMPLER, 0),
    SAMPLERCUBEARRAY("samplerCubeArray", Kind.SAMPLER, 0),
    SAMPLEREXTERNALOES("samplerExternalOES", Kind.SAMPLER, 0),
    VEC_TYPE("vec_type", Kind.GENERIC, 0),
    VEC_INT_TYPE("vec_int_type", Kind.GENERIC, 0),
    VEC_UINT_TYPE("vec_uint_type", Kind.GENERIC, 0),
    VEC_BOOL_TYPE("vec_bool_type", Kind.GENERIC, 0),
    MAT_TYPE("mat_type", Kind.GENERIC, 0),
    GVEC4_TYPE("gvec4_type", Kind.GENERIC, 0),
    GSAMPLER2D("gsampler2D", Kind.GENERIC, 0),
    GSAMPLER2DARRAY("gsampler2DArray", Kind.GENERIC, 0),
    GSAMPLER3D("gsampler3D", Kind.GENERIC, 0);

    /**
     * The category of a {@link GodotDataType}.
     */
    public enum Kind {
        VOID,
        SCALAR,
        VECTOR,
        MATRIX,
        SAMPLER,
        GENERIC
    }

    private final String myName;
    private final Kind myKind;
    private final int mySize;

    GodotDataType(String name, Kind kind, int size) {
        myName = name;
        myKind = kind;
        mySize = size;
    }

    /**
     * The type name as written in the shader (or in built-in signatures, for generic types).
     */
    public String getName() {
        return myName;
    }

    public Kind getKind() {
        return myKind;
    }

    /**
     * Component count of a vector, column count of a matrix, 1 for a scalar, 0 otherwise.
     */
    public int getSize() {
        return mySize;
    }

    public boolean isGeneric() {
        return myKind == Kind.GENERIC;
    }

    /**
     * Whether a variable or uniform may be declared with this type ({@code void} and generic types may not).
     */
    public boolean isDeclarable() {
        return myKind != Kind.VOID && myKind != Kind.GENERIC;
    }

    /**
     * The scalar type of a scalar, vector or matrix; null for other kinds.
     */
    public @Nullable GodotDataType getComponentType() {
        return switch (this) {
            case BOOL, BVEC2, BVEC3, BVEC4 -> BOOL;
            case INT, IVEC2, IVEC3, IVEC4 -> INT;
            case UINT, UVEC2, UVEC3, UVEC4 -> UINT;
            case FLOAT, VEC2, VEC3, VEC4, MAT2, MAT3, MAT4 -> FLOAT;
            default -> null;
        };
    }

    /**
     * The concrete types a generic type stands for; a concrete type stands only for itself.
     */
    public List<GodotDataType> getVariants() {
        return switch (this) {
            case VEC_TYPE -> List.of(FLOAT, VEC2, VEC3, VEC4);
            case VEC_INT_TYPE -> List.of(INT, IVEC2, IVEC3, IVEC4);
            case VEC_UINT_TYPE -> List.of(UINT, UVEC2, UVEC3, UVEC4);
            case VEC_BOOL_TYPE -> List.of(BOOL, BVEC2, BVEC3, BVEC4);
            case MAT_TYPE -> List.of(MAT2, MAT3, MAT4);
            case GVEC4_TYPE -> List.of(VEC4, IVEC4, UVEC4);
            case GSAMPLER2D -> List.of(SAMPLER2D, ISAMPLER2D, USAMPLER2D);
            case GSAMPLER2DARRAY -> List.of(SAMPLER2DARRAY, ISAMPLER2DARRAY, USAMPLER2DARRAY);
            case GSAMPLER3D -> List.of(SAMPLER3D, ISAMPLER3D, USAMPLER3D);
            default -> List.of(this);
        };
    }

    /**
     * The vector (or, for size 1, scalar) type with {@code size} components of {@code componentType}; null when
     * there is none.
     */
    public static @Nullable GodotDataType vectorOf(GodotDataType componentType, int size) {
        for (GodotDataType type : values()) {
            if ((type.myKind == Kind.SCALAR || type.myKind == Kind.VECTOR) && type.mySize == size && type.getComponentType() == componentType) {
                return type;
            }
        }
        return null;
    }

    public static @Nullable GodotDataType findByName(String name) {
        for (GodotDataType type : values()) {
            if (type.myName.equals(name)) {
                return type;
            }
        }
        return null;
    }
}

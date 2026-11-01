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
package consulo.shader.glsl.impl.godot;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.psi.PsiElement;
import consulo.project.Project;
import consulo.shader.glsl.godot.model.GodotBuiltinFunction;
import consulo.shader.glsl.godot.model.GodotBuiltinVariable;
import consulo.shader.glsl.godot.model.GodotBuiltins;
import consulo.shader.glsl.godot.model.GodotDataType;
import consulo.shader.glsl.godot.model.GodotParameter;
import consulo.shader.glsl.godot.model.GodotParameterQualifier;
import consulo.shader.glsl.GlslLanguage;
import consulo.shader.impl.builtins.GeneratedDeclarationsFile;
import consulo.util.dataholder.Key;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The Godot built-ins as a read-only Godot shader, generated from the built-in model: one declaration per variable and
 * one prototype per function overload. It holds signatures only.
 */
public final class GodotBuiltinsFile {
    public static final String FILE_NAME = "godot_builtins.gdshader";

    private static final Key<GeneratedDeclarationsFile> KEY = Key.create("GodotBuiltinsFile");

    private GodotBuiltinsFile() {
    }

    @RequiredReadAction
    public static GeneratedDeclarationsFile getInstance(Project project) {
        GeneratedDeclarationsFile file = project.getUserData(KEY);
        if (file == null) {
            file = project.putUserDataIfAbsent(KEY, new GeneratedDeclarationsFile(project, GlslLanguage.INSTANCE, FILE_NAME, buildText()));
        }
        return file;
    }

    @RequiredReadAction
    public static boolean isBuiltin(PsiElement element) {
        return GeneratedDeclarationsFile.isBuiltin(element);
    }

    private static String buildText() {
        GodotBuiltins builtins = GodotBuiltins.getInstance();
        StringBuilder text = new StringBuilder("// Godot shading language built-ins, generated from their signatures.\n\n");
        Set<String> variables = new LinkedHashSet<>();
        for (GodotBuiltinVariable variable : builtins.getAllVariables()) {
            if (variables.add(variable.name())) {
                text.append(variable.type().getName()).append(" ").append(variable.name()).append(";\n");
            }
        }
        text.append("\n");
        Set<String> functions = new LinkedHashSet<>();
        for (GodotBuiltinFunction function : builtins.getAllFunctions()) {
            functions.addAll(concreteSignatures(function));
        }
        for (String function : functions) {
            text.append(function).append(";\n");
        }
        return text.toString();
    }

    /**
     * The concrete overloads a built-in signature stands for, so that overload resolution can match argument types and
     * counts: generic types ({@code vec_type}, {@code gsampler2D}, {@code gvec4_type}) vary together, so
     * {@code gvec4_type texture(gsampler2D s, vec2 p, float bias?)} is {@code vec4 texture(sampler2D s, vec2 p)},
     * {@code ivec4 texture(isampler2D s, vec2 p)}, ..., each also with the optional {@code bias}.
     */
    static List<String> concreteSignatures(GodotBuiltinFunction function) {
        int variants = 1;
        for (GodotDataType type : typesOf(function)) {
            if (type.isGeneric()) {
                variants = Math.max(variants, type.getVariants().size());
            }
        }
        List<String> signatures = new ArrayList<>();
        for (int variant = 0; variant < variants; variant++) {
            for (int count = function.getRequiredParameterCount(); count <= function.parameters().size(); count++) {
                StringBuilder signature = new StringBuilder();
                signature.append(concrete(function.returnType(), variant, variants)).append(' ').append(function.name()).append('(');
                for (int i = 0; i < count; i++) {
                    GodotParameter parameter = function.parameters().get(i);
                    if (i > 0) {
                        signature.append(", ");
                    }
                    if (parameter.qualifier() != GodotParameterQualifier.IN) {
                        signature.append(parameter.qualifier().getText()).append(' ');
                    }
                    signature.append(concrete(parameter.type(), variant, variants)).append(' ').append(parameter.name());
                }
                signatures.add(signature.append(')').toString());
            }
        }
        return signatures;
    }

    private static List<GodotDataType> typesOf(GodotBuiltinFunction function) {
        List<GodotDataType> types = new ArrayList<>();
        types.add(function.returnType());
        for (GodotParameter parameter : function.parameters()) {
            types.add(parameter.type());
        }
        return types;
    }

    // a generic type whose variants do not line up with the others keeps its generic name
    private static String concrete(GodotDataType type, int variant, int variants) {
        List<GodotDataType> concrete = type.getVariants();
        return type.isGeneric() && concrete.size() == variants ? concrete.get(variant).getName() : type.getName();
    }
}

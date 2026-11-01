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
package consulo.shader.glsl.impl.builtins;

import consulo.annotation.access.RequiredReadAction;
import consulo.project.Project;
import consulo.shader.ShaderStage;
import consulo.shader.glsl.GlslLanguage;
import consulo.shader.impl.builtins.GeneratedDeclarationsFile;
import consulo.util.dataholder.Key;
import org.jspecify.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The built-in variables and functions of GLSL, loaded from {@code glsl-builtins.txt}, which tools/glsl-builtins
 * generates from the built-in symbol table of glslang (3-Clause BSD License, see NOTICE.txt). Core symbols only.
 */
public final class GlslBuiltins {
    public static final String FILE_NAME = "glsl_builtins.glsl";

    private static final String RESOURCE = "/consulo/shader/glsl/impl/builtins/glsl-builtins.txt";
    private static final Key<GeneratedDeclarationsFile> FILE_KEY = Key.create("GlslBuiltinsFile");

    private static volatile @Nullable List<GlslBuiltin> ourBuiltins;

    private GlslBuiltins() {
    }

    public static List<GlslBuiltin> getAll() {
        List<GlslBuiltin> builtins = ourBuiltins;
        if (builtins == null) {
            builtins = List.copyOf(load());
            ourBuiltins = builtins;
        }
        return builtins;
    }

    /**
     * The generated read-only file of the built-in declarations, one per project.
     */
    @RequiredReadAction
    public static GeneratedDeclarationsFile getFile(Project project) {
        GeneratedDeclarationsFile file = project.getUserData(FILE_KEY);
        if (file == null) {
            file = project.putUserDataIfAbsent(FILE_KEY, new GeneratedDeclarationsFile(project, GlslLanguage.INSTANCE, FILE_NAME, buildText()));
        }
        return file;
    }

    /**
     * The stage a GLSL file is for, by the extension glslang uses, or {@code null} for a file of any stage.
     */
    public static @Nullable ShaderStage stageOf(@Nullable String extension) {
        if (extension == null) {
            return null;
        }
        return switch (extension) {
            case "vert", "vsh" -> ShaderStage.VERTEX;
            case "tesc" -> ShaderStage.TESSELLATION_CONTROL;
            case "tese" -> ShaderStage.TESSELLATION_EVALUATION;
            case "geom", "gsh" -> ShaderStage.GEOMETRY;
            case "frag", "fsh" -> ShaderStage.FRAGMENT;
            case "comp" -> ShaderStage.COMPUTE;
            default -> null;
        };
    }

    private static String buildText() {
        StringBuilder text = new StringBuilder("// GLSL built-ins, generated from the built-in symbol table of glslang.\n\n");
        // the members of gl_in[] and gl_out[], which glslang lists only as a block
        text.append("struct gl_PerVertex { vec4 gl_Position; float gl_PointSize; float gl_ClipDistance[]; float gl_CullDistance[]; };\n\n");
        Set<String> declarations = new LinkedHashSet<>();
        for (GlslBuiltin builtin : getAll()) {
            if (declarations.add(builtin.declaration())) {
                text.append(builtin.declaration()).append(";\n");
            }
        }
        return text.toString();
    }

    private static List<GlslBuiltin> load() {
        List<GlslBuiltin> builtins = new ArrayList<>();
        try (InputStream stream = GlslBuiltins.class.getResourceAsStream(RESOURCE)) {
            if (stream == null) {
                throw new IllegalStateException("Missing " + RESOURCE);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split(" \\| ");
                if (parts.length != 3) {
                    throw new IllegalStateException(RESOURCE + ": malformed line: " + line);
                }
                boolean function = parts[0].equals("function");
                String declaration = parts[1];
                builtins.add(new GlslBuiltin(function, nameOf(declaration, function), declaration, parseStages(parts[2])));
            }
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return builtins;
    }

    private static String nameOf(String declaration, boolean function) {
        String head = function ? declaration.substring(0, declaration.indexOf('(')) : declaration;
        return head.substring(head.lastIndexOf(' ') + 1);
    }

    private static Map<String, Set<ShaderStage>> parseStages(String text) {
        Map<String, Set<ShaderStage>> stages = new HashMap<>();
        for (String targetStages : text.trim().split(" ")) {
            int colon = targetStages.indexOf(':');
            Set<ShaderStage> set = EnumSet.noneOf(ShaderStage.class);
            for (String stage : targetStages.substring(colon + 1).split(",")) {
                ShaderStage shaderStage = stageOf(stage);
                if (shaderStage != null) {
                    set.add(shaderStage);
                }
            }
            stages.put(targetStages.substring(0, colon), set);
        }
        return stages;
    }
}

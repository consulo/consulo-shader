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
package consulo.shader.hlsl.impl.builtins;

import consulo.annotation.access.RequiredReadAction;
import consulo.project.Project;
import consulo.shader.hlsl.HlslLanguage;
import consulo.shader.impl.builtins.GeneratedDeclarationsFile;
import consulo.util.dataholder.Key;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * The intrinsic functions and built-in structs of HLSL, from {@code builtins.hlsl} of Rider-HLSL-Plugin-Continue (MIT
 * License, see NOTICE.txt) and our own {@code supplement.hlsl} (more intrinsics, the Cg texture functions). Of these the
 * generated declarations take the classes of built-in types ({@code Texture2D} with its methods, whose members member
 * access resolves into), the structs ({@code RayDesc}) and the free functions, generic ones over a template parameter
 * {@code T}; attribute signatures and variadic functions ({@code printf}) are left out.
 */
public final class HlslBuiltins {
    public static final String FILE_NAME = "hlsl_builtins.hlsl";

    private static final String RESOURCE = "/consulo/shader/hlsl/impl/builtins/builtins.hlsl";
    private static final String SUPPLEMENT = "/consulo/shader/hlsl/impl/builtins/supplement.hlsl";
    private static final Key<GeneratedDeclarationsFile> FILE_KEY = Key.create("HlslBuiltinsFile");

    private static final Pattern FUNCTION = Pattern.compile("^\\S.*?\\s+[A-Za-z_]\\w*\\s*\\([^)]*\\)\\s*;$");
    private static final Pattern STRUCT = Pattern.compile("^(?:struct|class)\\s+[A-Za-z_]\\w*");
    private static final Pattern GENERIC = Pattern.compile("\\bT\\b");

    private static volatile @Nullable String ourText;
    private static final Set<String> ourVariadic = ConcurrentHashMap.newKeySet();

    private HlslBuiltins() {
    }

    /**
     * The generated read-only file of the built-in declarations, one per project.
     */
    @RequiredReadAction
    public static GeneratedDeclarationsFile getFile(Project project) {
        GeneratedDeclarationsFile file = project.getUserData(FILE_KEY);
        if (file == null) {
            file = project.putUserDataIfAbsent(FILE_KEY, new GeneratedDeclarationsFile(project, HlslLanguage.INSTANCE, FILE_NAME, getText()));
        }
        return file;
    }

    /**
     * The text of the generated file.
     */
    public static String getText() {
        String text = ourText;
        if (text == null) {
            text = buildText(load(RESOURCE) + "\n" + load(SUPPLEMENT));
            ourText = text;
        }
        return text;
    }

    /**
     * The variadic intrinsics ({@code printf}), which the generated file cannot declare.
     */
    public static boolean isVariadic(String name) {
        getText();
        return ourVariadic.contains(name);
    }

    static String buildText(String source) {
        StringBuilder text = new StringBuilder("// HLSL intrinsics, generated from builtins.hlsl of Rider-HLSL-Plugin-Continue (MIT License) and supplement.hlsl.\n\n");
        int depth = 0;
        String template = null;
        for (String rawLine : source.split("\n")) {
            String line = rawLine.strip();
            if (depth == 0) {
                if (line.startsWith("template") && line.indexOf('(') < 0) {
                    // the parameters of the class that follows
                    template = line;
                    continue;
                }
                if (STRUCT.matcher(line).find()) {
                    if (template != null) {
                        text.append(template).append('\n');
                    }
                    template = null;
                }
                else {
                    template = null;
                    if (line.indexOf('{') < 0) {
                        if (FUNCTION.matcher(line).matches()) {
                            if (line.contains("...")) {
                                // the grammar has no variadic parameters: printf is known, not declared
                                ourVariadic.add(line.substring(0, line.indexOf('(')).replaceAll(".*\\s", ""));
                            }
                            else {
                                text.append(GENERIC.matcher(line).find() ? "template<typename T> " : "").append(line).append('\n');
                            }
                        }
                        continue;
                    }
                }
            }
            text.append(rawLine).append('\n');
            depth += count(line, '{') - count(line, '}');
        }
        return text.toString();
    }

    private static int count(String line, char c) {
        return (int) line.chars().filter(ch -> ch == c).count();
    }

    private static String load(String resource) {
        try (InputStream stream = HlslBuiltins.class.getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IllegalStateException("Missing " + resource);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8).replace("\r\n", "\n");
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

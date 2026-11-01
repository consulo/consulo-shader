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
package consulo.shader.hlsl.unreal;

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * The virtual shader directories of an Unreal engine and project ({@code /Engine/Private/Common.ush},
 * {@code /Plugin/FX/Niagara/...}, a project's own {@code /CubeCollector/...}), as the engine registers them with
 * {@code AddShaderSourceDirectoryMapping}. Those calls happen in C++ at run time, so the mappings are recovered from, in
 * order of trust:
 * <ol>
 *     <li>{@code FileSystemMappings.ini}, which the RiderLink editor plugin writes from the running editor</li>
 *     <li>the mappings the engine makes itself: {@code /Engine}, {@code /Project}, {@code /ShaderAutogen}</li>
 *     <li>the {@code AddShaderSourceDirectoryMapping(TEXT("/X"), ...)} calls in the C++ sources of engine and project
 *     modules and plugins, each mapped to the {@code Shaders} directory nearest to the source file</li>
 *     <li>{@code /Plugin/<Name>} for every plugin with a {@code Shaders} directory that no call maps</li>
 * </ol>
 * Works on plain paths so it can run outside the IDE.
 */
public final class UnrealShaderMappings {
    public enum Source {
        RIDER_LINK,
        ENGINE,
        SOURCE_CODE,
        PLUGIN_NAME
    }

    public record Mapping(String virtualPath, Path directory, Source source) {
    }

    /**
     * Virtual files the shader compiler generates per material and vertex factory; they never exist on disk.
     */
    public static final String GENERATED_PREFIX = "/Engine/Generated/";

    private static final Pattern MAPPING_CALL = Pattern.compile("AddShaderSourceDirectoryMapping\\s*\\(\\s*TEXT\\s*\\(\\s*\"(/[^\"]+)\"");
    private static final String MAPPING_FUNCTION = "AddShaderSourceDirectoryMapping";
    private static final String SHADERS = "Shaders";
    private static final Set<String> SKIPPED_DIRECTORIES = Set.of("Intermediate", "Binaries", "Saved", "DerivedDataCache", "Content",
        "ThirdParty", ".git");

    private final List<Mapping> myMappings;

    private UnrealShaderMappings(List<Mapping> mappings) {
        List<Mapping> sorted = new ArrayList<>(mappings);
        sorted.sort(Comparator.comparingInt((Mapping mapping) -> mapping.virtualPath().length()).reversed());
        myMappings = List.copyOf(sorted);
    }

    public static boolean isGenerated(String virtualPath) {
        return virtualPath.startsWith(GENERATED_PREFIX);
    }

    /**
     * Every mapping, longest virtual path first.
     */
    public List<Mapping> getMappings() {
        return myMappings;
    }

    /**
     * The file a virtual path names, or {@code null} when no mapping covers it or the file does not exist.
     */
    public @Nullable Path resolve(String virtualPath) {
        for (Mapping mapping : myMappings) {
            String prefix = mapping.virtualPath();
            if (virtualPath.startsWith(prefix) && virtualPath.length() > prefix.length() && virtualPath.charAt(prefix.length()) == '/') {
                Path file = mapping.directory().resolve(virtualPath.substring(prefix.length() + 1));
                if (Files.isRegularFile(file)) {
                    return file;
                }
            }
        }
        return null;
    }

    public static UnrealShaderMappings discover(@Nullable Path engineRoot, @Nullable Path projectDirectory) {
        Map<String, Mapping> mappings = new LinkedHashMap<>();

        for (Path root : nonNull(projectDirectory, engineRoot == null ? null : engineRoot.resolve("Engine"))) {
            for (Path ini : findRiderLinkMappings(root.resolve("Plugins"))) {
                readRiderLinkMappings(ini, mappings);
            }
        }

        if (engineRoot != null) {
            add(mappings, "/Engine", engineRoot.resolve("Engine/Shaders"), Source.ENGINE);
        }
        if (projectDirectory != null) {
            add(mappings, "/Project", projectDirectory.resolve(SHADERS), Source.ENGINE);
            add(mappings, "/ShaderAutogen", projectDirectory.resolve("Intermediate/ShaderAutogen"), Source.ENGINE);
        }

        List<Path> codeRoots = new ArrayList<>();
        if (engineRoot != null) {
            codeRoots.add(engineRoot.resolve("Engine/Plugins"));
            codeRoots.add(engineRoot.resolve("Engine/Source"));
        }
        if (projectDirectory != null) {
            codeRoots.add(projectDirectory.resolve("Plugins"));
            codeRoots.add(projectDirectory.resolve("Source"));
        }
        for (Path root : codeRoots) {
            scanSourceCode(root, projectDirectory, mappings);
        }

        for (Path root : codeRoots) {
            if (root.getFileName().toString().equals("Plugins")) {
                addPluginNames(root, mappings);
            }
        }
        return new UnrealShaderMappings(new ArrayList<>(mappings.values()));
    }

    private static List<Path> nonNull(@Nullable Path first, @Nullable Path second) {
        List<Path> paths = new ArrayList<>(2);
        if (first != null) {
            paths.add(first);
        }
        if (second != null) {
            paths.add(second);
        }
        return paths;
    }

    private static void add(Map<String, Mapping> mappings, String virtualPath, Path directory, Source source) {
        String normalized = virtualPath.endsWith("/") ? virtualPath.substring(0, virtualPath.length() - 1) : virtualPath;
        if (!mappings.containsKey(normalized) && Files.isDirectory(directory)) {
            mappings.put(normalized, new Mapping(normalized, directory, source));
        }
    }

    private static List<Path> findRiderLinkMappings(Path plugins) {
        if (!Files.isDirectory(plugins)) {
            return List.of();
        }
        try (Stream<Path> files = Files.walk(plugins, 4)) {
            return files.filter(path -> path.getFileName().toString().equals("RiderLink"))
                .map(path -> path.resolve("Intermediate/FileSystemMappings.ini"))
                .filter(Files::isRegularFile)
                .toList();
        }
        catch (IOException | UncheckedIOException e) {
            return List.of();
        }
    }

    private static void readRiderLinkMappings(Path ini, Map<String, Mapping> mappings) {
        try {
            for (String rawLine : Files.readAllLines(ini, StandardCharsets.UTF_8)) {
                String line = rawLine.strip();
                int equals = line.indexOf('=');
                if (line.startsWith("/") && equals > 0) {
                    add(mappings, line.substring(0, equals).strip(), Path.of(line.substring(equals + 1).strip()), Source.RIDER_LINK);
                }
            }
        }
        catch (IOException e) {
            // an unreadable file only loses the exact mappings; the others still apply
        }
    }

    private static void scanSourceCode(Path root, @Nullable Path projectDirectory, Map<String, Mapping> mappings) {
        if (!Files.isDirectory(root)) {
            return;
        }
        List<Path> sources;
        try (Stream<Path> files = Files.walk(root)) {
            sources = files.filter(path -> isCppSource(path) && !isSkipped(root, path)).toList();
        }
        catch (IOException | UncheckedIOException e) {
            return;
        }
        for (Path source : sources) {
            String text;
            try {
                text = Files.readString(source, StandardCharsets.UTF_8);
            }
            catch (IOException | UncheckedIOException e) {
                continue;
            }
            if (!text.contains(MAPPING_FUNCTION)) {
                continue;
            }
            Path shaders = nearestShadersDirectory(source, projectDirectory);
            if (shaders == null) {
                continue;
            }
            Matcher matcher = MAPPING_CALL.matcher(text);
            while (matcher.find()) {
                add(mappings, matcher.group(1), shaders, Source.SOURCE_CODE);
            }
        }
    }

    private static boolean isCppSource(Path path) {
        String name = path.getFileName().toString();
        return name.endsWith(".cpp") || name.endsWith(".h");
    }

    private static boolean isSkipped(Path root, Path path) {
        for (Path part : root.relativize(path)) {
            if (SKIPPED_DIRECTORIES.contains(part.toString())) {
                return true;
            }
        }
        return false;
    }

    /**
     * The {@code Shaders} directory of the module, plugin or project a source file belongs to: the nearest one found
     * walking up, without leaving the plugin ({@code .uplugin}) or the project.
     */
    private static @Nullable Path nearestShadersDirectory(Path source, @Nullable Path projectDirectory) {
        for (Path directory = source.getParent(); directory != null; directory = directory.getParent()) {
            Path shaders = directory.resolve(SHADERS);
            if (Files.isDirectory(shaders)) {
                return shaders;
            }
            if (directory.equals(projectDirectory) || isPluginRoot(directory) || isEngineDirectory(directory)) {
                return null;
            }
        }
        return null;
    }

    private static boolean isPluginRoot(Path directory) {
        try (Stream<Path> children = Files.list(directory)) {
            return children.anyMatch(child -> child.getFileName().toString().endsWith(".uplugin"));
        }
        catch (IOException | UncheckedIOException e) {
            return false;
        }
    }

    private static boolean isEngineDirectory(Path directory) {
        return directory.getFileName() != null && directory.getFileName().toString().equals("Engine")
            && Files.isRegularFile(directory.resolve("Build/Build.version"));
    }

    private static void addPluginNames(Path plugins, Map<String, Mapping> mappings) {
        if (!Files.isDirectory(plugins)) {
            return;
        }
        List<Path> descriptors;
        try (Stream<Path> files = Files.walk(plugins)) {
            descriptors = files.filter(path -> path.getFileName().toString().endsWith(".uplugin") && !isSkipped(plugins, path)).toList();
        }
        catch (IOException | UncheckedIOException e) {
            return;
        }
        Set<Path> mapped = mappedDirectories(mappings);
        for (Path descriptor : descriptors) {
            Path shaders = descriptor.resolveSibling(SHADERS);
            if (Files.isDirectory(shaders) && !mapped.contains(shaders)) {
                String name = descriptor.getFileName().toString();
                add(mappings, "/Plugin/" + name.substring(0, name.length() - ".uplugin".length()), shaders, Source.PLUGIN_NAME);
            }
        }
    }

    private static Set<Path> mappedDirectories(Map<String, Mapping> mappings) {
        return Set.copyOf(mappings.values().stream().map(Mapping::directory).toList());
    }
}

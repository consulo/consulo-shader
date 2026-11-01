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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Finds Unreal projects and the engine they are built with, the way the engine's own tools do. Works on plain paths so
 * it can run outside the IDE.
 */
public final class UnrealEngineLocator {
    private static final Pattern ENGINE_ASSOCIATION = Pattern.compile("\"EngineAssociation\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern VERSION_NUMBER = Pattern.compile("\"(MajorVersion|MinorVersion)\"\\s*:\\s*(\\d+)");

    private UnrealEngineLocator() {
    }

    /**
     * The {@code .uproject} file of the project {@code file} belongs to: the first one found walking up from it.
     */
    public static @Nullable Path findProjectFile(Path file) {
        for (Path directory = Files.isDirectory(file) ? file : file.getParent(); directory != null; directory = directory.getParent()) {
            try (Stream<Path> children = Files.list(directory)) {
                Path project = children.filter(child -> child.getFileName().toString().endsWith(".uproject") && Files.isRegularFile(child))
                    .findFirst()
                    .orElse(null);
                if (project != null) {
                    return project;
                }
            }
            catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * The {@code EngineAssociation} of a project: an engine version ({@code 5.7}), the GUID of a registered source
     * build, or empty for a project that lives inside an engine tree.
     */
    public static @Nullable String readEngineAssociation(Path projectFile) {
        try {
            Matcher matcher = ENGINE_ASSOCIATION.matcher(Files.readString(projectFile, StandardCharsets.UTF_8));
            return matcher.find() ? matcher.group(1) : null;
        }
        catch (IOException e) {
            return null;
        }
    }

    /**
     * The engine root (the directory holding {@code Engine/}) that {@code file} lies in, for engine shaders and for
     * projects inside a source build.
     */
    public static @Nullable Path findEngineRootContaining(Path file) {
        for (Path directory = file; directory != null; directory = directory.getParent()) {
            if (isEngineRoot(directory)) {
                return directory;
            }
        }
        return null;
    }

    public static boolean isEngineRoot(Path directory) {
        return Files.isRegularFile(directory.resolve("Engine/Build/Build.version"));
    }

    /**
     * {@code Major.Minor} of an engine, read from {@code Engine/Build/Build.version}.
     */
    public static @Nullable String readVersion(Path engineRoot) {
        try {
            String text = Files.readString(engineRoot.resolve("Engine/Build/Build.version"), StandardCharsets.UTF_8);
            Matcher matcher = VERSION_NUMBER.matcher(text);
            String major = null;
            String minor = null;
            while (matcher.find()) {
                if (matcher.group(1).equals("MajorVersion")) {
                    major = matcher.group(2);
                }
                else {
                    minor = matcher.group(2);
                }
            }
            return major == null || minor == null ? null : major + "." + minor;
        }
        catch (IOException e) {
            return null;
        }
    }

    /**
     * The engine a project is built with:
     * <ol>
     *     <li>the source build the project lies in</li>
     *     <li>an engine configured by the user, matching the project's version, or the only one configured</li>
     *     <li>an engine registered with the Epic launcher or {@code UnrealVersionSelector}: {@code Install.ini} on
     *     Linux and macOS, the registry on Windows</li>
     * </ol>
     */
    public static @Nullable Path findEngineRoot(Path projectFile, List<Path> configuredRoots) {
        Path projectDirectory = projectFile.getParent();
        Path containing = projectDirectory == null ? null : findEngineRootContaining(projectDirectory);
        if (containing != null) {
            return containing;
        }

        String association = readEngineAssociation(projectFile);
        List<Path> configured = configuredRoots.stream().filter(UnrealEngineLocator::isEngineRoot).toList();
        if (association != null && !association.isEmpty()) {
            for (Path root : configured) {
                if (association.equals(readVersion(root))) {
                    return root;
                }
            }
        }
        if (configured.size() == 1) {
            return configured.get(0);
        }
        if (association == null || association.isEmpty()) {
            return null;
        }

        for (Path root : registeredEngines(association)) {
            if (isEngineRoot(root)) {
                return root;
            }
        }
        return null;
    }

    /**
     * Engine roots registered for {@code association}.
     */
    static List<Path> registeredEngines(String association) {
        List<Path> roots = new ArrayList<>();
        String home = System.getProperty("user.home");
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            String builds = queryRegistry("HKCU\\Software\\Epic Games\\Unreal Engine\\Builds", association);
            if (builds != null) {
                roots.add(Path.of(builds));
            }
            String installed = queryRegistry("HKLM\\SOFTWARE\\EpicGames\\Unreal Engine\\" + association, "InstalledDirectory");
            if (installed != null) {
                roots.add(Path.of(installed));
            }
        }
        else if (home != null) {
            Path installIni = os.contains("mac")
                ? Path.of(home, "Library/Application Support/Epic/UnrealEngine/Install.ini")
                : Path.of(home, ".config/Epic/UnrealEngine/Install.ini");
            String path = readIniValue(installIni, "Installations", association);
            if (path != null) {
                roots.add(Path.of(path));
            }
        }
        return roots;
    }

    static @Nullable String readIniValue(Path ini, String section, String key) {
        if (!Files.isRegularFile(ini)) {
            return null;
        }
        try {
            boolean inSection = false;
            for (String rawLine : Files.readAllLines(ini, StandardCharsets.UTF_8)) {
                String line = rawLine.strip();
                if (line.startsWith("[")) {
                    inSection = line.equalsIgnoreCase("[" + section + "]");
                }
                else if (inSection) {
                    int equals = line.indexOf('=');
                    if (equals > 0 && line.substring(0, equals).strip().equalsIgnoreCase(key)) {
                        return line.substring(equals + 1).strip();
                    }
                }
            }
        }
        catch (IOException e) {
            return null;
        }
        return null;
    }

    private static @Nullable String queryRegistry(String key, String value) {
        try {
            Process process = new ProcessBuilder("reg", "query", key, "/v", value).redirectErrorStream(true).start();
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            if (!process.waitFor(5, TimeUnit.SECONDS) || process.exitValue() != 0) {
                return null;
            }
            for (String line : output.split("\\R")) {
                int type = line.indexOf("REG_SZ");
                if (type >= 0 && line.strip().startsWith(value)) {
                    return line.substring(type + "REG_SZ".length()).strip();
                }
            }
        }
        catch (IOException e) {
            return null;
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }
}

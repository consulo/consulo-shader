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

import consulo.annotation.component.ExtensionImpl;
import consulo.shader.include.ShaderIncludeKind;
import consulo.shader.include.ShaderIncludeResolver;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Virtual shader paths of Unreal Engine: {@code #include "/Engine/Private/Common.ush"}, {@code "/Plugin/FX/..."},
 * a project's own {@code "/MyGame/..."}. The project is the one whose {@code .uproject} the including file lies under,
 * the engine the one it is built with (see {@link UnrealEngineLocator}); the mappings are discovered once per project
 * and engine (see {@link UnrealShaderMappings}). Only reads the project and engine; files the shader compiler
 * generates ({@code /Engine/Generated/...}) resolve to nothing.
 */
@ExtensionImpl
public class UnrealShaderIncludeResolver implements ShaderIncludeResolver {
    record Key(@Nullable Path engineRoot, @Nullable Path projectDirectory) {
    }

    // per IDE project: the resolver is a project extension
    private final Map<Key, UnrealShaderMappings> myMappings = new ConcurrentHashMap<>();
    private final Map<Path, Optional<Key>> myKeys = new ConcurrentHashMap<>();

    @Override
    public @Nullable VirtualFile resolve(VirtualFile includingFile, String path, ShaderIncludeKind kind) {
        if (!path.startsWith("/") || UnrealShaderMappings.isGenerated(path) || !isUnrealShader(includingFile)) {
            return null;
        }
        VirtualFile directory = includingFile.getParent();
        if (directory == null || !(directory.getFileSystem() instanceof LocalFileSystem)) {
            return null;
        }
        Key key = myKeys.computeIfAbsent(directory.toNioPath(), UnrealShaderIncludeResolver::keyOf).orElse(null);
        if (key == null) {
            return null;
        }
        UnrealShaderMappings mappings = myMappings.computeIfAbsent(key, k -> UnrealShaderMappings.discover(k.engineRoot(), k.projectDirectory()));
        Path file = mappings.resolve(path);
        return file == null ? null : LocalFileSystem.getInstance().findFileByNioFile(file);
    }

    private static boolean isUnrealShader(VirtualFile file) {
        String extension = file.getExtension();
        return "usf".equals(extension) || "ush".equals(extension);
    }

    static Optional<Key> keyOf(Path directory) {
        Path projectFile = UnrealEngineLocator.findProjectFile(directory);
        Path projectDirectory = projectFile == null ? null : projectFile.getParent();
        Path engineRoot = projectFile != null ? UnrealEngineLocator.findEngineRoot(projectFile, List.of())
            : UnrealEngineLocator.findEngineRootContaining(directory);
        return projectDirectory == null && engineRoot == null ? Optional.empty() : Optional.of(new Key(engineRoot, projectDirectory));
    }
}

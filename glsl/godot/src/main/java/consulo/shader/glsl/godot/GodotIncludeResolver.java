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
package consulo.shader.glsl.godot;

import consulo.annotation.component.ExtensionImpl;
import consulo.shader.include.ShaderIncludeKind;
import consulo.shader.include.ShaderIncludeResolver;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

/**
 * Godot resource paths: {@code #include "res://shaders/common.gdshaderinc"} is relative to the Godot project, the
 * nearest directory above the including file that holds {@code project.godot}.
 */
@ExtensionImpl
public class GodotIncludeResolver implements ShaderIncludeResolver {
    private static final String RESOURCE_PREFIX = "res://";
    private static final String PROJECT_FILE = "project.godot";

    @Override
    public @Nullable VirtualFile resolve(VirtualFile includingFile, String path, ShaderIncludeKind kind) {
        if (!path.startsWith(RESOURCE_PREFIX)) {
            return null;
        }
        VirtualFile project = findProjectDirectory(includingFile);
        if (project == null) {
            return null;
        }
        VirtualFile file = project.findFileByRelativePath(path.substring(RESOURCE_PREFIX.length()));
        return file == null || file.isDirectory() ? null : file;
    }

    /**
     * The Godot project {@code file} belongs to: the nearest directory above it holding {@code project.godot}.
     */
    public static @Nullable VirtualFile findProjectDirectory(VirtualFile file) {
        for (VirtualFile directory = file.getParent(); directory != null; directory = directory.getParent()) {
            VirtualFile projectFile = directory.findChild(PROJECT_FILE);
            if (projectFile != null && !projectFile.isDirectory()) {
                return directory;
            }
        }
        return null;
    }
}

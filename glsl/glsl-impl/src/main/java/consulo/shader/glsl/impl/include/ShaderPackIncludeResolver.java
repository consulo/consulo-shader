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
package consulo.shader.glsl.impl.include;

import consulo.annotation.component.ExtensionImpl;
import consulo.shader.include.ShaderIncludeKind;
import consulo.shader.include.ShaderIncludeResolver;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

/**
 * Minecraft shader packs (Iris, OptiFine): a root-relative path ({@code #include "/lib/settings.glsl"}) is relative to
 * the {@code shaders} directory of the pack the including file belongs to.
 */
@ExtensionImpl
public class ShaderPackIncludeResolver implements ShaderIncludeResolver {
    private static final String SHADERS_DIRECTORY = "shaders";

    @Override
    public @Nullable VirtualFile resolve(VirtualFile includingFile, String path, ShaderIncludeKind kind) {
        if (kind != ShaderIncludeKind.QUOTED || !path.startsWith("/")) {
            return null;
        }
        for (VirtualFile directory = includingFile.getParent(); directory != null; directory = directory.getParent()) {
            if (SHADERS_DIRECTORY.equals(directory.getName())) {
                VirtualFile file = directory.findFileByRelativePath(path);
                return file == null || file.isDirectory() ? null : file;
            }
        }
        return null;
    }
}

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
package consulo.shader.impl.include;

import consulo.annotation.component.ExtensionImpl;
import consulo.module.content.ProjectFileIndex;
import consulo.shader.include.ShaderIncludeKind;
import consulo.shader.include.ShaderIncludeResolver;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

/**
 * Resolves root-relative paths ({@code #include "/common/lighting.glsl"}) against the source root of the including
 * file, and against its content root when it is in no source root.
 */
@ExtensionImpl(order = "last")
public class RootRelativeIncludeResolver implements ShaderIncludeResolver {
    private final ProjectFileIndex myProjectFileIndex;

    @Inject
    public RootRelativeIncludeResolver(ProjectFileIndex projectFileIndex) {
        myProjectFileIndex = projectFileIndex;
    }

    @Override
    public @Nullable VirtualFile resolve(VirtualFile includingFile, String path, ShaderIncludeKind kind) {
        if (!path.startsWith("/")) {
            return null;
        }
        VirtualFile included = findIn(myProjectFileIndex.getSourceRootForFile(includingFile), path);
        if (included != null) {
            return included;
        }
        return findIn(myProjectFileIndex.getContentRootForFile(includingFile), path);
    }

    private static @Nullable VirtualFile findIn(@Nullable VirtualFile root, String path) {
        if (root == null) {
            return null;
        }
        VirtualFile file = root.findFileByRelativePath(path);
        return file == null || file.isDirectory() ? null : file;
    }
}

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
package consulo.shader.include;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

/**
 * Resolves the {@code #include} paths of shaders the way the engine or toolchain that compiles them does: Unity's
 * {@code UnityCG.cginc} and {@code Packages/...} paths, Unreal's {@code /Engine/...} virtual paths, a shader pack's
 * root-relative {@code /lib/...} paths, the include directories of a build. A path next to the including file is
 * found before any resolver is asked, see {@link ShaderIncludes#resolve}.
 */
@ExtensionAPI(ComponentScope.PROJECT)
public interface ShaderIncludeResolver {
    /**
     * The file {@code path} names when it is included from {@code includingFile}, or {@code null} when this resolver
     * does not know it.
     *
     * @param path the path as written, without its delimiters, with {@code /} as the separator
     */
    @Nullable VirtualFile resolve(VirtualFile includingFile, String path, ShaderIncludeKind kind);
}

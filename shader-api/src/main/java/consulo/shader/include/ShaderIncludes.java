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

import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

/**
 * Include path resolution shared by all shader languages.
 */
public final class ShaderIncludes {
    private ShaderIncludes() {
    }

    /**
     * The file an {@code #include} of {@code includingFile} names. A quoted relative path is looked up next to the
     * including file first; everything else, and a quoted path not found there, is left to the
     * {@link ShaderIncludeResolver}s.
     *
     * @param path the path as written, without its delimiters
     */
    public static @Nullable VirtualFile resolve(Project project, VirtualFile includingFile, String path, ShaderIncludeKind kind) {
        String normalized = path.replace('\\', '/');
        if (normalized.isEmpty()) {
            return null;
        }

        if (kind == ShaderIncludeKind.QUOTED && !normalized.startsWith("/")) {
            VirtualFile directory = includingFile.getParent();
            VirtualFile sibling = directory == null ? null : directory.findFileByRelativePath(normalized);
            if (sibling != null && !sibling.isDirectory()) {
                return sibling;
            }
        }

        return project.getExtensionPoint(ShaderIncludeResolver.class)
            .computeSafeIfAny(resolver -> resolver.resolve(includingFile, normalized, kind));
    }

    /**
     * The path of an include as written, without its quotes or angle brackets; an unterminated path loses only its
     * opening delimiter.
     */
    public static String stripDelimiters(CharSequence pathWithDelimiters) {
        int length = pathWithDelimiters.length();
        if (length == 0) {
            return "";
        }
        char open = pathWithDelimiters.charAt(0);
        if (open != '"' && open != '<') {
            return pathWithDelimiters.toString();
        }
        char close = open == '<' ? '>' : '"';
        int end = length > 1 && pathWithDelimiters.charAt(length - 1) == close ? length - 1 : length;
        return pathWithDelimiters.subSequence(1, end).toString();
    }
}

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

/**
 * How an {@code #include} names its file.
 */
public enum ShaderIncludeKind {
    /**
     * {@code #include "path"}: looked up next to the including file first.
     */
    QUOTED,
    /**
     * {@code #include <path>}: looked up only in the include roots of the environment.
     */
    ANGLE_BRACKETS;

    /**
     * The kind of an include path as written, delimiters included.
     */
    public static ShaderIncludeKind of(CharSequence pathWithDelimiters) {
        return pathWithDelimiters.length() > 0 && pathWithDelimiters.charAt(0) == '<' ? ANGLE_BRACKETS : QUOTED;
    }
}

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
package consulo.shader.impl.refactoring;

import consulo.language.editor.refactoring.NamesValidator;
import consulo.project.Project;

/**
 * Names a rename may give: identifiers of the language that are not its keywords or built-in type names.
 */
public abstract class ShaderNamesValidator implements NamesValidator {
    /**
     * Whether the language's lexer makes {@code name} a plain identifier.
     */
    protected abstract boolean isPlainIdentifier(String name);

    @Override
    public boolean isKeyword(String name, Project project) {
        return isIdentifierText(name) && !isPlainIdentifier(name);
    }

    @Override
    public boolean isIdentifier(String name, Project project) {
        return isIdentifierText(name) && isPlainIdentifier(name);
    }

    private static boolean isIdentifierText(String name) {
        if (name.isEmpty() || !(Character.isLetter(name.charAt(0)) || name.charAt(0) == '_')) {
            return false;
        }
        for (int i = 1; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!(c == '_' || c < 128 && Character.isLetterOrDigit(c))) {
                return false;
            }
        }
        return true;
    }
}

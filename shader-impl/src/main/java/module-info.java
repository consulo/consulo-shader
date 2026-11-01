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
import org.jspecify.annotations.NullMarked;

@NullMarked
module consulo.shader.impl {
    requires transitive consulo.shader.api;
    requires transitive consulo.language.editor.api;
    requires transitive consulo.language.editor.refactoring.api;
    requires consulo.module.content.api;
    requires transitive consulo.language.impl;

    exports consulo.shader.impl;
    exports consulo.shader.impl.builtins;
    exports consulo.shader.impl.completion;
    exports consulo.shader.impl.findUsage;
    exports consulo.shader.impl.highlight;
    exports consulo.shader.impl.include;
    exports consulo.shader.impl.localize;
    exports consulo.shader.impl.parser;
    exports consulo.shader.impl.psi;
    exports consulo.shader.impl.refactoring;
    exports consulo.shader.impl.resolve;
    exports consulo.shader.impl.type;
}

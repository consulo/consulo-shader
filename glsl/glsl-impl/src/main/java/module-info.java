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
module consulo.shader.glsl.impl {
    requires transitive consulo.shader.glsl.api;
    requires consulo.shader.preprocessor.api;
    requires consulo.shader.impl;
    requires consulo.shader.glsl.godot;
    requires consulo.language.editor.api;
    requires consulo.language.impl;

    exports consulo.shader.glsl.impl;
    exports consulo.shader.glsl.impl.builtins;
    exports consulo.shader.glsl.impl.completion;
    exports consulo.shader.glsl.impl.findUsage;
    exports consulo.shader.glsl.impl.godot;
    exports consulo.shader.glsl.impl.highlight;
    exports consulo.shader.glsl.impl.include;
    exports consulo.shader.glsl.impl.lexer;
    exports consulo.shader.glsl.impl.localize;
    exports consulo.shader.glsl.impl.parser;
    exports consulo.shader.glsl.impl.psi;
    exports consulo.shader.glsl.impl.psi.impl;
    exports consulo.shader.glsl.impl.refactoring;
    exports consulo.shader.glsl.impl.resolve;
    exports consulo.shader.glsl.impl.version;
}

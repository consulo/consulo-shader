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
module consulo.shader.shaderlab {
    requires transitive consulo.language.api;
    requires transitive consulo.language.editor.api;
    requires consulo.language.impl;
    requires consulo.shader.glsl.api;
    requires consulo.shader.hlsl.api;

    exports consulo.shader.shaderlab.icon;
    exports consulo.shader.shaderlab.ide;
    exports consulo.shader.shaderlab.ide.completion;
    exports consulo.shader.shaderlab.ide.editor;
    exports consulo.shader.shaderlab.ide.highlight;
    exports consulo.shader.shaderlab.ide.refactoring;
    exports consulo.shader.shaderlab.lang;
    exports consulo.shader.shaderlab.lang.lexer;
    exports consulo.shader.shaderlab.lang.parser;
    exports consulo.shader.shaderlab.lang.pragma;
    exports consulo.shader.shaderlab.lang.parser.roles;
    exports consulo.shader.shaderlab.lang.psi;
    exports consulo.shader.shaderlab.lang.psi.impl;
    exports consulo.shader.shaderlab.lang.psi.light;
    exports consulo.shader.shaderlab.lang.psi.stub;
    exports consulo.shader.shaderlab.lang.psi.stub.elementType;
    exports consulo.shader.shaderlab.lang.psi.stub.index;
}

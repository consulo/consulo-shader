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
package consulo.shader.glsl.impl;

import consulo.language.version.LanguageVersion;
import consulo.shader.glsl.GlslLanguage;
import consulo.shader.glsl.godot.GodotShaderLanguageVersion;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Every Godot shader of the corpus parses without errors. The corpus is the parsing test data of gdshader-support
 * (MIT License, see NOTICE.txt), without its intentionally broken files. {@code -Dgodot.corpus=<dir>} adds a directory.
 */
public class GodotParsingCorpusTest extends ShaderParsingCorpusTestBase {
    @TestFactory
    public Stream<DynamicTest> godot(Context context) throws Exception {
        LanguageVersion version = Objects.requireNonNull(GlslLanguage.INSTANCE.findVersionByClass(GodotShaderLanguageVersion.class));
        return corpus(context, "parsing/godot", System.getProperty("godot.corpus"), Set.of("gdshader", "gdshaderinc"), version);
    }

    // the expression and statement inputs of gdshader-support are snippets with undeclared names
    @Override
    protected boolean checksNames(Path relative) {
        return !relative.startsWith("expressions") && !relative.startsWith("statements");
    }
}

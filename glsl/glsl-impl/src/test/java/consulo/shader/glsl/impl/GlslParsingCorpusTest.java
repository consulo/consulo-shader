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
import consulo.shader.glsl.impl.version.GlslOpenGlLanguageVersion;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Every GLSL file of the corpus parses without errors. {@code -Dglsl.corpus=<dir>} adds the files of a directory, to run
 * against corpora that are not part of the repository.
 */
public class GlslParsingCorpusTest extends ShaderParsingCorpusTestBase {
    @TestFactory
    public Stream<DynamicTest> glsl(Context context) throws Exception {
        LanguageVersion version = Objects.requireNonNull(GlslLanguage.INSTANCE.findVersionByClass(GlslOpenGlLanguageVersion.class));
        return corpus(context, "parsing/glsl", System.getProperty("glsl.corpus"), Set.of("glsl", "vert", "frag", "geom", "tesc", "tese",
            "comp", "vsh", "fsh", "gsh", "rgen", "rchit", "rmiss", "rahit", "rint", "rcall"), version);
    }
}

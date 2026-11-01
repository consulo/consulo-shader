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
package consulo.shader.hlsl.impl;

import consulo.language.version.LanguageVersion;
import consulo.shader.hlsl.HlslLanguage;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Every HLSL file of the corpus parses without errors: the HLSL tests of glslang that it compiles without errors, the
 * HLSL test inputs of resharper-unity and the program blocks of its ShaderLab inputs, and the samples (see NOTICE.txt).
 * {@code -Dhlsl.corpus=<dir>} adds the files of a directory, to run against corpora that are not part of the repository.
 * <p>
 * Left out of the glslang tests, as syntax only FXC accepts: hlsl.type.identifier.frag and hlsl.cbuffer-identifier.vert
 * (type names as identifiers: {@code float float;}), hlsl.pp.expand.frag (a macro after a declarator) and
 * hlsl.pp.line3.frag (an {@code #include} as the body of an {@code if}).
 */
public class HlslParsingCorpusTest extends ShaderParsingCorpusTestBase {
    @TestFactory
    public Stream<DynamicTest> hlsl(Context context) throws Exception {
        LanguageVersion version = HlslLanguage.INSTANCE.getVersions()[0];
        return corpus(context, "parsing/hlsl", System.getProperty("hlsl.corpus"), Set.of("hlsl", "hlsli", "fx", "fxh", "cg", "cginc",
            "usf", "ush", "compute", "vert", "frag", "geom", "tesc", "tese", "comp"), version);
    }

    // program blocks of ShaderLab files: Unity compiles them with implicit includes (UnityCG, UnityShaderVariables)
    @Override
    protected boolean checksNames(Path relative) {
        return !relative.startsWith("resharper-unity-programs");
    }
}

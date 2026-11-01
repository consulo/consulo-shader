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
package consulo.shader.shaderlab;

import consulo.language.psi.PsiErrorElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiFileFactory;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.shader.shaderlab.lang.ShaderLabLanguage;
import consulo.test.junit.impl.extension.ConsuloProjectLoader;
import consulo.test.junit.impl.extension.InjectingRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * Parses the ShaderLab lexing and parsing inputs of JetBrains resharper-unity (Apache License 2.0, see NOTICE.txt).
 * Some of them are deliberately broken, so the test only requires that every file parses without exceptions and keeps
 * its text; the files with error elements are listed in the output.
 */
@ExtendWith(ConsuloProjectLoader.class)
public class ShaderLabParsingCorpusTest {
    public record Context(PsiFileFactory psiFileFactory) implements InjectingRecord {
    }

    @TestFactory
    public Stream<DynamicTest> jetbrains(Context context) throws Exception {
        URL resource = getClass().getClassLoader().getResource("jetbrains");
        Assertions.assertNotNull(resource);
        Path root = Path.of(resource.toURI());
        List<Path> files;
        try (Stream<Path> walk = Files.walk(root)) {
            files = walk.filter(path -> path.getFileName().toString().endsWith(".shader")).sorted().toList();
        }
        Assertions.assertFalse(files.isEmpty());
        return files.stream().map(path -> DynamicTest.dynamicTest(root.relativize(path).toString(), () -> {
            // ReSharper test markers are not ShaderLab
            String text = new String(Files.readAllBytes(path), StandardCharsets.UTF_8).replace("\r\n", "\n").replaceAll("\\{(caret|selstart|selend)[^}]*\\}", "");
            PsiFile file = context.psiFileFactory().createFileFromText(path.getFileName().toString(), ShaderLabLanguage.INSTANCE, text);
            Assertions.assertEquals(text, file.getText(), "psi text mismatch");
            int errors = PsiTreeUtil.collectElementsOfType(file, PsiErrorElement.class).size();
            if (errors > 0) {
                System.out.println("SHADERLAB-ERRORS " + errors + " " + root.relativize(path));
            }
        }));
    }
}

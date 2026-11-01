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

import consulo.language.psi.PsiErrorElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiFileFactory;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.version.LanguageVersion;
import consulo.shader.impl.highlight.ShaderUnresolvedReferences;
import consulo.shader.psi.ShaderReferenceElement;
import consulo.test.junit.impl.extension.ConsuloProjectLoader;
import consulo.test.junit.impl.extension.InjectingRecord;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Parses every file of a corpus and fails on any error element or reported unresolved name, listing each with its line.
 */
@ExtendWith(ConsuloProjectLoader.class)
public abstract class ShaderParsingCorpusTestBase {
    public record Context(PsiFileFactory psiFileFactory) implements InjectingRecord {
    }

    protected Stream<DynamicTest> corpus(Context context,
                                         String resourceDirectory,
                                         @Nullable String externalDirectory,
                                         Set<String> extensions,
                                         LanguageVersion version) throws Exception {
        List<Path> roots = new ArrayList<>();
        URL resource = getClass().getClassLoader().getResource(resourceDirectory);
        Assertions.assertNotNull(resource, resourceDirectory);
        roots.add(Path.of(resource.toURI()));
        if (externalDirectory != null && !externalDirectory.isEmpty()) {
            roots.add(Path.of(externalDirectory));
        }

        List<DynamicTest> tests = new ArrayList<>();
        for (Path root : roots) {
            List<Path> files;
            try (Stream<Path> walk = Files.walk(root)) {
                files = walk.filter(path -> Files.isRegularFile(path) && extensions.contains(extensionOf(path))).sorted().toList();
            }
            for (Path file : files) {
                Path relative = root.relativize(file);
                tests.add(DynamicTest.dynamicTest(relative.toString(), () -> checkFile(context, file, version, checksNames(relative))));
            }
        }
        Assertions.assertFalse(tests.isEmpty(), "empty corpus");
        return tests.stream();
    }

    private static String extensionOf(Path path) {
        String name = path.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1);
    }

    /**
     * Whether a file of the corpus must also have no unresolved names: not for inputs written only to test the parser.
     */
    protected boolean checksNames(Path relative) {
        return true;
    }

    private static void checkFile(Context context, Path path, LanguageVersion version, boolean checkNames) throws IOException {
        String text = new String(Files.readAllBytes(path), StandardCharsets.ISO_8859_1).replace("\r\n", "\n");
        PsiFile file = context.psiFileFactory().createFileFromText(path.getFileName().toString(), version.getLanguage(), version, text);
        Assertions.assertEquals(text, file.getText(), "psi text mismatch");

        List<String> errors = new ArrayList<>();
        for (PsiErrorElement error : PsiTreeUtil.collectElementsOfType(file, PsiErrorElement.class)) {
            int offset = error.getTextRange().getStartOffset();
            int line = (int) text.substring(0, offset).chars().filter(c -> c == '\n').count() + 1;
            String around = text.substring(Math.max(0, offset - 20), Math.min(text.length(), offset + 20)).replace('\n', ' ');
            errors.add("line " + line + ": " + error.getErrorDescriptionValue().get() + "  [" + around + "]");
        }
        for (ShaderReferenceElement reference : PsiTreeUtil.collectElementsOfType(file, ShaderReferenceElement.class)) {
            if (checkNames && ShaderUnresolvedReferences.isReported(reference)) {
                int offset = reference.getTextRange().getStartOffset();
                int line = (int) text.substring(0, offset).chars().filter(c -> c == '\n').count() + 1;
                errors.add("line " + line + ": unresolved " + reference.getReferenceNameElement().getText());
            }
        }
        if (!errors.isEmpty()) {
            Assertions.fail(errors.size() + " parse errors or unresolved names in " + path + "\n" + String.join("\n", errors));
        }
    }
}

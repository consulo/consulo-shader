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

import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiErrorElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiFileFactory;
import consulo.language.psi.PsiReference;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.version.LanguageVersion;
import consulo.project.Project;
import consulo.shader.glsl.GlslLanguage;
import consulo.shader.glsl.godot.GodotShaderLanguageVersion;
import consulo.shader.glsl.impl.godot.GodotBuiltinsFile;
import consulo.test.junit.impl.extension.ConsuloProjectLoader;
import consulo.test.junit.impl.extension.InjectingRecord;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Objects;

/**
 * Godot built-ins resolve into the generated built-ins file, user declarations win over them.
 */
@ExtendWith(ConsuloProjectLoader.class)
public class GodotResolveTest {
    public record Context(Project project, PsiFileFactory psiFileFactory) implements InjectingRecord {
    }

    @Test
    public void builtinsFileParses(Context context) {
        PsiElement albedo = GodotBuiltinsFile.getInstance(context.project()).find("ALBEDO");
        Assertions.assertNotNull(albedo);
        PsiFile file = Objects.requireNonNull(albedo.getContainingFile());
        Assertions.assertEquals(0, PsiTreeUtil.collectElementsOfType(file, PsiErrorElement.class).size(), file.getText());
    }

    @Test
    public void builtinsResolve(Context context) {
        // tex is declared: with an argument of unknown type the overloads of texture would be ambiguous
        String text = "shader_type spatial;\nuniform sampler2D tex;\nvoid fragment() {\n\tALBEDO = texture(tex, UV).rgb * TIME;\n}\n";
        PsiFile file = parse(context, text);
        for (String name : new String[]{"ALBEDO", "texture", "TIME"}) {
            PsiElement target = resolveAt(file, text.indexOf(name));
            Assertions.assertNotNull(target, name);
            Assertions.assertTrue(GodotBuiltinsFile.isBuiltin(target), name + " -> " + target);
        }
    }

    @Test
    public void userDeclarationWins(Context context) {
        String text = "shader_type spatial;\nuniform float TIME;\nvoid fragment() {\n\tfloat t = TIME;\n}\n";
        PsiFile file = parse(context, text);
        PsiElement target = resolveAt(file, text.lastIndexOf("TIME"));
        Assertions.assertNotNull(target);
        Assertions.assertFalse(GodotBuiltinsFile.isBuiltin(target));
    }

    private static PsiFile parse(Context context, String text) {
        LanguageVersion version = Objects.requireNonNull(GlslLanguage.INSTANCE.findVersionByClass(GodotShaderLanguageVersion.class));
        return context.psiFileFactory().createFileFromText("test.gdshader", GlslLanguage.INSTANCE, version, text);
    }

    private static @Nullable PsiElement resolveAt(PsiFile file, int offset) {
        PsiReference reference = file.findReferenceAt(offset);
        Assertions.assertNotNull(reference, "no reference at " + offset);
        return reference.resolve();
    }
}

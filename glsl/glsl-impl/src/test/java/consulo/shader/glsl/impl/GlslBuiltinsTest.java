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
import consulo.shader.ShaderStage;
import consulo.shader.glsl.GlslLanguage;
import consulo.shader.impl.builtins.GeneratedDeclarationsFile;
import consulo.shader.glsl.impl.builtins.GlslBuiltin;
import consulo.shader.glsl.impl.builtins.GlslBuiltins;
import consulo.shader.glsl.impl.version.GlslOpenGlLanguageVersion;
import consulo.test.junit.impl.extension.ConsuloProjectLoader;
import consulo.test.junit.impl.extension.InjectingRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Objects;

/**
 * The GLSL built-ins generated from glslang: the data, the generated declarations file and resolution into it.
 */
@ExtendWith(ConsuloProjectLoader.class)
public class GlslBuiltinsTest {
    public record Context(Project project, PsiFileFactory psiFileFactory) implements InjectingRecord {
    }

    @Test
    public void stages() {
        GlslBuiltin position = find("gl_Position");
        Assertions.assertTrue(position.isAvailableIn(ShaderStage.VERTEX));
        Assertions.assertFalse(position.isAvailableIn(ShaderStage.FRAGMENT));
        Assertions.assertTrue(find("gl_FragCoord").isAvailableIn(ShaderStage.FRAGMENT));
        Assertions.assertTrue(find("gl_VertexIndex").isAvailable("vulkan", ShaderStage.VERTEX));
        Assertions.assertFalse(find("gl_VertexIndex").isAvailable("gl", ShaderStage.VERTEX));
        Assertions.assertTrue(find("barrier").isAvailableIn(ShaderStage.COMPUTE));
    }

    @Test
    public void declarationsFileParses(Context context) {
        PsiFile file = GlslBuiltins.getFile(context.project()).getFile();
        List<PsiErrorElement> errors = List.copyOf(PsiTreeUtil.collectElementsOfType(file, PsiErrorElement.class));
        Assertions.assertTrue(errors.isEmpty(), errors.size() + " errors, first near: "
            + (errors.isEmpty() ? "" : file.getText().substring(errors.get(0).getTextRange().getStartOffset(),
            Math.min(file.getTextLength(), errors.get(0).getTextRange().getStartOffset() + 80))));
    }

    @Test
    public void builtinsResolve(Context context) {
        String text = "#version 460\nlayout(location = 0) out vec4 color;\nuniform sampler2D tex;\nvoid main() {\n"
            + "    color = texture(tex, gl_FragCoord.xy) + vec4(dot(vec3(1.0), vec3(2.0)));\n    gl_Position = color;\n}\n";
        LanguageVersion version = Objects.requireNonNull(GlslLanguage.INSTANCE.findVersionByClass(GlslOpenGlLanguageVersion.class));
        PsiFile file = context.psiFileFactory().createFileFromText("test.glsl", GlslLanguage.INSTANCE, version, text);
        for (String name : new String[]{"texture", "gl_FragCoord", "dot", "gl_Position"}) {
            PsiReference reference = file.findReferenceAt(text.indexOf(name));
            Assertions.assertNotNull(reference, name);
            PsiElement target = reference.resolve();
            Assertions.assertNotNull(target, name);
            Assertions.assertTrue(GeneratedDeclarationsFile.isBuiltin(target), name + " -> " + target);
        }
    }

    private static GlslBuiltin find(String name) {
        return GlslBuiltins.getAll().stream().filter(builtin -> builtin.name().equals(name)).findFirst()
            .orElseThrow(() -> new AssertionError(name));
    }

    @Test
    public void perVertexMembers(Context context) {
        String text = "#version 450\nlayout(triangles) in; void main() { vec4 p = gl_in[0].gl_Position; vec2 xy = p.xy; }";
        consulo.language.psi.PsiFile file = context.psiFileFactory().createFileFromText("test.geom", consulo.shader.glsl.GlslLanguage.INSTANCE, text);
        consulo.language.psi.PsiReference reference = file.findReferenceAt(text.indexOf("gl_Position"));
        Assertions.assertNotNull(reference);
        consulo.language.psi.PsiElement target = reference.resolve();
        Assertions.assertNotNull(target);
        Assertions.assertTrue(consulo.shader.impl.builtins.GeneratedDeclarationsFile.isBuiltin(target));
        Assertions.assertNotNull(consulo.language.psi.util.PsiTreeUtil.getParentOfType(target, consulo.shader.psi.ShaderTypeDefinition.class));
    }
}

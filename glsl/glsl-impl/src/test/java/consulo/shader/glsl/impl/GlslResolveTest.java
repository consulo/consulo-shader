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
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiFileFactory;
import consulo.language.psi.PsiNameIdentifierOwner;
import consulo.language.psi.PsiReference;
import consulo.language.version.LanguageVersion;
import consulo.shader.glsl.GlslLanguage;
import consulo.shader.glsl.impl.version.GlslOpenGlLanguageVersion;
import consulo.test.junit.impl.extension.ConsuloProjectLoader;
import consulo.test.junit.impl.extension.InjectingRecord;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Objects;

/**
 * Name resolution: a reference is marked with {@code <ref>} before it, its expected declaration with {@code <decl>}
 * before its name; without {@code <decl>} the reference must not resolve.
 */
@ExtendWith(ConsuloProjectLoader.class)
public class GlslResolveTest {
    public record Context(PsiFileFactory psiFileFactory) implements InjectingRecord {
    }

    @Test
    public void localVariable(Context context) {
        check(context, "void main() { float <decl>x = 1.0; float y = <ref>x * 2.0; }");
    }

    @Test
    public void localNotVisibleBeforeDeclaration(Context context) {
        check(context, "void main() { float y = <ref>x; float x = 1.0; }");
    }

    @Test
    public void innerScopeShadowsOuter(Context context) {
        check(context, "float x; void main() { float <decl>x = 1.0; { float y = <ref>x; } }");
    }

    @Test
    public void parameter(Context context) {
        check(context, "float twice(float <decl>value) { return <ref>value * 2.0; }");
    }

    @Test
    public void forLoopVariable(Context context) {
        check(context, "void main() { for (int <decl>i = 0; <ref>i < 4; i++) { } }");
    }

    @Test
    public void globalUsedBeforeItsDeclaration(Context context) {
        check(context, "void main() { float y = <ref>g; } uniform float <decl>g;");
    }

    @Test
    public void functionDefinitionWinsOverPrototype(Context context) {
        check(context, "float f(float a); void main() { <ref>f(1.0); } float <decl>f(float a) { return a; }");
    }

    @Test
    public void structType(Context context) {
        check(context, "struct <decl>Light { vec3 direction; }; void main() { <ref>Light light; }");
    }

    @Test
    public void interfaceBlockInstance(Context context) {
        check(context, "uniform <decl>Camera { mat4 view; } camera; void main() { mat4 v = <ref>camera.view; }");
    }

    @Test
    public void anonymousInterfaceBlockMember(Context context) {
        check(context, "uniform Camera { mat4 <decl>view; }; void main() { mat4 v = <ref>view; }");
    }

    @Test
    public void structField(Context context) {
        check(context, "struct Light { vec3 <decl>color; }; uniform Light light; void main() { vec3 c = light.<ref>color; }");
    }

    @Test
    public void nestedStructField(Context context) {
        check(context, "struct A { float <decl>v; }; struct B { A a; }; void main() { B b; float x = b.a.<ref>v; }");
    }

    @Test
    public void arrayOfStructsField(Context context) {
        check(context, "struct L { vec3 <decl>c; }; uniform L lights[4]; void main() { vec3 x = lights[1].<ref>c; }");
    }

    @Test
    public void interfaceBlockInstanceMember(Context context) {
        check(context, "uniform Camera { mat4 <decl>view; } camera; void main() { mat4 v = camera.<ref>view; }");
    }

    @Test
    public void fieldOfReturnedStruct(Context context) {
        check(context, "struct S { float <decl>v; }; S make() { S s; return s; } void main() { float x = make().<ref>v; }");
    }

    @Test
    public void swizzleHasNoDeclaration(Context context) {
        check(context, "void main() { vec4 v; float x = v.<ref>x; }");
    }

    private static void check(Context context, String markedText) {
        String text = markedText.replace("<ref>", "").replace("<decl>", "");
        int referenceOffset = offsetOf(markedText, "<ref>");
        int declarationOffset = markedText.contains("<decl>") ? offsetOf(markedText, "<decl>") : -1;

        LanguageVersion version = Objects.requireNonNull(GlslLanguage.INSTANCE.findVersionByClass(GlslOpenGlLanguageVersion.class));
        PsiFile file = context.psiFileFactory().createFileFromText("test.glsl", GlslLanguage.INSTANCE, version, text);
        PsiReference reference = file.findReferenceAt(referenceOffset);
        Assertions.assertNotNull(reference, "no reference at " + referenceOffset);
        @Nullable PsiElement target = reference.resolve();
        if (declarationOffset < 0) {
            Assertions.assertNull(target, "should not resolve");
            return;
        }
        Assertions.assertNotNull(target, "unresolved");
        PsiElement name = target instanceof PsiNameIdentifierOwner owner ? owner.getNameIdentifier() : null;
        Assertions.assertNotNull(name, "no name identifier: " + target);
        Assertions.assertEquals(declarationOffset, name.getTextRange().getStartOffset(), "wrong target: " + target.getText());
    }

    /**
     * Offset of a marker in the text without markers.
     */
    private static int offsetOf(String markedText, String marker) {
        int index = markedText.indexOf(marker);
        String before = markedText.substring(0, index).replace("<ref>", "").replace("<decl>", "");
        return before.length();
    }
}

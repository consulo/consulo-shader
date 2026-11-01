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

import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiFileFactory;
import consulo.language.psi.PsiNameIdentifierOwner;
import consulo.language.psi.PsiReference;
import consulo.language.psi.scope.LocalSearchScope;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
import consulo.shader.hlsl.HlslLanguage;
import consulo.shader.hlsl.impl.refactoring.HlslNamesValidator;
import consulo.shader.impl.highlight.ShaderUnresolvedReferences;
import consulo.shader.impl.resolve.ShaderScopes;
import consulo.shader.psi.ShaderNamedElement;
import consulo.shader.psi.ShaderReferenceElement;
import consulo.test.junit.impl.extension.ConsuloProjectLoader;
import consulo.test.junit.impl.extension.InjectingRecord;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Name resolution through the shared resolver: a reference is marked with {@code <ref>} before it, its expected
 * declaration with {@code <decl>} before its name; without {@code <decl>} the reference must not resolve.
 */
@ExtendWith(ConsuloProjectLoader.class)
public class HlslResolveTest {
    public record Context(PsiFileFactory psiFileFactory, Project project) implements InjectingRecord {
    }

    @Test
    public void localAndParameter(Context context) {
        check(context, "float4 main(float4 <decl>p : SV_Position) : SV_Target { float4 c = <ref>p; return c; }");
        check(context, "float4 main(float4 p : SV_Position) : SV_Target { float4 <decl>c = p; return <ref>c; }");
    }

    @Test
    public void localNotVisibleBeforeDeclaration(Context context) {
        check(context, "void main() { float y = <ref>x; float x = 1.0; }");
    }

    @Test
    public void constantBufferMember(Context context) {
        check(context, "cbuffer Params : register(b0) { float4 <decl>_Color; }; float4 frag() : SV_Target { return <ref>_Color; }");
    }

    @Test
    public void unityMacroBuffer(Context context) {
        check(context, "CBUFFER_START(UnityPerMaterial) float4 <decl>_BaseColor; CBUFFER_END\nfloat4 frag() : SV_Target { return <ref>_BaseColor; }");
    }

    @Test
    public void structTypeAndMethodField(Context context) {
        check(context, "struct <decl>Light { float3 dir; }; float3 f(<ref>Light l) { return l.dir; }");
        check(context, "struct Light { float3 <decl>dir; float3 get() { return <ref>dir; } };");
    }

    @Test
    public void methodDefinedOutsideStruct(Context context) {
        check(context, "struct S { float g(); }; float S::<decl>g() { return 0; } float f(S s) { return <ref>g(); }");
    }

    @Test
    public void templatedResource(Context context) {
        check(context, "struct <decl>Particle { float3 p; }; RWStructuredBuffer<<ref>Particle> particles : register(u0);");
    }

    @Test
    public void typedefAndEnum(Context context) {
        check(context, "typedef float4 <decl>Color; <ref>Color c;");
        check(context, "enum Mode { <decl>Fast, Slow }; int f() { return <ref>Fast; }");
    }

    @Test
    public void contextualKeywordAsName(Context context) {
        check(context, "Texture2D t; SamplerState s; float4 f(float2 uv) { float4 <decl>sample = t.Sample(s, uv); return <ref>sample; }");
    }

    @Test
    public void conditionDeclaration(Context context) {
        check(context, "float f(float4 v) { if (float <decl>x = v.x) return <ref>x; return 0; }");
    }

    @Test
    public void forLoopVariable(Context context) {
        check(context, "void main() { [unroll] for (int <decl>i = 0; <ref>i < 4; i++) { } }");
    }

    @Test
    public void definitionWinsOverPrototype(Context context) {
        check(context, "float f(float a); void main() { <ref>f(1.0); } float <decl>f(float a) { return a; }");
    }

    @Test
    public void structFieldAndMethod(Context context) {
        check(context, "struct Light { float3 <decl>dir; }; float3 f(Light l) { return l.<ref>dir; }");
        check(context, "struct S { float <decl>get() { return 1; } }; float f(S s) { return s.<ref>get(); }");
    }

    @Test
    public void structuredBufferElement(Context context) {
        check(context, "struct P { float3 <decl>pos; }; StructuredBuffer<P> ps; float3 f(uint i) { return ps[i].<ref>pos; }");
        check(context, "struct P { float3 <decl>pos; }; StructuredBuffer<P> ps; float3 f(uint i) { return ps.Load(i).<ref>pos; }");
    }

    @Test
    public void constantBufferStructMember(Context context) {
        check(context, "struct M { float4x4 <decl>world; }; cbuffer C { M m; }; float4x4 f() { return m.<ref>world; }");
    }

    @Test
    public void typedefOfStruct(Context context) {
        check(context, "struct L { float <decl>a; }; typedef L Alias; float f(Alias x) { return x.<ref>a; }");
    }

    @Test
    public void swizzleHasNoDeclaration(Context context) {
        check(context, "float f(float4 c) { return c.<ref>x; }");
    }

    @Test
    public void overloadByArgumentType(Context context) {
        check(context, "float f(float a) { return a; } float <decl>f(float2 a) { return a.x; } float g(float2 v) { return <ref>f(v); }");
        check(context, "float <decl>f(float a) { return a; } float f(float2 a) { return a.x; } float g(float v) { return <ref>f(v); }");
    }

    @Test
    public void overloadByArgumentCount(Context context) {
        check(context, "float f(float a) { return a; } float <decl>f(float a, float b) { return b; } float g() { return <ref>f(1, 2); }");
        check(context, "float <decl>f(float a, float b = 1) { return b; } float g() { return <ref>f(1); }");
    }

    @Test
    public void overloadsOfUnknownArgumentAreAmbiguous(Context context) {
        check(context, "float f(float a) { return a; } float f(int a) { return 0; } float g() { return <ref>f(unknown); }");
    }

    @Test
    public void builtinOverloadByArgumentType(Context context) {
        String text = "float4x4 a; float4x4 b; float4 v; void main() { float4x4 m = mul(a, b); float4 w = mul(v, a); }";
        PsiFile file = context.psiFileFactory().createFileFromText("test.hlsl", HlslLanguage.INSTANCE, text);
        PsiReference matrices = file.findReferenceAt(text.indexOf("mul(a"));
        PsiReference vector = file.findReferenceAt(text.indexOf("mul(v"));
        Assertions.assertNotNull(matrices);
        Assertions.assertNotNull(vector);
        PsiElement matrixTarget = matrices.resolve();
        PsiElement vectorTarget = vector.resolve();
        Assertions.assertNotNull(matrixTarget);
        Assertions.assertNotNull(vectorTarget);
        Assertions.assertTrue(matrixTarget.getText().startsWith("float4x4 mul(float4x4 a, float4x4 b)"), matrixTarget.getText());
        Assertions.assertTrue(vectorTarget.getText().startsWith("float4 mul(float4 v, float4x4 m)"), vectorTarget.getText());
    }

    @Test
    public void qualifiedStaticMember(Context context) {
        check(context, "struct Test { static float <decl>f(float a) { return a; } }; float g() { return Test::<ref>f(1); }");
        check(context, "enum class Mode { Fast, <decl>Slow }; int g() { return (int) Mode::<ref>Slow; }");
    }

    @Test
    public void unresolvedNamesReported(Context context) {
        Assertions.assertTrue(reported(context, "float g() { return missing; }"));
        Assertions.assertFalse(reported(context, "#include \"common.hlsl\"\nfloat g() { return missing; }"));
        Assertions.assertFalse(reported(context, "#define missing 1\nfloat g() { return missing; }"));
        Assertions.assertFalse(reported(context, "float g() { return __LINE__; }"));
    }

    @Test
    public void localUseScope(Context context) {
        PsiFile file = context.psiFileFactory().createFileFromText("test.hlsl", HlslLanguage.INSTANCE, "float g; float f(float p) { float l = p; return l + g; }");
        for (ShaderNamedElement declaration : PsiTreeUtil.findChildrenOfType(file, ShaderNamedElement.class)) {
            boolean local = !"g".equals(declaration.getName()) && !"f".equals(declaration.getName());
            Assertions.assertEquals(local, ShaderScopes.localOwner(declaration) != null, declaration.getName());
            if (local) {
                Assertions.assertInstanceOf(LocalSearchScope.class, declaration.getUseScope(), declaration.getName());
            }
        }
        HlslNamesValidator validator = new HlslNamesValidator();
        Assertions.assertTrue(validator.isIdentifier("color", context.project()));
        Assertions.assertTrue(validator.isKeyword("float4", context.project()));
        Assertions.assertFalse(validator.isIdentifier("1x", context.project()));
    }

    private static boolean reported(Context context, String text) {
        PsiFile file = context.psiFileFactory().createFileFromText("test.hlsl", HlslLanguage.INSTANCE, text);
        return PsiTreeUtil.findChildrenOfType(file, ShaderReferenceElement.class).stream().anyMatch(ShaderUnresolvedReferences::isReported);
    }

    private static void check(Context context, String markedText) {
        String text = markedText.replace("<ref>", "").replace("<decl>", "");
        int referenceOffset = offsetOf(markedText, "<ref>");
        int declarationOffset = markedText.contains("<decl>") ? offsetOf(markedText, "<decl>") : -1;

        PsiFile file = context.psiFileFactory().createFileFromText("test.hlsl", HlslLanguage.INSTANCE, text);
        PsiReference reference = file.findReferenceAt(referenceOffset);
        Assertions.assertNotNull(reference, "no reference at " + referenceOffset);
        @Nullable PsiElement target = reference.resolve();
        if (declarationOffset < 0) {
            Assertions.assertNull(target, "should not resolve");
            return;
        }
        Assertions.assertNotNull(target, "unresolved: " + markedText);
        Assertions.assertTrue(target instanceof PsiNameIdentifierOwner, String.valueOf(target));
        Assertions.assertEquals(declarationOffset, target.getTextOffset(), "wrong target " + target + " in " + markedText);
    }

    private static int offsetOf(String markedText, String marker) {
        int offset = markedText.indexOf(marker);
        String before = markedText.substring(0, offset);
        return before.replace("<ref>", "").replace("<decl>", "").length();
    }
}

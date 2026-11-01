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
import consulo.language.psi.PsiErrorElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiFileFactory;
import consulo.language.psi.PsiReference;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
import consulo.shader.hlsl.HlslLanguage;
import consulo.shader.hlsl.impl.builtins.HlslBuiltins;
import consulo.shader.impl.builtins.GeneratedDeclarationsFile;
import consulo.shader.impl.type.ShaderType;
import consulo.shader.impl.type.ShaderTypeEvaluator;
import consulo.shader.psi.ShaderMemberAccess;
import consulo.shader.psi.ShaderFunction;
import consulo.shader.psi.ShaderNameKind;
import consulo.shader.psi.ShaderTypeDefinition;
import consulo.test.junit.impl.extension.ConsuloProjectLoader;
import consulo.test.junit.impl.extension.InjectingRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collection;
import java.util.List;

/**
 * The generated file of HLSL intrinsics parses, and names a file does not declare resolve into it.
 */
@ExtendWith(ConsuloProjectLoader.class)
public class HlslBuiltinsTest {
    public record Context(PsiFileFactory psiFileFactory, Project project) implements InjectingRecord {
    }

    @Test
    public void declarationsFileParses(Context context) {
        PsiFile file = context.psiFileFactory().createFileFromText(HlslBuiltins.FILE_NAME, HlslLanguage.INSTANCE, HlslBuiltins.getText());
        Collection<PsiErrorElement> errors = PsiTreeUtil.findChildrenOfType(file, PsiErrorElement.class);
        Assertions.assertTrue(errors.isEmpty(), () -> errors.size() + " errors, first near: "
            + file.getText().substring(errors.iterator().next().getTextOffset(), Math.min(file.getTextLength(), errors.iterator().next().getTextOffset() + 80)));
    }

    @Test
    public void builtinsAreDeclared(Context context) {
        GeneratedDeclarationsFile builtins = HlslBuiltins.getFile(context.project());
        for (String name : List.of("mul", "saturate", "lerp", "tex2D", "InterlockedAdd", "WaveActiveSum", "TraceRay", "GroupMemoryBarrierWithGroupSync")) {
            Assertions.assertInstanceOf(ShaderFunction.class, builtins.find(name), name);
        }
        Assertions.assertInstanceOf(ShaderTypeDefinition.class, builtins.find("RayDesc", ShaderNameKind.TYPE));
        Assertions.assertTrue(builtins.getNames(ShaderNameKind.VALUE).size() > 150, () -> String.valueOf(builtins.getNames(ShaderNameKind.VALUE).size()));
    }

    @Test
    public void builtinsResolve(Context context) {
        String text = "float4 f(float4 c, float4x4 m) { RayDesc ray; return saturate(mul(m, c)); }";
        PsiFile file = context.psiFileFactory().createFileFromText("test.hlsl", HlslLanguage.INSTANCE, text);
        for (String name : List.of("saturate", "mul", "RayDesc")) {
            PsiReference reference = file.findReferenceAt(text.indexOf(name));
            Assertions.assertNotNull(reference, name);
            PsiElement target = reference.resolve();
            Assertions.assertNotNull(target, name);
            Assertions.assertTrue(GeneratedDeclarationsFile.isBuiltin(target), name);
        }
        // a local declaration shadows the built-in
        String shadowed = "float lerp(float a) { return a; } float f() { return lerp(1); }";
        PsiFile local = context.psiFileFactory().createFileFromText("test.hlsl", HlslLanguage.INSTANCE, shadowed);
        PsiReference localReference = local.findReferenceAt(shadowed.lastIndexOf("lerp"));
        Assertions.assertNotNull(localReference);
        PsiElement target = localReference.resolve();
        Assertions.assertNotNull(target);
        Assertions.assertFalse(GeneratedDeclarationsFile.isBuiltin(target));
    }

    @Test
    public void builtinTypeMethods(Context context) {
        String text = "Texture2D<float4> t; SamplerState s; float4 f(float2 uv) { return t.Sample(s, uv); }";
        PsiFile file = context.psiFileFactory().createFileFromText("test.hlsl", HlslLanguage.INSTANCE, text);
        PsiReference reference = file.findReferenceAt(text.indexOf("Sample("));
        Assertions.assertNotNull(reference);
        PsiElement target = reference.resolve();
        Assertions.assertNotNull(target);
        Assertions.assertInstanceOf(ShaderFunction.class, target);
        Assertions.assertTrue(GeneratedDeclarationsFile.isBuiltin(target));
    }

    @Test
    public void expressionTypes(Context context) {
        String text = """
            struct Light { float3 dir; };
            Texture2D<half4> t; SamplerState s; StructuredBuffer<Light> lights; float4x4 m;
            float4 f(float2 uv, float3 a, float3 b) {
                float4 c;
                return float4(c.rgb.x, t.Sample(s, uv).rg.x, lights[0].dir.yz.x, m[1].w + lerp(a, b, 0.5).x + float2(1, 2).y);
            }
            """;
        PsiFile file = context.psiFileFactory().createFileFromText("test.hlsl", HlslLanguage.INSTANCE, text);
        assertType(file, text, "c.rgb", "float3");
        assertType(file, text, "t.Sample(s, uv)", "half4");
        assertType(file, text, "t.Sample(s, uv).rg", "half2");
        assertType(file, text, "lights[0]", "Light");
        assertType(file, text, "lights[0].dir.yz", "float2");
        assertType(file, text, "m[1]", "float4");
        assertType(file, text, "lerp(a, b, 0.5)", "float3");
        assertType(file, text, "float2(1, 2)", "float2");
    }

    private static void assertType(PsiFile file, String text, String expression, String expected) {
        int start = text.indexOf(expression);
        PsiElement element = file.findElementAt(start);
        while (element != null && !(element.getTextRange().getStartOffset() == start && element.getTextLength() == expression.length())) {
            element = element.getParent();
        }
        Assertions.assertNotNull(element, expression);
        ShaderType type = ShaderTypeEvaluator.typeOf(element);
        Assertions.assertNotNull(type, expression);
        Assertions.assertEquals(expected, type.name(), expression);
    }
}

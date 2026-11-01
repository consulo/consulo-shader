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
import consulo.language.psi.PsiNamedElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.shader.hlsl.HlslLanguage;
import consulo.shader.hlsl.impl.psi.HlslAdditiveExpr;
import consulo.shader.hlsl.impl.psi.HlslCastExpr;
import consulo.shader.hlsl.impl.psi.HlslDeclarator;
import consulo.shader.hlsl.impl.psi.HlslFunctionDeclaration;
import consulo.shader.hlsl.impl.psi.HlslIfStatement;
import consulo.shader.hlsl.impl.psi.HlslMacroDeclaration;
import consulo.shader.hlsl.impl.psi.HlslMacroStatement;
import consulo.shader.hlsl.impl.psi.HlslParameter;
import consulo.shader.hlsl.impl.psi.HlslRegisterBinding;
import consulo.shader.hlsl.impl.psi.HlslSemantic;
import consulo.shader.hlsl.impl.psi.HlslTemplateArgumentList;
import consulo.test.junit.impl.extension.ConsuloProjectLoader;
import consulo.test.junit.impl.extension.InjectingRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collection;
import java.util.List;

/**
 * The trees of the constructs the grammar has to disambiguate: casts, templates, semantics, macros, contextual keywords.
 */
@ExtendWith(ConsuloProjectLoader.class)
public class HlslParserTest {
    public record Context(PsiFileFactory psiFileFactory) implements InjectingRecord {
    }

    @Test
    public void functionWithSemantics(Context context) {
        PsiFile file = parse(context, "float4 main(float4 p : SV_Position) : SV_Target { return (float4) p; }");
        Assertions.assertEquals(List.of("main"), names(file, HlslFunctionDeclaration.class));
        Assertions.assertEquals(List.of("p"), names(file, HlslParameter.class));
        Assertions.assertEquals(2, PsiTreeUtil.findChildrenOfType(file, HlslSemantic.class).size());
        Assertions.assertEquals(1, PsiTreeUtil.findChildrenOfType(file, HlslCastExpr.class).size());
    }

    @Test
    public void parenthesizedNameIsNotACast(Context context) {
        PsiFile file = parse(context, "float f(float a, float b) { return (a) - b; }");
        Assertions.assertTrue(PsiTreeUtil.findChildrenOfType(file, HlslCastExpr.class).isEmpty());
        Assertions.assertEquals(1, PsiTreeUtil.findChildrenOfType(file, HlslAdditiveExpr.class).size());
    }

    @Test
    public void resources(Context context) {
        PsiFile file = parse(context, "Texture2D<float4> t : register(t0, space1); SamplerState s : register(s0);");
        Assertions.assertEquals(List.of("t", "s"), names(file, HlslDeclarator.class));
        Assertions.assertEquals(1, PsiTreeUtil.findChildrenOfType(file, HlslTemplateArgumentList.class).size());
        Assertions.assertEquals(2, PsiTreeUtil.findChildrenOfType(file, HlslRegisterBinding.class).size());
    }

    @Test
    public void unityMacros(Context context) {
        PsiFile file = parse(context, """
            CBUFFER_START(UnityPerMaterial)
                float4 _Color;
            CBUFFER_END
            struct v2f { float4 pos : SV_POSITION; UNITY_VERTEX_OUTPUT_STEREO };
            v2f vert(appdata v) {
                v2f o;
                UNITY_SETUP_INSTANCE_ID(v)
                UNITY_BRANCH if (o.pos.x > 0) discard;
                return o;
            }
            """);
        Assertions.assertEquals(3, PsiTreeUtil.findChildrenOfType(file, HlslMacroDeclaration.class).size());
        Assertions.assertEquals(List.of("_Color", "pos", "o"), names(file, HlslDeclarator.class));
        Assertions.assertEquals(1, PsiTreeUtil.findChildrenOfType(file, HlslMacroStatement.class).size());
        Assertions.assertEquals(1, PsiTreeUtil.findChildrenOfType(file, HlslIfStatement.class).size());
    }

    @Test
    public void contextualKeywordsAsNames(Context context) {
        PsiFile file = parse(context, """
            [maxvertexcount(3)]
            void gs(triangle float4 input[3] : SV_Position, inout TriangleStream<float4> output) {
                float4 sample = input[0];
                sample.x += 1;
                output.Append(sample);
            }
            """);
        Assertions.assertEquals(List.of("input", "output"), names(file, HlslParameter.class));
        Assertions.assertEquals(List.of("sample"), names(file, HlslDeclarator.class));
    }

    @Test
    public void methods(Context context) {
        PsiFile file = parse(context, "struct S { float f() { return 1; } float v; }; float S::g() { return 0; }");
        Assertions.assertEquals(List.of("f", "g"), names(file, HlslFunctionDeclaration.class));
        Assertions.assertEquals(List.of("v"), names(file, HlslDeclarator.class));
    }

    private static PsiFile parse(Context context, String text) {
        PsiFile file = context.psiFileFactory().createFileFromText("test.hlsl", HlslLanguage.INSTANCE, text);
        Collection<PsiErrorElement> errors = PsiTreeUtil.findChildrenOfType(file, PsiErrorElement.class);
        Assertions.assertTrue(errors.isEmpty(), () -> errors.stream().map(e -> e.getErrorDescriptionValue().get() + " at " + e.getTextOffset()).toList().toString());
        return file;
    }

    private static List<String> names(PsiElement root, Class<? extends PsiNamedElement> type) {
        return PsiTreeUtil.findChildrenOfType(root, type).stream().map(PsiNamedElement::getName).toList();
    }
}

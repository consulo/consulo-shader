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
package consulo.shader.hlsl.impl.resolve;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.shader.hlsl.HlslLanguage;
import consulo.shader.hlsl.impl.builtins.HlslBuiltins;
import consulo.shader.hlsl.impl.psi.HlslAnnotation;
import consulo.shader.hlsl.impl.psi.HlslAttribute;
import consulo.shader.hlsl.impl.psi.HlslMacroDeclaration;
import consulo.shader.hlsl.impl.psi.HlslSemantic;
import consulo.shader.hlsl.impl.psi.HlslStateBlock;
import consulo.shader.impl.ShaderLanguageSupport;
import consulo.shader.psi.ShaderNameKind;
import consulo.shader.psi.ShaderNamedElement;
import consulo.shader.psi.ShaderReferenceElement;

import java.util.List;

/**
 * HLSL built-ins for the shared resolve: the intrinsics, and the classes of built-in types ({@code Texture2D}).
 */
@ExtensionImpl
public class HlslShaderLanguageSupport implements ShaderLanguageSupport {
    @RequiredReadAction
    @Override
    public List<? extends ShaderNamedElement> findBuiltins(PsiElement place, String name, ShaderNameKind kind) {
        return HlslBuiltins.getFile(place.getProject()).findAll(name, kind);
    }

    /**
     * Names in attributes, semantics, annotations and effect state blocks, which name compile-time settings
     * ({@code [[vk::binding(0, 1)]]}, {@code AddressU = WRAP;}); variadic intrinsics ({@code printf}); and names a macro
     * call may declare ({@code DEFINE_TEXTURE(albedo)}).
     */
    @RequiredReadAction
    @Override
    public boolean isImplicitlyDeclared(PsiElement place, String name, ShaderNameKind kind) {
        if (PsiTreeUtil.getParentOfType(place, HlslAttribute.class, HlslSemantic.class, HlslAnnotation.class, HlslStateBlock.class) != null
            || HlslBuiltins.isVariadic(name)) {
            return true;
        }
        PsiFile file = place.getContainingFile();
        if (file == null) {
            return false;
        }
        for (HlslMacroDeclaration macro : PsiTreeUtil.findChildrenOfType(file, HlslMacroDeclaration.class)) {
            for (ShaderReferenceElement argument : PsiTreeUtil.findChildrenOfType(macro, ShaderReferenceElement.class)) {
                if (name.equals(argument.getReferenceNameElement().getText())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Language getLanguage() {
        return HlslLanguage.INSTANCE;
    }
}

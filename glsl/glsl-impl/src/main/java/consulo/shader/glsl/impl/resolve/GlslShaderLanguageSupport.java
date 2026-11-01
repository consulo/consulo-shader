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
package consulo.shader.glsl.impl.resolve;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.psi.PsiElement;
import consulo.shader.glsl.GlslLanguage;
import consulo.shader.glsl.impl.builtins.GlslBuiltins;
import consulo.shader.glsl.impl.godot.GodotBuiltinsFile;
import consulo.shader.glsl.impl.godot.GodotShaderContext;
import consulo.shader.impl.ShaderLanguageSupport;
import consulo.shader.impl.resolve.ShaderResolver;
import consulo.shader.psi.ShaderFunction;
import consulo.shader.psi.ShaderNameKind;
import consulo.shader.psi.ShaderNamedElement;

import java.util.List;
import java.util.regex.Pattern;

/**
 * GLSL built-ins for the shared resolve: those of the glslang table, or in a Godot shader those of Godot.
 */
@ExtensionImpl
public class GlslShaderLanguageSupport implements ShaderLanguageSupport {
    private static final Pattern VULKAN_TEXTURE = Pattern.compile("[iu]?texture(1D|2D|3D|Cube|2DRect|1DArray|2DArray|CubeArray|Buffer|2DMS|2DMSArray)");

    @RequiredReadAction
    @Override
    public List<? extends ShaderNamedElement> findBuiltins(PsiElement place, String name, ShaderNameKind kind) {
        return GodotShaderContext.of(place) != null
            ? GodotBuiltinsFile.getInstance(place.getProject()).findAll(name, kind)
            : GlslBuiltins.getFile(place.getProject()).findAll(name, kind);
    }

    @Override
    public Language getLanguage() {
        return GlslLanguage.INSTANCE;
    }

    /**
     * Names of extensions, which the built-ins leave out ({@code gl_ViewIndex} and the other {@code gl_} symbols of
     * extensions), and the separate texture types of Vulkan GLSL, which the lexer keeps as names because legacy GLSL has
     * functions with them ({@code texture2D}), and subroutine types.
     */
    @RequiredReadAction
    @Override
    public boolean isImplicitlyDeclared(PsiElement place, String name, ShaderNameKind kind) {
        if (name.startsWith("gl_") || kind == ShaderNameKind.TYPE && VULKAN_TEXTURE.matcher(name).matches()) {
            return true;
        }
        // a subroutine type is named by a function prototype: subroutine vec4 Shade(vec3 n); subroutine uniform Shade s;
        return kind == ShaderNameKind.TYPE && ShaderResolver.collectVisible(place, ShaderNameKind.VALUE).stream()
            .anyMatch(declaration -> declaration instanceof ShaderFunction && name.equals(declaration.getReferencedName()));
    }
}

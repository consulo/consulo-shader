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
package consulo.shader.glsl.impl.godot;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.shader.glsl.godot.model.GodotProcessingFunction;
import consulo.shader.glsl.godot.model.GodotShaderType;
import consulo.shader.glsl.impl.GlslParserDefinition;
import consulo.shader.glsl.impl.psi.GlslFunctionDeclaration;
import consulo.shader.glsl.impl.psi.GlslShaderTypeDeclaration;
import consulo.shader.glsl.impl.psi.GlslTypes;
import org.jspecify.annotations.Nullable;

/**
 * Where an element of a Godot shader is: the shader type the file declares and the processing function around it.
 */
public record GodotShaderContext(@Nullable GodotShaderType shaderType, @Nullable GodotProcessingFunction processingFunction) {
    /**
     * The context of {@code element}, or {@code null} when its file is not a Godot shader.
     */
    @RequiredReadAction
    public static @Nullable GodotShaderContext of(PsiElement element) {
        PsiFile file = element.getContainingFile();
        if (file == null || !GlslParserDefinition.isGodot(file.getLanguageVersion())) {
            return null;
        }
        GodotShaderType shaderType = shaderTypeOf(file);
        GlslFunctionDeclaration function = PsiTreeUtil.getParentOfType(element, GlslFunctionDeclaration.class);
        String functionName = function == null ? null : function.getName();
        GodotProcessingFunction processingFunction = functionName == null ? null
            : shaderType == null ? GodotProcessingFunction.findByName(functionName)
            : GodotProcessingFunction.findByName(shaderType, functionName);
        return new GodotShaderContext(shaderType, processingFunction);
    }

    /**
     * The {@code shader_type} a file declares; an include file ({@code .gdshaderinc}) declares none.
     */
    @RequiredReadAction
    public static @Nullable GodotShaderType shaderTypeOf(PsiFile file) {
        GlslShaderTypeDeclaration declaration = PsiTreeUtil.getChildOfType(file, GlslShaderTypeDeclaration.class);
        ASTNode name = declaration == null ? null : declaration.getNode().findChildByType(GlslTypes.IDENTIFIER);
        return name == null ? null : GodotShaderType.findById(name.getText());
    }
}

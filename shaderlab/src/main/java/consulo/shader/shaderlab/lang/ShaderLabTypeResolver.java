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
package consulo.shader.shaderlab.lang;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.GlobalSearchScope;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Resolves the script types a shader names: the {@code CustomEditor} class (a {@code UnityEditor.ShaderGUI}) and the
 * drawers behind material property attributes such as {@code [Toggle]}. ShaderLab itself knows no scripting language;
 * the plugin of the engine that compiles the shader implements this.
 */
@NullMarked
@ExtensionAPI(ComponentScope.PROJECT)
public interface ShaderLabTypeResolver {
    /**
     * The declaration of the type named {@code qualifiedName}, such as {@code UnityEditor.MaterialToggleDrawer}.
     */
    @Nullable PsiElement findType(String qualifiedName, GlobalSearchScope scope);

    /**
     * Offers the types that directly extend {@code baseQualifiedName}, each as a lookup string in double quotes.
     */
    void collectInheritors(String baseQualifiedName, Consumer<LookupElement> consumer);
}

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
package consulo.shader.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;
import consulo.application.Application;
import consulo.component.extension.ExtensionPointCacheKey;
import consulo.language.Language;
import consulo.language.extension.ByLanguageValue;
import consulo.language.extension.LanguageExtension;
import consulo.language.extension.LanguageOneToOne;
import consulo.language.psi.PsiElement;
import consulo.shader.psi.ShaderNameKind;
import consulo.shader.psi.ShaderNamedElement;

import java.util.List;

/**
 * What the shared resolve needs from a shader language: its built-in declarations. Resolve asks here for a name the
 * file does not declare, member access for the class of a built-in type ({@code Texture2D}).
 */
@ExtensionAPI(ComponentScope.APPLICATION)
public interface ShaderLanguageSupport extends LanguageExtension {
    ExtensionPointCacheKey<ShaderLanguageSupport, ByLanguageValue<ShaderLanguageSupport>> KEY =
        ExtensionPointCacheKey.create("ShaderLanguageSupport", LanguageOneToOne.build(new ShaderLanguageSupport() {
            @Override
            public Language getLanguage() {
                return Language.ANY;
            }
        }));

    static ShaderLanguageSupport forLanguage(Language language) {
        return Application.get().getExtensionPoint(ShaderLanguageSupport.class).getOrBuildCache(KEY).requiredGet(language);
    }

    /**
     * The built-in declarations of {@code kind} named {@code name} that are visible at {@code place}: every overload of
     * a function.
     */
    @RequiredReadAction
    default List<? extends ShaderNamedElement> findBuiltins(PsiElement place, String name, ShaderNameKind kind) {
        return List.of();
    }

    /**
     * Whether a name that resolves to nothing is still known to the language and must not be reported: a type of an
     * extension, a symbol only an extension or the preprocessor declares.
     */
    @RequiredReadAction
    default boolean isImplicitlyDeclared(PsiElement place, String name, ShaderNameKind kind) {
        return false;
    }
}

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
package consulo.shader.impl.findUsage;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.findUsage.FindUsagesProvider;
import consulo.language.psi.PsiElement;
import consulo.shader.impl.localize.ShaderImplLocalize;
import consulo.shader.psi.ShaderFunction;
import consulo.shader.psi.ShaderNamedElement;
import consulo.shader.psi.ShaderParameter;
import consulo.shader.psi.ShaderTypeDefinition;

/**
 * Find usages of the declarations of a shader language; a language adds its words scanner.
 */
public abstract class ShaderFindUsagesProvider implements FindUsagesProvider {
    @Override
    public boolean canFindUsagesFor(PsiElement element) {
        return element instanceof ShaderNamedElement && element.getLanguage() == getLanguage();
    }

    @Override
    public String getType(PsiElement element) {
        if (element instanceof ShaderFunction) {
            return ShaderImplLocalize.findUsagesFunction().get();
        }
        if (element instanceof ShaderParameter) {
            return ShaderImplLocalize.findUsagesParameter().get();
        }
        if (element instanceof ShaderTypeDefinition) {
            return ShaderImplLocalize.findUsagesType().get();
        }
        return ShaderImplLocalize.findUsagesVariable().get();
    }

    @RequiredReadAction
    @Override
    public String getDescriptiveName(PsiElement element) {
        String name = element instanceof ShaderNamedElement declaration ? declaration.getName() : null;
        return name == null ? "" : name;
    }

    @RequiredReadAction
    @Override
    public String getNodeText(PsiElement element, boolean useFullName) {
        return getDescriptiveName(element);
    }
}

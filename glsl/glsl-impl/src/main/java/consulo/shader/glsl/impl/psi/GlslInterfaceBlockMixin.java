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
package consulo.shader.glsl.impl.psi;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.ast.ASTNode;
import consulo.shader.psi.ShaderDeclarationContainer;
import consulo.shader.psi.ShaderDeclarationProvider;
import consulo.shader.psi.ShaderNameKind;
import consulo.shader.psi.ShaderNamedElement;
import org.jspecify.annotations.Nullable;

import java.util.function.Predicate;

/**
 * The {@code mixin} of interface blocks. References name a block by its instance name; without one, its members are
 * globals.
 */
public abstract class GlslInterfaceBlockMixin extends GlslNamedElementMixin implements ShaderDeclarationProvider {
    protected GlslInterfaceBlockMixin(ASTNode node) {
        super(node);
    }

    @RequiredReadAction
    @Override
    public @Nullable String getReferencedName() {
        ASTNode[] identifiers = getNode().getChildren(NAME_TOKENS);
        return identifiers.length < 2 ? null : identifiers[1].getText();
    }

    @RequiredReadAction
    @Override
    public boolean processShaderDeclarations(ShaderNameKind kind, Predicate<? super ShaderNamedElement> processor) {
        if (kind != ShaderNameKind.VALUE) {
            return true;
        }
        return getReferencedName() != null ? processor.test(this) : ShaderDeclarationContainer.processContents(this, kind, processor);
    }
}

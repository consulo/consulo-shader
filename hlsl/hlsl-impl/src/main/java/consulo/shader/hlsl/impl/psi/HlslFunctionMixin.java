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
package consulo.shader.hlsl.impl.psi;

import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.shader.impl.psi.ShaderFunctionBase;

/**
 * The {@code mixin} of HLSL function definitions, prototypes and methods; of {@code float S::f()} the name is {@code f}.
 */
public abstract class HlslFunctionMixin extends ShaderFunctionBase {
    protected HlslFunctionMixin(ASTNode node) {
        super(node);
    }

    @Override
    protected TokenSet getNameTokens() {
        return HlslNamedElementMixin.NAME_TOKENS;
    }

    @Override
    protected IElementType getQualifierSeparator() {
        return HlslTypes.COLON_COLON;
    }
}

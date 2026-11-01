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
import consulo.shader.impl.psi.ShaderNamedElementBase;

/**
 * The {@code mixin} of named HLSL declarations. Contextual qualifiers ({@code sample}, {@code line}) are names too.
 */
public abstract class HlslNamedElementMixin extends ShaderNamedElementBase {
    // a type token names only the classes of built-in types in the generated built-ins; everywhere else types are in
    // type specifiers, not direct children
    static final TokenSet NAME_TOKENS = TokenSet.create(HlslTypes.IDENTIFIER, HlslTypes.SOFT_QUALIFIER, HlslTypes.TYPE);

    protected HlslNamedElementMixin(ASTNode node) {
        super(node);
    }

    @Override
    protected TokenSet getNameTokens() {
        return NAME_TOKENS;
    }

    @Override
    protected IElementType getQualifierSeparator() {
        return HlslTypes.COLON_COLON;
    }
}

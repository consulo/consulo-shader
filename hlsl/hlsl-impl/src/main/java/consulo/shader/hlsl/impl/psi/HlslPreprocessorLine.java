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

import consulo.language.ast.IElementType;
import consulo.language.impl.psi.LazyParseablePsiElement;
import consulo.language.psi.PsiComment;
import org.jspecify.annotations.Nullable;

/**
 * A preprocessor line: a comment to the grammar, with its own small tree, parsed on demand. It is both the lazy AST
 * node and its PSI, which {@link HlslPreprocessorLineElementType#createNode} creates; lazy nodes do not get their PSI
 * from the parser definition.
 */
public class HlslPreprocessorLine extends LazyParseablePsiElement implements PsiComment {
    public HlslPreprocessorLine(IElementType type, @Nullable CharSequence text) {
        super(type, text);
    }

    @Override
    public IElementType getTokenType() {
        return getElementType();
    }
}

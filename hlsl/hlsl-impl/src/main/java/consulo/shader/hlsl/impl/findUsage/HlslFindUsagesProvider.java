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
package consulo.shader.hlsl.impl.findUsage;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.ast.TokenSet;
import consulo.language.cacheBuilder.DefaultWordsScanner;
import consulo.language.cacheBuilder.WordsScanner;
import consulo.shader.hlsl.HlslLanguage;
import consulo.shader.hlsl.impl.lexer.HlslTokens;
import consulo.shader.hlsl.impl.lexer._HlslLexer;
import consulo.shader.hlsl.impl.psi.HlslTypes;
import consulo.shader.impl.findUsage.ShaderFindUsagesProvider;
import org.jspecify.annotations.Nullable;

@ExtensionImpl
public class HlslFindUsagesProvider extends ShaderFindUsagesProvider {
    @Override
    public @Nullable WordsScanner getWordsScanner() {
        return new DefaultWordsScanner(new _HlslLexer(), TokenSet.create(HlslTypes.IDENTIFIER, HlslTypes.SOFT_QUALIFIER),
            HlslTokens.COMMENTS, TokenSet.create(HlslTypes.STRING_LITERAL));
    }

    @Override
    public Language getLanguage() {
        return HlslLanguage.INSTANCE;
    }
}

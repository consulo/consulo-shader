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

import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.ast.ILazyParseableElementType;
import consulo.language.parser.PsiBuilder;
import consulo.language.parser.PsiBuilderFactory;
import consulo.language.psi.PsiElement;
import consulo.language.version.LanguageVersion;
import consulo.shader.hlsl.HlslLanguage;
import consulo.shader.hlsl.impl.lexer.HlslTokens;
import consulo.shader.hlsl.impl.lexer._HlslDirectiveLexer;
import consulo.shader.impl.parser.ShaderIncludeParsing;

import java.util.Objects;

/**
 * A preprocessor line. To the grammar it is a comment; its own contents are parsed on demand, so that the path of an
 * {@code #include} becomes a reference.
 */
public class HlslPreprocessorLineElementType extends ILazyParseableElementType {
    public HlslPreprocessorLineElementType() {
        super("HLSL_PREPROCESSOR_LINE", HlslLanguage.INSTANCE);
    }

    @Override
    public ASTNode createNode(CharSequence text) {
        return new HlslPreprocessorLine(this, text);
    }

    @Override
    protected ASTNode doParseContents(ASTNode chameleon, PsiElement psi) {
        Language language = getLanguageForParser(psi);
        LanguageVersion storedVersion = chameleon.getUserData(LanguageVersion.KEY);
        LanguageVersion version = storedVersion == null ? psi.getLanguageVersion() : storedVersion;
        PsiBuilder builder = PsiBuilderFactory.getInstance()
            .createBuilder(psi.getProject(), chameleon, new _HlslDirectiveLexer(), language, version, chameleon.getChars());
        PsiBuilder.Marker root = builder.mark();
        while (!builder.eof()) {
            if (!ShaderIncludeParsing.parseInclude(builder, HlslTokens.PREPROCESSOR_DIRECTIVE, HlslTokens.INCLUDE_PATH,
                HlslTokens.INCLUDE_DIRECTIVE, HlslTokens.INCLUDE_PATH_ELEMENT)) {
                builder.advanceLexer();
            }
        }
        root.done(this);
        // a preprocessor line always has at least its "#"
        return Objects.requireNonNull(builder.getTreeBuilt().getFirstChildNode());
    }
}

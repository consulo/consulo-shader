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

import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.ast.ILazyParseableElementType;
import consulo.language.parser.PsiBuilder;
import consulo.language.parser.PsiBuilderFactory;
import consulo.language.psi.PsiElement;
import consulo.language.version.LanguageVersion;
import consulo.shader.glsl.GlslLanguage;
import consulo.shader.glsl.impl.lexer.GlslTokens;
import consulo.shader.glsl.impl.lexer._GlslDirectiveLexer;
import consulo.shader.impl.parser.ShaderIncludeParsing;

import java.util.Objects;

/**
 * A preprocessor line. To the grammar it is a comment; its own contents are parsed on demand, so that the path of an
 * {@code #include} becomes a reference.
 */
public class GlslPreprocessorLineElementType extends ILazyParseableElementType {
    public GlslPreprocessorLineElementType() {
        super("GLSL_PREPROCESSOR_LINE", GlslLanguage.INSTANCE);
    }

    @Override
    public ASTNode createNode(CharSequence text) {
        return new GlslPreprocessorLine(this, text);
    }

    @Override
    protected ASTNode doParseContents(ASTNode chameleon, PsiElement psi) {
        Language language = getLanguageForParser(psi);
        LanguageVersion storedVersion = chameleon.getUserData(LanguageVersion.KEY);
        LanguageVersion version = storedVersion == null ? psi.getLanguageVersion() : storedVersion;
        PsiBuilder builder = PsiBuilderFactory.getInstance()
            .createBuilder(psi.getProject(), chameleon, new _GlslDirectiveLexer(), language, version, chameleon.getChars());
        PsiBuilder.Marker root = builder.mark();
        while (!builder.eof()) {
            if (!ShaderIncludeParsing.parseInclude(builder, GlslTokens.PREPROCESSOR_DIRECTIVE, GlslTokens.INCLUDE_PATH,
                GlslTokens.INCLUDE_DIRECTIVE, GlslTokens.INCLUDE_PATH_ELEMENT)) {
                builder.advanceLexer();
            }
        }
        root.done(this);
        // a preprocessor line always has at least its "#"
        return Objects.requireNonNull(builder.getTreeBuilt().getFirstChildNode());
    }
}

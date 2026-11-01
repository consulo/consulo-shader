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
package consulo.shader.impl.psi;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.content.scope.SearchScope;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.impl.ast.LeafElement;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import consulo.language.psi.PsiComment;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiWhiteSpace;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.scope.LocalSearchScope;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.util.IncorrectOperationException;
import consulo.shader.impl.resolve.ShaderScopes;
import consulo.shader.psi.ShaderFunction;
import consulo.shader.psi.ShaderNamedElement;
import consulo.shader.psi.ShaderUnorderedScope;
import org.jspecify.annotations.Nullable;

/**
 * The base of named declarations of generated PSI (a Grammar-Kit {@code mixin}). The name is the first name token among
 * the direct children, since types, qualifiers and bases are child elements; of a qualified name ({@code S::f}) the
 * last part.
 */
public abstract class ShaderNamedElementBase extends ASTWrapperPsiElement implements ShaderNamedElement {
    protected ShaderNamedElementBase(ASTNode node) {
        super(node);
    }

    /**
     * The tokens that can be a name.
     */
    protected abstract TokenSet getNameTokens();

    /**
     * The separator of qualified names, or {@code null} for a language without them.
     */
    protected @Nullable IElementType getQualifierSeparator() {
        return null;
    }

    @RequiredReadAction
    @Override
    public @Nullable PsiElement getNameIdentifier() {
        ASTNode name = getNode().findChildByType(getNameTokens());
        IElementType separator = getQualifierSeparator();
        while (name != null && separator != null) {
            ASTNode next = skipSpace(name.getTreeNext());
            if (next == null || next.getElementType() != separator) {
                break;
            }
            ASTNode part = skipSpace(next.getTreeNext());
            if (part == null || !getNameTokens().contains(part.getElementType())) {
                break;
            }
            name = part;
        }
        return name == null ? null : name.getPsi();
    }

    @RequiredReadAction
    @Override
    public @Nullable String getName() {
        PsiElement identifier = getNameIdentifier();
        return identifier == null ? null : identifier.getText();
    }

    @RequiredWriteAction
    @Override
    public PsiElement setName(String name) throws IncorrectOperationException {
        PsiElement identifier = getNameIdentifier();
        if (identifier == null || !(identifier.getNode() instanceof LeafElement leaf)) {
            throw new IncorrectOperationException("No name to rename");
        }
        leaf.replaceWithText(name);
        return this;
    }

    @RequiredReadAction
    @Override
    public int getTextOffset() {
        PsiElement identifier = getNameIdentifier();
        return identifier == null ? super.getTextOffset() : identifier.getTextRange().getStartOffset();
    }

    /**
     * A struct with methods or a namespace offers its members to the code inside it.
     */
    @RequiredReadAction
    @Override
    public boolean processDeclarations(PsiScopeProcessor processor, ResolveState state, @Nullable PsiElement lastParent, PsiElement place) {
        return !(this instanceof ShaderUnorderedScope) || ShaderScopes.processUnorderedScope(this, processor, state, lastParent);
    }

    /**
     * A local or parameter is used only in its function.
     */
    @RequiredReadAction
    @Override
    public SearchScope getUseScope() {
        ShaderFunction owner = ShaderScopes.localOwner(this);
        return owner != null ? new LocalSearchScope(owner) : super.getUseScope();
    }

    private static @Nullable ASTNode skipSpace(@Nullable ASTNode node) {
        while (node != null && (node.getPsi() instanceof PsiWhiteSpace || node.getPsi() instanceof PsiComment)) {
            node = node.getTreeNext();
        }
        return node;
    }
}

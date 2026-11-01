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
package consulo.shader.impl.resolve;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import consulo.shader.psi.ShaderFunction;
import consulo.shader.psi.ShaderNameKind;
import consulo.shader.psi.ShaderNamedElement;
import consulo.util.dataholder.Key;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Collects the declarations of one name space that a tree walk offers. With a name it keeps every function of that name
 * (overloads, and prototypes whose definition may come later) and stops at the first variable or type; scopes offer the
 * nearest declarations first, so that one shadows the others. Without a name (completion) it
 * keeps the first declaration of every name and never stops.
 */
public class ShaderResolveProcessor implements PsiScopeProcessor {
    private final @Nullable String myName;
    private final ShaderNameKind myKind;
    private final List<ShaderNamedElement> myResults = new ArrayList<>();
    private final Set<String> myNames = new HashSet<>();

    public ShaderResolveProcessor(@Nullable String name, ShaderNameKind kind) {
        myName = name;
        myKind = kind;
    }

    @RequiredReadAction
    @Override
    public boolean execute(PsiElement element, ResolveState state) {
        if (!(element instanceof ShaderNamedElement declaration)) {
            return true;
        }
        String name = declaration.getReferencedName();
        if (name == null) {
            return true;
        }
        if (myName == null) {
            if (myNames.add(name)) {
                myResults.add(declaration);
            }
            return true;
        }
        if (!myName.equals(name)) {
            return true;
        }
        if (declaration instanceof ShaderFunction) {
            // collect every overload
            myResults.add(declaration);
            return true;
        }
        // a variable or type shadows what is further out, but not the functions already found
        if (myResults.isEmpty()) {
            myResults.add(declaration);
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> @Nullable T getHint(Key<T> hintKey) {
        if (hintKey == ShaderResolveHints.KIND) {
            return (T) myKind;
        }
        if (hintKey == ShaderResolveHints.NAME) {
            return (T) myName;
        }
        return null;
    }

    /**
     * The declarations found, nearest first.
     */
    public List<ShaderNamedElement> getResults() {
        return myResults;
    }

}

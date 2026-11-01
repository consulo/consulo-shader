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
package consulo.shader.psi;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.psi.PsiElement;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * A call: {@code f(x)}, {@code float4(0, 0, 0, 1)}, {@code t.Sample(s, uv)}.
 */
public interface ShaderCallExpression extends PsiElement {
    @RequiredReadAction
    default @Nullable PsiElement getCallee() {
        return ShaderPsiUtil.firstCompositeChild(this);
    }

    /**
     * The arguments, in the wrappers the grammar gives them (initializers).
     */
    @RequiredReadAction
    default List<PsiElement> getArguments() {
        List<PsiElement> children = ShaderPsiUtil.compositeChildren(this);
        return children.size() < 2 ? List.of() : ShaderPsiUtil.compositeChildren(children.get(children.size() - 1));
    }
}

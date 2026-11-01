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

import consulo.language.psi.PsiElement;

/**
 * The type of a constructor call: the {@code float4} of {@code float4(0, 0, 0, 1)}. It holds a type token, then
 * template arguments and array specifiers, like a type specifier.
 */
public interface ShaderConstructorExpression extends PsiElement {
}

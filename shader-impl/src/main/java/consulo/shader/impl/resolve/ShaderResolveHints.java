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

import consulo.shader.psi.ShaderNameKind;
import consulo.util.dataholder.Key;

/**
 * What a shader resolve processor looks for, so that scopes can skip what it does not want.
 */
public interface ShaderResolveHints {
    /**
     * The name space looked in; without it, both.
     */
    Key<ShaderNameKind> KIND = Key.create("ShaderResolveHints.KIND");

    /**
     * The name looked for; absent when completing.
     */
    Key<String> NAME = Key.create("ShaderResolveHints.NAME");
}

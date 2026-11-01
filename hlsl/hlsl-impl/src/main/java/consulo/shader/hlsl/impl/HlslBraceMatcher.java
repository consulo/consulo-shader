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
package consulo.shader.hlsl.impl;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.BracePair;
import consulo.language.Language;
import consulo.language.PairedBraceMatcher;
import consulo.shader.hlsl.HlslLanguage;
import consulo.shader.hlsl.impl.psi.HlslTypes;

@ExtensionImpl
public class HlslBraceMatcher implements PairedBraceMatcher {
    private static final BracePair[] PAIRS = {
        new BracePair(HlslTypes.LEFT_BRACE, HlslTypes.RIGHT_BRACE, true),
        new BracePair(HlslTypes.LEFT_PAREN, HlslTypes.RIGHT_PAREN, false),
        new BracePair(HlslTypes.LEFT_BRACKET, HlslTypes.RIGHT_BRACKET, false),
    };

    @Override
    public BracePair[] getPairs() {
        return PAIRS;
    }

    @Override
    public Language getLanguage() {
        return HlslLanguage.INSTANCE;
    }
}

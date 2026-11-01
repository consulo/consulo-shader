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
package consulo.shader.glsl.impl.version;

import consulo.annotation.component.ExtensionImpl;
import consulo.shader.glsl.GlslDialect;
import consulo.shader.glsl.GlslLanguageVersion;

/**
 * Desktop OpenGL GLSL, the dialect of any GLSL file no other dialect claims.
 */
@ExtensionImpl
public class GlslOpenGlLanguageVersion extends GlslLanguageVersion {
    public GlslOpenGlLanguageVersion() {
        super("GLSL_OPENGL", "OpenGL", GlslDialect.OPENGL);
    }
}

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
package consulo.shader.hlsl;

import consulo.annotation.component.ExtensionImpl;
import consulo.virtualFileSystem.fileType.FileTypeConsumer;
import consulo.virtualFileSystem.fileType.FileTypeFactory;

/**
 * Registers HLSL and its relatives: Direct3D effect files ({@code .fx}/{@code .fxh}), Cg ({@code .cg}/{@code .cginc})
 * and Unreal Engine shaders ({@code .usf}/{@code .ush}).
 */
@ExtensionImpl
public class HlslFileTypeFactory extends FileTypeFactory {
    @Override
    public void createFileTypes(FileTypeConsumer consumer) {
        consumer.consume(HlslFileType.INSTANCE, "hlsl;hlsli;fx;fxh;cg;cginc;usf;ush");
    }
}

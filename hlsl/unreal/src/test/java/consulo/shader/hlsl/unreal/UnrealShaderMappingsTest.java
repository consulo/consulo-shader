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
package consulo.shader.hlsl.unreal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Virtual shader paths of a project inside a source build, on a small engine and project tree written to a temporary
 * directory: the engine's own mapping, a plugin's, and the one a project module adds in C++.
 */
public class UnrealShaderMappingsTest {
    @Test
    public void projectInsideSourceBuild(@TempDir Path root) throws IOException {
        write(root.resolve("Engine/Build/Build.version"), "{ \"MajorVersion\": 5, \"MinorVersion\": 7 }");
        Path common = write(root.resolve("Engine/Shaders/Private/Common.ush"), "// common");
        Path niagara = write(root.resolve("Engine/Plugins/FX/Niagara/Shaders/Private/NiagaraCommon.ush"), "// niagara");
        write(root.resolve("Engine/Plugins/FX/Niagara/Niagara.uplugin"), "{}");
        write(root.resolve("Engine/Plugins/FX/Niagara/Source/Niagara/Private/NiagaraModule.cpp"),
            "AddShaderSourceDirectoryMapping(TEXT(\"/Plugin/FX/Niagara\"), PluginShaderDir);");
        Path game = root.resolve("Game");
        write(game.resolve("Game.uproject"), "{ \"EngineAssociation\": \"\" }");
        Path palette = write(game.resolve("Shaders/Private/Palette.ush"), "// palette");
        write(game.resolve("Shaders/Private/Pulse.usf"), "#include \"/Game/Private/Palette.ush\"");
        write(game.resolve("Source/Game/Private/GameModule.cpp"),
            "AddShaderSourceDirectoryMapping(TEXT(\"/Game\"), FPaths::Combine(FPaths::ProjectDir(), TEXT(\"Shaders\")));");

        UnrealShaderIncludeResolver.Key key = UnrealShaderIncludeResolver.keyOf(game.resolve("Shaders/Private")).orElseThrow();
        Assertions.assertEquals(root, key.engineRoot());
        Assertions.assertEquals(game, key.projectDirectory());

        UnrealShaderMappings mappings = UnrealShaderMappings.discover(key.engineRoot(), key.projectDirectory());
        Assertions.assertEquals(common, mappings.resolve("/Engine/Private/Common.ush"));
        Assertions.assertEquals(niagara, mappings.resolve("/Plugin/FX/Niagara/Private/NiagaraCommon.ush"));
        Assertions.assertEquals(palette, mappings.resolve("/Game/Private/Palette.ush"));
        Assertions.assertNull(mappings.resolve("/Engine/Private/Missing.ush"));
        Assertions.assertTrue(UnrealShaderMappings.isGenerated("/Engine/Generated/Material.ush"));
    }

    private static Path write(Path file, String text) throws IOException {
        Files.createDirectories(Objects.requireNonNull(file.getParent()));
        Files.writeString(file, text);
        return file;
    }
}

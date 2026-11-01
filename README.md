# Shader language support for Consulo

One source tree for every shader language. Each language is its own set of modules. The C-style preprocessor, the
pipeline stage model and the editor infrastructure are shared.

| Module | Artifact | Contents |
|---|---|---|
| `preprocessor-api`, `preprocessor-impl` | `consulo.shader.preprocessor.*` | Language-independent C-style preprocessor: macro table, expansion, `#if` evaluation, include walking |
| `shader-api`, `shader-impl` | `consulo.shader.api`, `consulo.shader.impl` | Pipeline stages, include resolution, and what all shader languages share: PSI interfaces (`consulo.shader.psi`: functions, variables, types, scopes, references), one name resolver, semantic highlighting, find usages and generated built-in files |
| `glsl/glsl-api`, `glsl/glsl-impl` | `consulo.shader.glsl.*` | GLSL: one grammar (Grammar-Kit, `Glsl.bnf`) for all dialects, which are language versions. Lexer for GLSL 4.60, ES 3.20, Vulkan and common extensions; highlighting, keyword and type completion |
| `glsl/godot` | `consulo.shader.glsl.godot` | The Godot shading language as a GLSL dialect: `.gdshader`/`.gdshaderinc`, `shader_type`, `render_mode`, `stencil_mode`, uniform hints, `res://` includes |
| `hlsl/hlsl-api`, `hlsl/hlsl-impl` | `consulo.shader.hlsl.*` | HLSL, Cg (`.cg`, `.cginc`) and Unreal shaders (`.usf`, `.ush`). One grammar (Grammar-Kit, `Hlsl.bnf`) for HLSL, the effect framework and Cg; highlighting, keyword and intrinsic completion |
| `shaderlab` | `consulo.shader.shaderlab` | Unity ShaderLab, moved from consulo-unity3d. `CGPROGRAM` and `HLSLPROGRAM` blocks are HLSL. Script types (`CustomEditor`, property drawers) are resolved through `ShaderLabTypeResolver`, implemented by the Unity plugin |
| `plugin` | `consulo.shader` | The installable plugin, bundling all of the above |

Planned: OpenGL ES and Vulkan GLSL dialects, HLSL dialects (Cg, AZSL, Unreal), the shared preprocessor, member and
type resolution.

## Status

Early. GLSL and Godot shaders have a full parser, tested on a corpus (`mvn test`), and name resolution (go to
declaration, find usages, rename) for variables, parameters, functions, structs and interface blocks. Godot files complete
the built-ins of their shader type and processing function, render and stencil modes and uniform hints; built-ins
resolve into a generated read-only `godot_builtins.gdshader` (signatures only). GLSL files complete and resolve the
built-in variables and functions of GLSL 4.60, ES 3.20 and Vulkan, filtered by the stage of the file extension, into a
read-only `glsl_builtins.glsl`; the table is generated from glslang by `tools/glsl-builtins/generate.py`. Names are colored by kind (parameter,
local, global, function, built-in). HLSL has a full parser (Shader Model 5 and 6, HLSL 2021 templates, effect
techniques and state objects, Cg), tested on the HLSL tests of glslang and the Unity shaders of resharper-unity; it
tolerates common unexpanded macros (`CBUFFER_START(Name)`, `UNITY_BRANCH if`). HLSL has the same name resolution as GLSL (the shared resolver), plus `cbuffer` members, struct methods seeing their fields, `S::f` definitions, typedefs, enums and declarations in `if` conditions. HLSL intrinsics (`mul`, `saturate`, `tex2D`, wave and ray tracing functions, `RayDesc`) complete and resolve into a read-only `hlsl_builtins.hlsl`, generated from the MIT `builtins.hlsl` of Rider-HLSL-Plugin-Continue plus our `supplement.hlsl`. In both languages member access resolves through the shared type evaluator: struct fields and methods, interface block members, `buffer[i].field`, `tex.Sample(...)` into the built-in `Texture2D`, `gl_in[i].gl_Position`; swizzles (`.xyz`, `.rgb`) are typed but have no declaration. Resolve follows the Consulo resolve principles (consulo-key-principles): scopes declare through `processDeclarations`, references are cached poly-variant references with overloads chosen by argument count and types, locals rename in place, and names that resolve to nothing are reported, except where includes or the preprocessor may declare them; includes are not searched yet. `CGPROGRAM`/`HLSLPROGRAM` blocks are injected as HLSL, `GLSLPROGRAM` blocks as GLSL. ShaderLab has its full parser, as in consulo-unity3d; its packages are still `@NullUnmarked` until they get
nullness annotations.

consulo-unity3d still ships its own `cg-shader` and `shaderlab` modules. Installing both plugins registers `.shader` and
`.cginc` twice, until consulo-unity3d drops its copies and depends on this plugin.

## Include resolution

`#include` paths are references (Ctrl+Click) in GLSL and HLSL, also inside ShaderLab program blocks. A quoted relative
path is looked up next to the including file. Everything else goes through the `ShaderIncludeResolver` extension point
(`consulo.shader.include`), which engine plugins implement for their include roots and virtual paths. Built in:

- Minecraft shader packs: `/lib/x.glsl` is relative to the pack's `shaders` directory
- Root-relative paths (`/common/x.glsl`) against the source root, then the content root, of the including file
- Unreal Engine shaders (`.usf`, `.ush`): virtual paths (`/Engine/Private/Common.ush`, `/Plugin/FX/Niagara/...`, a
  project's own `/MyGame/...`) through the shader directory mappings of the project's engine (module `hlsl/unreal`; it
  only reads the project and engine, there is no Unreal project support)

## Supported file extensions

GLSL: `glsl`, `vert`, `tesc`, `tese`, `geom`, `frag`, `comp`, `rgen`, `rint`, `rahit`, `rchit`, `rmiss`, `rcall`,
`vsh`, `fsh`, `gsh`. HLSL: `hlsl`, `hlsli`, `fx`, `fxh`, `cg`, `cginc`, `usf`, `ush`. Godot: `gdshader`, `gdshaderinc`. ShaderLab: `shader`. Ambiguous extensions such as `csh` (also C shell), `mesh` and `task` are not claimed globally.

## License

Apache License 2.0, see [LICENSE.txt](LICENSE.txt). Every file in this repository must be available under it.
Code or data from other Apache 2.0, MIT or BSD projects keeps its notices and is credited in [NOTICE.txt](NOTICE.txt).
Code under other licenses, such as the LGPL glsl4idea plugin, must not be copied in.

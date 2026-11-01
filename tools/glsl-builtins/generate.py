#!/usr/bin/env python3
# Copyright 2013-2026 consulo.io
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
"""
Generates glsl/glsl-impl/src/main/resources/consulo/shader/glsl/impl/builtins/glsl-builtins.txt, the built-in variables
and functions of GLSL, from the built-in symbol table of glslang (https://github.com/KhronosGroup/glslang, 3-Clause BSD,
see NOTICE.txt): `glslangValidator --dump-builtin-symbols` for every stage of desktop OpenGL 4.60, OpenGL ES 3.20 and
Vulkan. Only core symbols are kept: anything glslang tags with an extension, and the explicit arithmetic types
(float16_t, int8_t, ... int64_t), are left out.

Usage: tools/glsl-builtins/generate.py [path to glslangValidator]
"""
import os
import re
import subprocess
import sys
import tempfile

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
OUTPUT = os.path.join(ROOT, "glsl/glsl-impl/src/main/resources/consulo/shader/glsl/impl/builtins/glsl-builtins.txt")

TARGETS = [
    ("gl", "#version 460 core", []),
    ("es", "#version 320 es", []),
    ("vulkan", "#version 460", ["-V", "--target-env", "vulkan1.3"]),
]
STAGES = ["vert", "tesc", "tese", "geom", "frag", "comp"]
ES_STAGES = ["vert", "tesc", "tese", "geom", "frag", "comp"]

QUALIFIERS = {"global", "const", "in", "out", "inout", "smooth", "flat", "noperspective", "centroid", "sample", "patch",
              "uniform", "temp", "coherent", "volatile", "nontemporal", "restrict", "readonly", "writeonly",
              "devicecoherent", "queuefamilycoherent", "workgroupcoherent", "subgroupcoherent", "nonprivate",
              "highp", "mediump", "lowp", "invariant", "precise", "buffer", "shared"}
PARAMETER_QUALIFIERS = {"in", "out", "inout"}

# the explicit arithmetic types and their vectors: extension-only, kept out
EXTENSION_TYPES = re.compile(r"(float16_t|int8_t|uint8_t|int16_t|uint16_t|int64_t|uint64_t|float64_t|\bf16|\bi8|\bu8|\bi16|\bu16|\bi64|\bu64)")

# a GLSL type name, as convert_type produces it
GLSL_TYPE = re.compile(r"^(void|bool|int|uint|float|double|atomic_uint|[biud]?vec[234]|d?mat[234](x[234])?"
                       r"|[iu]?(sampler|image|texture)\w*|sampler(Shadow)?|[iu]?subpassInput(MS)?|accelerationStructureEXT|rayQueryEXT"
                       r"|gl_PerVertex)(\[\d*\])?$")

SCALAR_PREFIX = {"float": "", "double": "d", "int": "i", "uint": "u", "bool": "b"}

# members of the gl_PerVertex block, which glslang prints only as "block"
PER_VERTEX_OUT = [("vec4", "gl_Position"), ("float", "gl_PointSize"), ("float[]", "gl_ClipDistance"), ("float[]", "gl_CullDistance")]
PER_VERTEX_OUT_STAGES = {"vert", "tesc", "tese", "geom"}
PER_VERTEX_IN_STAGES = {"tesc", "tese", "geom"}


def dump(validator, target, version, flags, stage):
    with tempfile.TemporaryDirectory() as directory:
        path = os.path.join(directory, "builtins." + stage)
        with open(path, "w") as file:
            file.write(version + "\nvoid main() {}\n")
        # run in the temporary directory: with -V glslang also writes <stage>.spv into the working directory
        result = subprocess.run([validator, *flags, "--dump-builtin-symbols", path], capture_output=True, text=True, cwd=directory)
        # with -V the table goes to stderr
        return result.stdout + "\n" + result.stderr


def convert_type(words):
    """
    Converts glslang's description of a type ("4-component vector of float", "unsized 1-element array of float",
    "3X3 matrix of float", "sampler2D") into GLSL, or None when it has no plain GLSL spelling.
    """
    text = " ".join(words)
    array = ""
    match = re.match(r"^unsized \d+-element array of (.*)$", text)
    if match:
        array, text = "[]", match.group(1)
    match = re.match(r"^(\d+)-element array of (.*)$", text)
    if match:
        array, text = "[" + match.group(1) + "]", match.group(2)
    match = re.match(r"^(\d)-component vector of (\w+)$", text)
    if match:
        prefix = SCALAR_PREFIX.get(match.group(2))
        return None if prefix is None else prefix + "vec" + match.group(1) + array
    match = re.match(r"^(\d)X(\d) matrix of (\w+)$", text)
    if match:
        columns, rows, scalar = match.groups()
        if scalar not in ("float", "double"):
            return None
        name = ("d" if scalar == "double" else "") + "mat" + (columns if columns == rows else columns + "x" + rows)
        return name + array
    if re.match(r"^\w+$", text) and GLSL_TYPE.match(text + array):
        return text + array
    return None


def split_type(words):
    """
    The type words of a declaration, without its leading qualifiers and its trailing built-in name.
    """
    while words and words[0] in QUALIFIERS:
        words = words[1:]
    return words


def parse_variable(name, rest):
    words = rest.split()
    # glslang may repeat the name, in its own casing (gl_VertexID: gl_VertexId int VertexId)
    while words and words[0].startswith("gl_"):
        words = words[1:]
    words = split_type(words)
    # the last word is glslang's built-in kind (FragCoord) unless the type ends the description
    for end in range(len(words), 0, -1):
        converted = convert_type(words[:end])
        if converted is not None:
            return converted
    return None


def parse_function(name, rest):
    match = re.match(r"^(.*?)\b" + re.escape(name) + r"\(\s*(.*?)\s*\)\s*$", rest)
    if not match:
        return None
    return_type = convert_type(split_type(match.group(1).split()))
    if return_type is None:
        return None
    parameters = []
    for parameter in [p.strip() for p in match.group(2).split(",")] if match.group(2).strip() else []:
        words = parameter.split()
        qualifier = words[0] if words and words[0] in PARAMETER_QUALIFIERS else "in"
        words = split_type(words)
        converted = convert_type(words) or (convert_type(words[:-1]) if len(words) > 1 else None)
        if converted is None:
            return None
        parameters.append(converted if qualifier == "in" else qualifier + " " + converted)
    return return_type, parameters


def collect(validator):
    """
    {(kind, signature): {target: set of stages}} over every target and stage.
    """
    symbols = {}
    for target, version, flags in TARGETS:
        for stage in (ES_STAGES if target == "es" else STAGES):
            for line in dump(validator, target, version, flags, stage).splitlines():
                match = re.match(r"^([A-Za-z_][A-Za-z_0-9]*):\s+(.*)$", line)
                if not match or "<" in line or EXTENSION_TYPES.search(line):
                    continue
                name, rest = match.groups()
                if "(" in rest:
                    function = parse_function(name, rest)
                    if function is None:
                        continue
                    key = ("function", function[0] + " " + name + "(" + ", ".join(function[1]) + ")")
                else:
                    variable = parse_variable(name, rest)
                    if variable is None:
                        continue
                    key = ("variable", variable + " " + name)
                symbols.setdefault(key, {}).setdefault(target, set()).add(stage)
            if stage in PER_VERTEX_OUT_STAGES:
                for type_name, member in PER_VERTEX_OUT:
                    symbols.setdefault(("variable", type_name + " " + member), {}).setdefault(target, set()).add(stage)
            if stage in PER_VERTEX_IN_STAGES:
                symbols.setdefault(("variable", "gl_PerVertex[] gl_in"), {}).setdefault(target, set()).add(stage)
    return symbols


def main():
    validator = sys.argv[1] if len(sys.argv) > 1 else "glslangValidator"
    version = subprocess.run([validator, "--version"], capture_output=True, text=True).stdout.splitlines()[0]
    symbols = collect(validator)
    os.makedirs(os.path.dirname(OUTPUT), exist_ok=True)
    with open(OUTPUT, "w") as output:
        output.write("# GLSL built-ins, generated by tools/glsl-builtins/generate.py from the built-in symbol table of glslang\n")
        output.write("# (https://github.com/KhronosGroup/glslang, 3-Clause BSD License, see NOTICE.txt), " + version + ".\n")
        output.write("# <kind> | <declaration> | <target>:<stages>[ <target>:<stages>...]\n")
        for (kind, signature), targets in sorted(symbols.items(), key=lambda item: (item[0][0] != "variable", item[0][1].split("(")[0].split()[-1], item[0][1])):
            places = " ".join(target + ":" + ",".join(sorted(stages, key=STAGES.index)) for target, stages in sorted(targets.items()))
            output.write(kind + " | " + signature + " | " + places + "\n")
    print("wrote", OUTPUT, len(symbols), "symbols")


if __name__ == "__main__":
    main()

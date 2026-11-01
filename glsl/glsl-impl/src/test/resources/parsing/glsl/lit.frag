#version 450
#extension GL_GOOGLE_include_directive : require
#include "lighting.glsl"

layout(location = 0) in vec3 inNormal;
layout(location = 0) out vec4 outColor;

layout(binding = 0) uniform Light {
    vec3 direction;
    vec3 color;
} light;

void main() {
    outColor = vec4(light.color * lambert(inNormal, -light.direction), 1.0);
}

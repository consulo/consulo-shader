#version 460 core
#extension GL_EXT_ray_tracing : enable
#define LIGHT_COUNT 4
#define SQUARE(x) ((x) * (x))
#if defined(LIGHT_COUNT) && LIGHT_COUNT > 2
#   define MANY_LIGHTS
#endif

precision highp float;
precision mediump int;

layout(local_size_x = 8, local_size_y = 8) in;
layout(std140, binding = 0) uniform Camera {
    mat4 view;
    mat4 projection;
    vec3 position;
} camera;

layout(std430, set = 1, binding = 2) readonly buffer Lights {
    vec4 colors[];
};

layout(location = 0) in vec3 inPosition;
layout(location = 1) flat in ivec2 inIndex;
layout(location = 0) out vec4 outColor;
layout(binding = 3, rgba8) uniform writeonly image2D target;
uniform sampler2D albedo;
shared float partial[64];
invariant gl_Position;

const float weights[3] = float[3](0.25, 0.5, 0.25);
const vec2 offsets[2][2] = vec2[2][2](vec2[2](vec2(0.0), vec2(1.0)), vec2[2](vec2(2.0), vec2(3.0)));

struct Material {
    vec3 albedo;
    float roughness;
    float layers[2];
};

struct Light {
    vec3 direction;
    float intensity;
} sun = Light(vec3(0.0, -1.0, 0.0), 1.0);

subroutine vec4 Shade(vec3 normal);
subroutine(Shade) vec4 shadeFlat(vec3 normal) { return vec4(normal, 1.0); }
subroutine uniform Shade shade;

float luminance(vec3 color);

float luminance(vec3 color) {
    return dot(color, vec3(0.2126, 0.7152, 0.0722));
}

void accumulate(inout vec4 sum, in vec4 value, const float weight) {
    sum += value * weight;
}

int classify(int value) {
    switch (value) {
        case 0:
            return 0;
        case 1:
        case 2: {
            int doubled = value << 1;
            return doubled;
        }
        default:
            break;
    }
    return -1;
}

void main() {
    Material material = Material(vec3(1.0), 0.5, float[2](1.0, 2.0));
    vec4 sum = vec4(0.0);
    for (int i = 0, j = 2; i < 3; i++, j--) {
        accumulate(sum, texture(albedo, inPosition.xy + float(i)), weights[i]);
    }
    int k = 0;
    while (k < LIGHT_COUNT) {
        k += 1;
        if (k == 2) continue; else if (k > 3) { break; }
    }
    do {
        k--;
    } while (k > 0);
    uint mask = 0xFFu & ~(1u << 3) | 0x10u ^ 0x01u;
    bool flag = !(mask > 2u) ^^ true || false && k != 0;
    float x = flag ? material.roughness : material.layers[1];
    x = SQUARE(x) - -x * +2.0 / 3.0e-1 % 1.0;
    x *= 2.0lf > 1.0 ? 1.5f : .5;
    vec3 swizzled = sum.rgb.zyx;
    int len = material.layers.length();
    partial[gl_LocalInvocationIndex] = luminance(swizzled);
    barrier();
    imageStore(target, ivec2(gl_GlobalInvocationID.xy), vec4(x, float(len), 0.0, 1.0));
    outColor = shade(swizzled) + vec4(camera.position, 1.0) * colors[0];
    if (x < 0.0) {
        discard;
    }
    // operators inside brackets, the middle of ?: and chained assignment
    float y = flag ? x + 1.0 : x - 1.0;
    y = partial[k + 1] + partial[k++] * float(len - 1);
    sum.x = sum.y = sum.z = flag ? y * 2.0 + 1.0 : -y;
    x = flag ? (y > 0.0 ? y : -y) : offsets[k - 1][len % 2].x;
    void helper(int value);
}

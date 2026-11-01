#version 330 compatibility
#include "/lib/settings.glsl"

varying vec4 color;

void main() {
#if SHADOW_QUALITY > 1
    gl_FragData[0] = color;
#else
    gl_FragData[0] = vec4(color.rgb, 1.0);
#endif
}

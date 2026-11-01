shader_type spatial;
render_mode blend_mix, cull_back, depth_draw_opaque;

#include "res://shaders/include/noise.gdshaderinc"

group_uniforms surface;
uniform vec4 water_color : source_color = vec4(0.1, 0.3, 0.5, 1.0);
uniform float wave_height : hint_range(0.0, 1.0, 0.01) = 0.2;
uniform sampler2D normal_map : hint_normal, filter_linear_mipmap;
group_uniforms;

varying float height;

void vertex() {
	height = sin(VERTEX.x * 4.0 + TIME) * wave_height + hash(VERTEX.xz) * 0.05;
	VERTEX.y += height;
}

void fragment() {
	vec3 normal = texture(normal_map, UV + TIME * 0.02).rgb;
	ALBEDO = water_color.rgb * (0.8 + height);
	NORMAL_MAP = normal;
	ROUGHNESS = 0.1;
}

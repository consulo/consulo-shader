float lambert(vec3 normal, vec3 lightDirection) {
    return max(dot(normalize(normal), normalize(lightDirection)), 0.0);
}

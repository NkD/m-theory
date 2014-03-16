varying vec2 v_texCoord2D;
varying vec3 v_pos;

void main(void) {
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
    v_texCoord2D = gl_Vertex.xy;
    v_pos = gl_Vertex.xyz;
}

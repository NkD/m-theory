varying vec2 v_texCoord2D;
varying vec3 v_pos;

float rand(vec2 co){
  return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

float noise(vec2 co){
  return rand(floor(co * 128.0));
}
       
       
vec4 rand2(vec2 A,vec2 B,vec2 C,vec2 D){
    vec2 s = vec2 (12.9898,78.233);
    vec4 tmp = vec4( dot(A,s),dot(B,s),dot(C,s),dot(D,s));
    return fract(tan(tmp)  * 43758.5453);
}



float noise2(vec2 coord,float d){
    vec2 C[4];
    C[0] = floor( coord * d)/d ;
    C[1] = C[0] + vec2(1.0/d ,0.0  );
    C[2] = C[0] + vec2(1.0/d ,1.0/d);
    C[3] = C[0] + vec2(0.0   ,1.0/d);
    vec2 p = fract(coord * d);
    vec2 q = 1.0 - p;
    vec4 w = vec4(q.x * q.y, p.x * q.y, p.x * p.y, q.x * p.y);
    return dot(vec4(rand2(C[0],C[1],C[2],C[3])),w);
}
        
void main (void) {
      
      //float r = rand(v_texCoord2D);
      //float n = noise(v_texCoord2D);
      //gl_FragColor = vec4(r,r,r,1);
      //gl_FragColor = vec4(n,n,n,1);
      
      
      //float r = rand(v_texCoord2D);
      float n = noise2(v_texCoord2D,100);
      //gl_FragColor = vec4(r,r,r,1);
      gl_FragColor = vec4(n,n,n,1);
      
      //gl_FragColor = vec4(v_pos.xyz,1);
}
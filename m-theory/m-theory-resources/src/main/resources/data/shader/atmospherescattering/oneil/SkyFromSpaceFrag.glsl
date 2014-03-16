//
// Atmospheric scattering fragment shader
//
// Author: Sean O'Neil
//
// Copyright (c) 2004 Sean O'Neil
//

uniform vec3 v3LightPos;
uniform float g; 
uniform float g2;

varying vec3 v3Direction;


void main (void)
{
	float fCos = dot(v3LightPos, v3Direction) / length(v3Direction);
	float fRayleighPhase = 0.75 * (1.0 + fCos*fCos);
	float fMiePhase = 1.5 * ((1.0 - g2) / (2.0 + g2)) * (1.0 + fCos*fCos) / pow(1.0 + g2 - 2.0*g*fCos, 1.5);
    gl_FragColor = fRayleighPhase * gl_Color + fMiePhase * gl_SecondaryColor;
	//gl_FragColor.a = gl_FragColor.b;
}


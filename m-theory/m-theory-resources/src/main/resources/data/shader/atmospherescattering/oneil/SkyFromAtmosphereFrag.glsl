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

uniform float fExposure;

varying vec3 v3Direction;

varying vec3 newFrontColor;
varying vec3 newFrontSecondaryColor;

void main (void)
{
	float fCos = dot(v3LightPos, v3Direction) / length(v3Direction);
	// here should be the texture lookup for opticalDepthBuffer ? - snareoj
	float fRayleighPhase = 0.75 * (1.0 + fCos*fCos);
	float fMiePhase = 1.5 * ((1.0 - g2) / (2.0 + g2)) * (1.0 + fCos*fCos) / pow(1.0 + g2 - 2.0*g*fCos, 1.5);
	gl_FragColor.rgb = fRayleighPhase * newFrontColor + fMiePhase * newFrontSecondaryColor;
	gl_FragColor.a = gl_FragColor.b;
	
	//gl_FragColor = vec4(0.3,0.3,0.3,1);
	
	// simple "HDR" clamping 
	//gl_FragColor = (1 - exp(-fExposure * gl_FragColor));
}


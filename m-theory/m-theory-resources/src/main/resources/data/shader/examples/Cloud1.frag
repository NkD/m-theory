//
// Fragment shader for producing clouds (mostly cloudy)
//
// Author: Randi Rost
//
// Copyright (c) 2002-2006 3Dlabs Inc. Ltd.
//
// See 3Dlabs-License.txt for license information
//

varying float LightIntensity; 
varying vec3  MCposition;

uniform sampler3D Noise;
uniform vec3 Offset;
uniform vec3 SkyColor;     // (0.0, 0.0, 0.8)
uniform vec3 CloudColor;   // (0.8, 0.8, 0.8)

void main (void)
{
    vec4  noisevec  = texture3D(Noise, MCposition + Offset);

    float intensity = (noisevec[0] + noisevec[1] + 
                       noisevec[2] + noisevec[3]) * 1.5;

    vec3 color   = mix(SkyColor, CloudColor, intensity) * LightIntensity;
    color  = clamp(color, 0.0, 1.0);
    gl_FragColor = vec4 (color, 1.0);
}


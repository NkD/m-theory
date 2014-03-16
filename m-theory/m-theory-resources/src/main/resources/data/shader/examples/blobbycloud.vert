//
// Vertex shader for blobby molecules
// Uses the seed migration/shrink wrap algorithm :
//	Migrate the vertex along it's normal until it reaches the isosurface or count == BailOut
//	Calculate the isosurface value by accumulating the inverse square of the distance to each blob
//	If this == RadiusOfInfluence then stop.
//
// Author: Jon Kennedy
//
// Copyright (c) 2002-2006 3Dlabs Inc. Ltd. 
//
// See 3Dlabs-License.txt for license information
//

//
// Uniforms
//
uniform float RadiusOfInfluence;
uniform int   BailOut;
uniform vec3  BlobbyPos[5];
uniform int   BlobbyCount;

//
// Globals
//
float Threshold = 0.01 * RadiusOfInfluence;

//
// Spheremap Func
//
vec2 SphereMap(in vec3 ecPosition3, in vec3 normal)
{
	float m;
	vec3 r, u, n;
	u = normalize(ecPosition3);
	r = reflect(u, normal);
	m = 2.0 * sqrt(r.x * r.x + r.y * r.y + (r.z + 1.0) * (r.z  + 1.0));
	return vec2(r.x/m + 0.5, r.y / m + 0.5);
}
 
//
// Main func
// 
void main(void)
{
	int ii, count = 0;
	float field = 0.0, fieldInc;
	float radius = 0.0; 
	vec3 vert;
	vec3 incVec = vec3(gl_Vertex) * 2.0;
	vec4 eyeCoord;
	vec3 eyeNorm3, eyeCoord3, norm;
	bool tooBig = true;
	bool found = false;
	
	//
	// Calc the position of the vertex in world space as
	// we are only passed in an untransormed vert relative to the origin
	//
	vert = vec3(gl_Vertex) + BlobbyPos[0];
	
	//
	// Migrate the vertex until found
	//
	while(found == false)
	{
	    count++;
	    
	    //
	    // Add the field of influence from the neighbours
	    //
	    field = 0.0;
	    eyeNorm3 = vec3(0.0, 0.0, 0.0);
		for(ii = 0; ii < BlobbyCount; ii++)
		{
			norm = vert - BlobbyPos[ii];
			
			// don't need the sqrt as we are squaring the radius anyway
			radius = dot(norm, norm);
	
			if((radius > 0.01)) 
			{
				fieldInc = 1.0 / radius;
				field += fieldInc;
				eyeNorm3 += (fieldInc * fieldInc) * norm;
			}
		}
		
		//
		// If we are within our tolerances, then we quit
		//
		if(field > (RadiusOfInfluence - Threshold))
		{
			if(field < (RadiusOfInfluence + Threshold))
			{
				found = true;
			}
		}
		 
		//
		// Outside tolerances, so add an increment and try again
		//
		if(found == false)
		{
			if(field < RadiusOfInfluence)  // radius is too big
			{
				if(tooBig == false)
					incVec /= 2.0;		// only do this when we've gone past the isosurface

				tooBig = true;
					
				vert -= incVec;
			}
			else
			{
				if(tooBig == true)
					incVec /= 2.0;		// only do this when we've gone past the isosurface

				tooBig = false;
				
				vert += incVec;
			}
		}
		
		//
		// If we've jumped too many times - bail out
		//
		if(count == BailOut && found != true)
		{
			// too many hops
			found = true;
		}
	}
	
	//
	// Calc the values as required by the fragment shader
	//
	eyeNorm3 = normalize(gl_NormalMatrix * eyeNorm3);
	eyeCoord = gl_ModelViewMatrix * vec4(vert, 1.0);
	eyeCoord3 = eyeCoord.xyz / eyeCoord.w;
	gl_TexCoord[0] = vec4(SphereMap(eyeCoord3, eyeNorm3), 0.0, 1.0);
	gl_Position  = gl_ProjectionMatrix * eyeCoord;
}
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// TEXTURES - IMPORTANT! you must pass these textures to
// the effect before generating any values using the improved
// noise basis function (inoise()).
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++
texture permTexture2d;
texture permGradTexture;

sampler permSampler2d = sampler_state
{
    texture =  lb permTexture2d rb; // lb and rb are left and right angled brackets
    AddressU  = Wrap;
    AddressV  = Wrap;
    MAGFILTER = POINT;
    MINFILTER = POINT;
    MIPFILTER = NONE;
};

sampler permGradSampler = sampler_state
{
    texture = lb permGradTexture rb; // lb and rb are left and right angled brackets
    AddressU  = Wrap;
    AddressV  = Wrap;
    MAGFILTER = POINT;
    MINFILTER = POINT;
    MIPFILTER = NONE;
};

//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// FUNCTIONS
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++
float3 fade(float3 t) {
    return t * t * t * (t * (t * 6 - 15) + 10); // new curve
}

float4 perm2d(float2 p) {
    return tex2D(permSampler2d, p);
}

float gradperm(float x, float3 p) {
    return dot(tex1D(permGradSampler, x), p);
}

// Improved 3d noise basis function
float inoise(float3 p) {
    float3 P = fmod(floor(p), 256.0);   // FIND UNIT CUBE THAT CONTAINS POINT
    p -= floor(p);                      // FIND RELATIVE X,Y,Z OF POINT IN CUBE.
    float3 f = fade(p);                 // COMPUTE FADE CURVES FOR EACH OF X,Y,Z.

    P = P / 256.0;

    // HASH COORDINATES OF THE 8 CUBE CORNERS
    float4 AA = perm2d(P.xy) + P.z;

    // AND ADD BLENDED RESULTS FROM 8 CORNERS OF CUBE
    return lerp( lerp( lerp( gradperm(AA.x, p ),
                             gradperm(AA.z, p + float3(-1, 0, 0) ), f.x),
                       lerp( gradperm(AA.y, p + float3(0, -1, 0) ),
                             gradperm(AA.w, p + float3(-1, -1, 0) ), f.x), f.y),

                 lerp( lerp( gradperm(AA.x+(1.0 / 256.0), p + float3(0, 0, -1) ),
                             gradperm(AA.z+(1.0 / 256.0), p + float3(-1, 0, -1) ), f.x),
                       lerp( gradperm(AA.y+(1.0 / 256.0), p + float3(0, -1, -1) ),
                             gradperm(AA.w+(1.0 / 256.0), p + float3(-1, -1, -1) ), f.x), f.y), f.z);
}

//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// FRACTAL FUNCTIONS
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// fractal sum
float fBm(float3 p, int octaves, float lacunarity = 2.0, float gain = 0.5) {
    float freq = 1.0f,
          amp  = 0.5f;
    float sum  = 0.0f;
    for(int i=0; i lb octaves; i++) {
        sum += inoise(p*freq)*amp;
        freq *= lacunarity;
        amp *= gain;
    }
    return sum;
}

float turbulence(float3 p, int octaves, float lacunarity = 2.0, float gain = 0.5) {
    float sum = 0;
    float freq = 1.0, amp = 1.0;
    for(int i=0; i lb octaves; i++) {
        sum += abs(inoise(p*freq))*amp;
        freq *= lacunarity;
        amp *= gain;
    }
    return sum;
}

// Ridged multifractal
// See "Texturing & Modeling, A Procedural Approach", Chapter 12
float ridge(float h, float offset){
    h = abs(h);
    h = offset - h;
    h = h * h;
    return h;
}

float ridgedmf(float3 p, int octaves, float lacunarity, float gain = 0.05, float offset = 1.0) {
    float sum = 0;
    float freq = 1.0;
    float amp = 0.5;
    float prev = 1.0;
    for(int i=0; i lb octaves; i++) {
        float n = ridge(inoise(p*freq), offset);
        sum += n*amp*prev;
        prev = n;
        freq *= lacunarity;
        amp *= gain;
    }
    return sum;
}
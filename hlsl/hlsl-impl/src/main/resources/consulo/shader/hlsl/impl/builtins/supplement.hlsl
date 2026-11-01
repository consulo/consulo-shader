// Copyright 2013-2026 consulo.io, Apache License 2.0.
// Intrinsics that builtins.hlsl does not declare: a few of HLSL, and the texture functions of Cg.

void abort();
bool CheckAccessFullyMapped(uint status);
bool WaveActiveAnyTrue(bool expr);
bool WaveActiveAllTrue(bool expr);
uint4 WaveActiveBallot(bool expr);
bool WaveActiveAllEqualBool(bool expr);
T QuadReadAcrossX(T localValue);
T QuadReadAcrossY(T localValue);
T QuadReadAcrossDiagonal(T localValue);
T QuadReadLaneAt(T sourceValue, uint quadLaneID);

float4 tex1Dfetch(sampler1D s, int4 t);
int tex1Dsize(sampler1D s, int lod);
float4 tex2Dfetch(sampler2D s, int4 t);
int2 tex2Dsize(sampler2D s, int lod);
float4 tex3Dfetch(sampler3D s, int4 t);
int3 tex3Dsize(sampler3D s, int lod);
int2 texCUBEsize(samplerCUBE s, int lod);
float4 texRECT(samplerRECT s, float2 t);
float4 texRECTbias(samplerRECT s, float4 t);
float4 texRECTfetch(samplerRECT s, int4 t);
float4 texRECTlod(samplerRECT s, float4 t);
float4 texRECTproj(samplerRECT s, float3 t);
int2 texRECTsize(samplerRECT s, int lod);
float4 tex1DARRAY(sampler1DARRAY s, float2 t);
float4 tex1DARRAYbias(sampler1DARRAY s, float4 t);
float4 tex1DARRAYlod(sampler1DARRAY s, float4 t);
float4 tex1DARRAYfetch(sampler1DARRAY s, int4 t);
int2 tex1DARRAYsize(sampler1DARRAY s, int lod);
float4 tex2DARRAY(sampler2DARRAY s, float3 t);
float4 tex2DARRAYbias(sampler2DARRAY s, float4 t);
float4 tex2DARRAYlod(sampler2DARRAY s, float4 t);
float4 tex2DARRAYfetch(sampler2DARRAY s, int4 t);
int3 tex2DARRAYsize(sampler2DARRAY s, int lod);
float4 texCUBEARRAY(samplerCUBEARRAY s, float4 t);
float4 texCUBEARRAYbias(samplerCUBEARRAY s, float4 t);
float4 texCUBEARRAYlod(samplerCUBEARRAY s, float4 t);
int3 texCUBEARRAYsize(samplerCUBEARRAY s, int lod);
float4 texBUF(samplerBUF s, int t);
int texBUFsize(samplerBUF s, int lod);
int floatToIntBits(float x);
int floatToRawIntBits(float x);
float intBitsToFloat(int x);

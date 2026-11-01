Texture2D<float4> Source : register(t0);
RWTexture2D<float4> Target : register(u0);

cbuffer BlurParameters : register(b0)
{
    float2 Direction;
    float Radius;
};

[numthreads(8, 8, 1)]
void main(uint3 id : SV_DispatchThreadID)
{
    float4 sum = 0;
    for (int i = -4; i <= 4; i++)
    {
        int2 offset = int2(Direction * i * Radius);
        sum += Source.Load(int3(id.xy + offset, 0));
    }
    Target[id.xy] = sum / 9.0;
}

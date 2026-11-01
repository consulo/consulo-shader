Shader "Samples/Unlit"
{
    Properties
    {
        _BaseColor ("Base Color", Color) = (1, 1, 1, 1)
        [Toggle] _UseFog ("Use Fog", Float) = 0
    }
    SubShader
    {
        Tags { "RenderType" = "Opaque" "RenderPipeline" = "UniversalPipeline" }
        Pass
        {
            HLSLPROGRAM
            #pragma vertex vert
            #pragma fragment frag

            float4 _BaseColor;

            float4 vert(float4 position : POSITION) : SV_POSITION
            {
                return position;
            }

            float4 frag() : SV_Target
            {
                return _BaseColor;
            }
            ENDHLSL
        }
    }
}

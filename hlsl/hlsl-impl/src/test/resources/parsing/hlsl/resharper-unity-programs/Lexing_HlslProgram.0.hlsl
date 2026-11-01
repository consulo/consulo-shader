

        #pragma surface surf Lambert

        sampler2D _MainTex;
        half4 _Color;

        struct Input
        {
            float2 uv_MainTex;
        };

        void surf (Input IN, inout SurfaceOutput o)
        {
            half4 c = tex2D (_MainTex, IN.uv_MainTex);
            o.Albedo = _Color.rgb * c.rgb;
            o.Alpha = _Color.a * c.a;
        }

        
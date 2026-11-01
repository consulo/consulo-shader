#include "Modules/ModuleManager.h"
#include "Interfaces/IPluginManager.h"
#include "ShaderCore.h"

class FShaderSampleModule : public IModuleInterface
{
public:
	virtual void StartupModule() override
	{
		// Maps "/ShaderSample/..." in #include and IMPLEMENT_GLOBAL_SHADER to <Project>/Shaders.
		const FString ShaderDir = FPaths::Combine(FPaths::ProjectDir(), TEXT("Shaders"));
		AddShaderSourceDirectoryMapping(TEXT("/ShaderSample"), ShaderDir);
	}
};

IMPLEMENT_MODULE(FShaderSampleModule, ShaderSample)

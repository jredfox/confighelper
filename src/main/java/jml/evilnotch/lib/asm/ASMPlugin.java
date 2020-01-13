package jml.evilnotch.lib.asm;

import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import net.minecraft.launchwrapper.Launch;

@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.Name("ASMPlugin")
@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.TransformerExclusions("evilnotch.lib.asm.")
public class ASMPlugin implements IFMLLoadingPlugin{

	static
	{
		PatchedClassLoader.stopMemoryOverflow(Launch.classLoader);
		TransformsReg.registerTransformer("jml.confighelper.asm.RegTransformer");
		TransformsReg.registerTransformer("jml.confighelper.asm.RegCompatTransformer");
		TransformsReg.registerTransformer("jml.shortids.asm.IdsTransformer");
	}
	
	@Override
	public String[] getASMTransformerClass() 
	{
		return new String[]{"jml.evilnotch.lib.asm.ASMLoader"};
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
		
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

}

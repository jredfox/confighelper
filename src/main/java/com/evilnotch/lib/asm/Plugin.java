package com.evilnotch.lib.asm;

import java.util.Map;

import com.evilnotch.mod.PatchedClassLoader;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import net.minecraft.launchwrapper.Launch;

@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.Name("evilnotchlib-transformer")
@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.TransformerExclusions("com.evilnotch.lib.asm.")
public class Plugin implements IFMLLoadingPlugin{

	static
	{
		PatchedClassLoader.stopMemoryOverflow(Launch.classLoader);
		TransformsReg.registerTransformer("com.jredfox.confighelper.asm.Transformer");
		TransformsReg.registerTransformer("com.jredfox.confighelper.compat.Transformer");
	}
	
	@Override
	public String[] getASMTransformerClass() 
	{
		return new String[]{"com.evilnotch.lib.asm.Transformer"};
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

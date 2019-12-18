package com.jredfox.confighelper.asm;

import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.Name("confighelper-registry-transformer")
public class Plugin implements IFMLLoadingPlugin{

	public static boolean isObf;
	@Override
	public String[] getASMTransformerClass() 
	{
		return new String[]{"com.jredfox.confighelper.asm.Transformer"};
	}

	@Override
	public String getModContainerClass() {return null;}

	@Override
	public String getSetupClass(){return null;}

	@Override
	public void injectData(Map<String, Object> data) {
		isObf = (Boolean) data.get("runtimeDeobfuscationEnabled");
	}

	@Override
	public String getAccessTransformerClass() {return null;}

}

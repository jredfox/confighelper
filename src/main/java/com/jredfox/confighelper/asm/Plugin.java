package com.jredfox.confighelper.asm;

import java.lang.reflect.Field;
import java.util.Map;

import com.evilnotch.lib.reflect.ReflectionHandler;
import com.evilnotch.mod.PatchedClassLoader;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.Name("confighelper-registry-transformer")
public class Plugin implements IFMLLoadingPlugin{
	
	@Override
	public String[] getASMTransformerClass() 
	{
		return new String[]
		{
			"com.jredfox.confighelper.asm.Transformer",
			"com.jredfox.confighelper.compat.Transformer"
		};
	}

	@Override
	public String getModContainerClass() {return null;}

	@Override
	public String getSetupClass(){return null;}

	@Override
	public void injectData(Map<String, Object> data){}

	@Override
	public String getAccessTransformerClass() {return null;}

}

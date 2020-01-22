package jml.evilnotch.lib.asm;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.ResourceLocation;

@IFMLLoadingPlugin.Name("ASMPlugin")
@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.TransformerExclusions("jml.evilnotch.lib.asm.")
public class ASMPlugin implements Coremod{
	
	@Override
	public String[] getASMTransformerClass() 
	{
		return new String[]{"jml.evilnotch.lib.asm.ASMLoader"};
	}

	@Override
	public void registerTransformers() 
	{
		System.out.println("Launch Minecraft Home:" + Launch.minecraftHome);
		PatchedClassLoader.stopMemoryOverflow(Launch.classLoader);
	}

}

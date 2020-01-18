package jml.confighelper.asm;

import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import jml.evilnotch.lib.asm.Coremod;
import jml.evilnotch.lib.asm.TransformsReg;

public class Plugin extends Coremod{

	@Override
	public void registerTransformers() 
	{
		TransformsReg.registerTransformer("jml.confighelper.asm.RegTransformer");
		TransformsReg.registerTransformer("jml.confighelper.asm.RegCompatTransformer");
		TransformsReg.registerTransformer("jml.shortids.asm.IdsTransformer");
	}

}

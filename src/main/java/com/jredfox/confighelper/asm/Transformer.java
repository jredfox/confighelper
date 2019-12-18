package com.jredfox.confighelper.asm;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.jredfox.confighelper.RegistryIds;

import net.minecraft.launchwrapper.IClassTransformer;

public class Transformer implements IClassTransformer{
	
	public static final List<String> clazzes = RegistryIds.asList(new String[]
	{
		"net.minecraft.world.biome.BiomeGenBase",
		"net.minecraft.potion.Potion",
		"net.minecraft.enchantment.Enchantment",
		"net.minecraftforge.common.DimensionManager",
		"net.minecraft.entity.EntityList",
		"net.minecraft.entity.DataWatcher"
	});
	
	@Override
	public byte[] transform(String oldName, String actualName, byte[] bytes) 
	{
		if(clazzes.contains(actualName))
		{
			String inputBase = "assets/confighelper/asm/" + (Plugin.isObf ? "srg/" : "deob/");
			String[] path = actualName.split("\\.");
			String fileClass = inputBase + path[path.length-1];
			InputStream stream = Transformer.class.getClassLoader().getResourceAsStream(fileClass);
			System.out.println("finding:" + fileClass + ", stream valid:" + (stream != null) );
			try 
			{
				return IOUtils.toByteArray(stream);
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		return bytes;
	}

}

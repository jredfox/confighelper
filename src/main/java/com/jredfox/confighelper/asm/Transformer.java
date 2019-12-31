package com.jredfox.confighelper.asm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.evilnotch.lib.reflect.ReflectionHandler;
import com.jredfox.confighelper.PatchedClassLoader;
import com.jredfox.confighelper.RegistryIds;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

public class Transformer implements IClassTransformer{
	
	static
	{
		PatchedClassLoader.stopMemoryOverflow(Launch.classLoader);
	}

	
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
			try 
			{
				return IOUtils.toByteArray(stream);
			}
			catch (Throwable t) 
			{
				t.printStackTrace();
			}
		}
		return bytes;
	}

}

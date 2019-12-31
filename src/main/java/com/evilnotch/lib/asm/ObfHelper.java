package com.evilnotch.lib.asm;

import com.evilnotch.lib.reflect.MCPSidedString;
import com.evilnotch.lib.reflect.ReflectionHandler;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import cpw.mods.fml.relauncher.CoreModManager;

public class ObfHelper
{
	public static final boolean isObf = !ReflectionHandler.getBoolean(ReflectionHandler.makePublicField(CoreModManager.class, "deobfuscatedEnvironment"), null);
	
	/**
	 * @return Whether or not the current environment contains obfuscated Minecraft code
	 */
	public static boolean isObfuscated()
	{
		return isObf;
	}

	/**
	 * Deobfuscates an obfuscated class name if {@link #isObfuscated()}.
	 */
	public static String toDeobfClassName(String obfClassName)
	{
		if (!isObfuscated())
			return forceToDeobfClassName(obfClassName);
		else
			return obfClassName.replace('.', '/');
	}

	/**
	 * Obfuscates a deobfuscated class name if {@link #isObfuscated()}.
	 */
	public static String toObfClassName(String deobfClassName)
	{
		if (isObfuscated())
			return forceToObfClassName(deobfClassName);
		else
			return deobfClassName.replace('.', '/');
	}
	
	/**
	 * Deobfuscates an obfuscated class name regardless of {@link #isObfuscated()}.
	 */
	public static String forceToDeobfClassName(String obfClassName)
	{
		return FMLDeobfuscatingRemapper.INSTANCE.map(obfClassName.replace('.', '/'));
	}

	/**
	 * Obfuscates a deobfuscated class name regardless of {@link #isObfuscated()}.
	 */
	public static String forceToObfClassName(String deobfClassName)
	{
		return FMLDeobfuscatingRemapper.INSTANCE.unmap(deobfClassName.replace('.', '/'));
	}
}
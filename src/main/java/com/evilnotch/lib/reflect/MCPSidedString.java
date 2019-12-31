package com.evilnotch.lib.reflect;

import cpw.mods.fml.relauncher.CoreModManager;

public class MCPSidedString {
	
	public static final boolean isObf = !ReflectionHandler.getBoolean(ReflectionHandler.makePublicField(CoreModManager.class, "deobfuscatedEnvironment"), null);
	
	public String deob;
	public String ob;
	
	public MCPSidedString(String deob, String ob)
	{
		this.deob = deob;
		this.ob = ob;
	}
	
	@Override
	public String toString()
	{
		return isObf ? this.ob : this.deob;
	}

}

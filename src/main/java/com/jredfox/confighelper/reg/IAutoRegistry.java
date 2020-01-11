package com.jredfox.confighelper.reg;

import net.minecraft.util.ResourceLocation;

public interface IAutoRegistry {
	
	public ResourceLocation getRegistryName();
	public void setRegistryName(ResourceLocation loc);
	/**
	 * the value must return -1 if it hasn't been set yet
	 */
	public int getId();
	public void setId(int id);

}

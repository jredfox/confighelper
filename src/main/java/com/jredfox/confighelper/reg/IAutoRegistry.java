package com.jredfox.confighelper.reg;

import net.minecraft.util.ResourceLocation;

public interface IAutoRegistry {
	
	public ResourceLocation getRegistryName();
	public void setRegistryName(ResourceLocation loc);
	public int getId();
	public void setId(int id);

}

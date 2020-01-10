package com.jredfox.confighelper.datawatcher;

import java.io.File;

import com.jredfox.confighelper.reg.IAutoRegistry;
import com.jredfox.confighelper.reg.RegistryWriter;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public abstract class WatcherDataType<T> implements IAutoRegistry{
	
	public int id = -1;
	public Class clazz;
	public ResourceLocation loc;
	public WatcherDataType(ResourceLocation loc, Class clazz)
	{
		this.loc = loc;
		this.clazz = clazz;
	}
	
	public abstract void write(PacketBuffer buf, T object);
	public abstract T read(PacketBuffer buf);
	
	@Override
	public boolean equals(Object other)
	{
		if(!(other instanceof WatcherDataType))
			return false;
		return this.id == ((WatcherDataType)other).id;
	}
	
	@Override
	public int hashCode()
	{
		return ((Integer)this.id).hashCode();
	}

	@Override
	public ResourceLocation getRegistryName() 
	{
		return this.loc;
	}

	@Override
	public void setRegistryName(ResourceLocation loc) 
	{
		this.loc = loc;
	}

	@Override
	public int getId()
	{
		return this.id;
	}

	@Override
	public void setId(int id) 
	{
		this.id = id;
	}

}

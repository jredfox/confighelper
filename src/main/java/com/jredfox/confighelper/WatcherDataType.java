package com.jredfox.confighelper;

import net.minecraft.network.PacketBuffer;

public abstract class WatcherDataType {
	
	public static int nextId = 7;
	
	public int dataType;
	public WatcherDataType()
	{
		this.dataType = nextId++;
	}
	
	public abstract void write(PacketBuffer buf, Object object);
	public abstract Object read(PacketBuffer buf);
	
	@Override
	public boolean equals(Object other)
	{
		if(!(other instanceof WatcherDataType))
			return false;
		return this.dataType == ((WatcherDataType)other).dataType;
	}
	
	@Override
	public int hashCode()
	{
		return ((Integer)this.dataType).hashCode();
	}

}

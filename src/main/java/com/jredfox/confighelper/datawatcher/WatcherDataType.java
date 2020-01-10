package com.jredfox.confighelper.datawatcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.jredfox.confighelper.reg.RegistryWriter;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

public abstract class WatcherDataType {
	
	public static int nextId = 7;
	
	public int dataType;
	public Class clazz;
	public WatcherDataType(Class clazz)
	{
		this.clazz = clazz;
		this.dataType = getNextId();
	}
	
	public static NBTTagCompound autonbt;
	public static File autonbtFile = new File(RegistryWriter.root, "auto/datawatcher-datatypes.dat");
	private int getNextId()
	{
		String clazz = this.clazz.getName();
		if(autonbt.hasKey(clazz))
		{
			return autonbt.getByte(clazz);
		}
		int id = this.nextId++;
		autonbt.setByte(clazz, (byte) id);
		return id;
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

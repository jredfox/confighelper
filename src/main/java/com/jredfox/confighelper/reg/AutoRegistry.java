package com.jredfox.confighelper.reg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.evilnotch.lib.JavaUtil;
import com.evilnotch.lib.simple.Directory;
import com.jredfox.confighelper.RegistryConfig;
import com.jredfox.confighelper.reg.Registry.DataType;

import cpw.mods.fml.common.Loader;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class AutoRegistry<T extends IAutoRegistry> {
	
	public DataType dataType;
	public int limitLower;
	public int limit;
	protected Map<ResourceLocation, T> reg = new HashMap();
	protected boolean frozen = true;//registries should be frozen before registration event and unfrozen after till before load complete
	
	public NBTTagCompound auto;
	public File autoFile;
	public AutoRegistry(DataType type)
	{
		this.dataType = type;
		this.limitLower = this.getLimitLower();
		this.limit = this.getLimit();
	}
	
	protected int getLimitLower()
	{
		return 0;
	}
	
	protected int getLimit()
	{
		return Integer.MAX_VALUE;
	}
	
	/**
	 * internal vanilla registration to ensure same ids
	 */
	public void register(int index, T obj)
	{
		this.check(obj);
		ResourceLocation loc = obj.getRegistryName();
		if(!isMinecraft(loc))
			Registries.makeCrashReport("registration", "a mod as attempted to register a hard coded id with a non minecraft object!");
		this.checkRange(index);
		obj.setId(index);
		this.reg.put(loc, obj);
	}

	private void checkRange(int index) 
	{
		if(index < this.limitLower || index > this.limit)
			Registries.makeCrashReport("registration", "index out of bounds:" + this.dataType + ", " + index + " the id must be between" + this.limitLower + "-" + this.limit + ")");
	}

	public void register(T obj)
	{
		this.check(obj);
		ResourceLocation loc = obj.getRegistryName();
		if(this.contains(loc))
			Registries.makeCrashReport("registration", "duplicate registry object " + this.dataType + " id:" + loc);
		int nextId = this.getNextId(obj);
		this.checkRange(nextId);
		obj.setId(nextId);
		this.reg.put(loc, obj);
	}

	/**
	 * use this for intended replacements
	 */
	public void replace(T obj)
	{
		this.check(obj);
		T old = this.unregister(obj);
		obj.setId(old.getId());
		this.reg.put(obj.getRegistryName(), obj);
	}

	public T unregister(T obj) 
	{
		this.check(obj);
		ResourceLocation loc = obj.getRegistryName();
		return this.reg.remove(loc);
	}

	public boolean contains(ResourceLocation loc)
	{
		return this.reg.containsKey(loc);
	}
	
	/**
	 * get an object based on registry name
	 */
	public T get(ResourceLocation key)
	{
		return this.reg.get(key);
	}
	
	/**
	 * for networking do not use
	 */
	public T get(int id)
	{
		for(T obj : this.reg.values())
		{
			if(id == obj.getId())
				return obj;
		}
		return null;
	}
	
	public Collection<T> values()
	{
		return this.reg.values();
	}
	
	public void freeze()
	{
		this.frozen = true;
	}
	
	public void unfreeze()
	{
		this.frozen = false;
	}
	
	protected void check(T obj) 
	{
		ResourceLocation loc = obj.getRegistryName();
		if(loc == null)
			Registries.makeCrashReport("registration", "null registry name(resource location) for object:" + obj);
		else if(this.frozen)
			Registries.makeCrashReport("registration", "registry are frozen use designated loading times registry:" + this.dataType + ", " + loc);
	}
	
	public static boolean isMinecraft(ResourceLocation loc) 
	{
		return loc.getResourceDomain().equals("minecraft");
	}
	
	public int nextId;
	public int getNextId(T obj)
	{
		String key = obj.getRegistryName().toString();
		if(this.auto.hasKey(key))
			return this.auto.getInteger(key);
		int id = nextId++;
		this.auto.setInteger(key, id);
		return id;
	}
	
	public void parseAutoConfig()
	{
		Directory dir = new Directory(RegistryWriter.root.getParent(), "autoids/" + this.dataType.getName(false)).create();
		this.autoFile = new File(dir, "ids.dat");
		long start = System.currentTimeMillis();
		this.auto = this.getOrCreateNBT(this.autoFile);
		this.removeInvalidIds();
		JavaUtil.printTime(start, "parsing auto ids took:");
	}
	
	private void removeInvalidIds()
	{
		Iterator<String> it = this.auto.func_150296_c().iterator();
		while(it.hasNext())
		{
			String key = it.next();
			ResourceLocation loc = new ResourceLocation(key);
			if(this.get(loc) == null)
			{
				it.remove();
			}
		}
	}

	public void saveAutoConfig()
	{
		this.saveNBT(this.auto, this.autoFile);
	}
	
	private NBTTagCompound getOrCreateNBT(File file) 
	{
		if(!file.exists())
			return new NBTTagCompound();
		try
		{
			NBTTagCompound nbt = CompressedStreamTools.readCompressed(new FileInputStream(file));
			return nbt != null ? nbt : new NBTTagCompound();
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
		return new NBTTagCompound();
	}

	private void saveNBT(NBTTagCompound nbt, File file) 
	{
		try 
		{
			long start = System.currentTimeMillis();
			CompressedStreamTools.writeCompressed(nbt, new FileOutputStream(file));
			JavaUtil.printTime(start, "auto ids saving took:");
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

}

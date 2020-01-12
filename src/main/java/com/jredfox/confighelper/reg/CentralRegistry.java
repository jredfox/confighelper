package com.jredfox.confighelper.reg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.evilnotch.lib.JavaUtil;
import com.evilnotch.lib.simple.Directory;
import com.jredfox.confighelper.RegistryConfig;
import com.jredfox.confighelper.reg.Registry.DataType;

import cpw.mods.fml.common.Loader;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class CentralRegistry<T extends IRegisterable> implements Iterable{
	
	/**
	 * the default value that the registry object should be set to by default
	 */
	public static final int unset = -2;
	
	public DataType dataType;
	public int limitLower;
	public int limit;
	protected Map<ResourceLocation, T> reg = new HashMap<ResourceLocation, T>();//retains the order for modders debuging things
	protected Map<Integer, T> idreg = new HashMap<Integer, T>();
	protected Set<Integer> vanillaIds;
	protected Set<Integer> usedIds = new TreeSet();
	protected boolean frozen = true;//registries should be frozen before registration event and unfrozen after till before load complete
	
	public CentralRegistry(DataType type)
	{
		this.dataType = type;
		this.limitLower = this.getLimitLower();
		this.limit = this.getLimit();
		this.vanillaIds = this.getVanillaIds();
	}
	
	private Set<Integer> getVanillaIds() 
	{
		if(this.dataType == DataType.DATAWATCHERTYPE)
		{
			return RegistryIds.datawatertypes;
		}
		return null;
	}

	protected int getLimitLower()
	{
		return 0;
	}
	
	protected int getLimit()
	{
		return RegistryIds.limitDatawatcherType;
	}
	
	/**
	 * internal use for vanilla registration do not use
	 */
	public void register(int id, T obj)
	{
		if(!isMinecraft(obj.getRegistryName()))
			Registries.makeCrashReport("registration","hard coded id! " + this.dataType + " id:" + id);
		obj.setId(id);
		this.register(obj);
	}

	public void register(T obj)
	{
		ResourceLocation loc = obj.getRegistryName();
		this.check(loc);
		if(this.contains(loc))
			Registries.makeCrashReport("registration", "duplicate registry object " + this.dataType + " id:" + loc);
		int id = obj.getId() == unset ? this.getId(obj) : obj.getId();
		this.checkId(id);
		obj.setId(id);
		this.reg.put(loc, obj);
		this.idreg.put(id, obj);
	}

	/**
	 * use this for intended replacements
	 */
	public void replace(T obj)
	{
		T old = this.unregister(obj.getRegistryName());
		obj.setId(old.getId());
		this.register(obj);
	}

	public T unregister(ResourceLocation loc) 
	{
		this.check(loc);
		T old = this.reg.remove(loc);
		if(old != null)
			this.idreg.remove(old.getId());
		return old;
	}

	public boolean contains(ResourceLocation loc)
	{
		return this.reg.containsKey(loc);
	}
	
	public boolean contains(int id)
	{
		return this.idreg.containsKey(id);
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
		return this.idreg.get(id);
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
	
	protected void check(ResourceLocation loc) 
	{
		if(loc == null)
			Registries.makeCrashReport("registration", "null registry name(resource location) for object:" + loc);
		else if(this.frozen)
			Registries.makeCrashReport("registration", "registry:" + this.dataType + " is frozen used designated loading times! tried to register:" + loc);
	}
	
	private void checkId(int index) 
	{
		if(index < this.limitLower || index > this.limit)
			Registries.makeCrashReport("registration", "index out of bounds:" + this.dataType + ", " + index + " the id must be between" + this.limitLower + "-" + this.limit + ")");
		if(this.contains(index))
			Registries.makeCrashReport("registration", this.dataType.getName(false) + " id conflict " + index);
	}
	
	public static boolean isMinecraft(ResourceLocation loc) 
	{
		return loc.getResourceDomain().equals("minecraft");
	}
	
	public int newId;
	protected int getId(T obj)
	{
		for(int index=this.newId;index<=this.limit;index++)
		{
			if(!this.isUsed(this.newId))
			{
				return this.newId++;
			}
			this.newId++;
		}
		return -1;
	}
	
	protected boolean isUsed(int id)
	{
		return this.vanillaIds.contains(id);
	}

	@Override
	public Iterator<T> iterator() 
	{
		return this.reg.values().iterator();
	}

}

package com.jredfox.confighelper.reg;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.jredfox.confighelper.RegistryConfig;
import com.jredfox.confighelper.reg.Registry.DataType;

import net.minecraft.util.ResourceLocation;

public class AutoRegistry<T extends IAutoRegistry> {
	
	public DataType dataType;
	public int limitLower;
	public int limit;
	protected Map<ResourceLocation, T> reg = new HashMap();
	public boolean frozen;
	
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
		return RegistryConfig.dataWatcherTypeLimit;
	}
	
	public void register(int index, T obj)
	{
		ResourceLocation loc = obj.getRegistryName();
		if(!loc.getResourceDomain().equals("minecraft"))
			Registries.makeCrashReport("registration", "a mod as attempted to register a hard coded id with a non minecraft object!");
	}
	
	public void register(T obj)
	{
		this.checkFrozen();
		ResourceLocation loc = obj.getRegistryName();
		if(this.contains(loc))
			Registries.makeCrashReport("registration", "duplicate registry object " + this.dataType + " id:" + loc);
		this.reg.put(loc, obj);
	}
	
	/**
	 * use this for intended replacements
	 */
	public void replace(T obj)
	{
		this.checkFrozen();
		T old = this.unregister(obj);
		obj.setId(old.getId());
		this.register(obj);
	}

	public T unregister(T obj) 
	{
		this.checkFrozen();
		ResourceLocation loc = obj.getRegistryName();
		return this.reg.remove(loc);
	}

	public boolean contains(ResourceLocation loc)
	{
		return this.reg.containsKey(loc);
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
		System.out.println("WARNING A mod has attempted to unfreeze a frozen registry this is usually the result of a broken mod");
		this.frozen = false;
	}
	
	protected void checkFrozen() 
	{
		if(this.frozen)
			Registries.makeCrashReport("registration", "registry are frozen use designated loading times" + this.dataType);
	}

}

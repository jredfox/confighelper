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
		T old = this.unreg(obj);
		obj.setId(old.getId());
		this.register(obj);
	}

	public T unreg(T obj) 
	{
		return this.reg.remove(obj.getRegistryName());
	}

	public boolean contains(ResourceLocation loc)
	{
		return this.reg.containsKey(loc);
	}
	
	public Collection<T> values()
	{
		return this.reg.values();
	}

}

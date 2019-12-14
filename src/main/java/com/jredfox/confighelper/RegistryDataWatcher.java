package com.jredfox.confighelper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.DataWatcher;

public class RegistryDatawatcher extends Registry{
	
	public DataWatcher watcher;

	public RegistryDatawatcher(DataWatcher watcher)
	{
		super(DataType.DATAWATCHER);
		this.watcher = watcher;
	}
	
	@Override
	public boolean canConflict(Class clazz, int id) 
	{
		for(Integer i : RegistryConfig.passableIds)
		{
			if(id == i)
				return false;
		}
		return true;
	}
	
	@Override
	public boolean canCrash()
	{
		return this.strict;
	}
	
	@Override
	public boolean containsId(int id)
	{
		return this.watcher.containsId(id);
	}
	
	@Override
   	public String getDisplay(Registry.Entry e)
	{
   		return "(" + e.name + ")";
	}
	
	public Set<Integer> vIds = new HashSet();
	@Override
	protected int getSuggestedId(Object obj, int org) 
	{
		if(this.isVanillaId(org) && !this.vIds.contains(org))
		{
			this.vIds.add(org);
			return org;
		}
		for(int i=this.suggestedId;i<=this.limit;i++)
		{
			if(!this.isVanillaId(this.suggestedId))
			{
				return this.suggestedId++;
			}
			this.suggestedId++;
		}
		return -1;
	}

}

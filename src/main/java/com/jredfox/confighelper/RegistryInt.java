package com.jredfox.confighelper;

import java.util.HashSet;
import java.util.Set;

import com.jredfox.confighelper.Registry.DataType;

public abstract class RegistryInt extends Registry{

	public RegistryInt(DataType dataType) 
	{
		super(dataType);
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
   	public String getDisplay(Registry.Entry e)
	{
   		return "(" + e.name + ")";
	}
	
	protected Set<Integer> cvids = new HashSet();
	@Override
	protected int getSuggestedId(Object obj, int org) 
	{
		if(this.isVanillaId(org) && !this.cvids.contains(org))
		{
			this.cvids.add(org);
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
	
	@Override
	public abstract boolean containsId(int id);

}

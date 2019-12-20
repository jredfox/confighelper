package com.jredfox.confighelper;

import java.util.HashSet;
import java.util.Set;

import com.jredfox.confighelper.Registry.DataType;

public abstract class RegistryInt extends Registry{

	public RegistryInt(DataType dataType) 
	{
		super(dataType);
	}
	
	/**
	 * a list of integer passable ids that are acceptible to conflict with
	 * WARNGING THIS ALLOWS FOR NOT JUST REPLACEMENT CONFLICTS BUT, ACTUAL CONFLICTS PROCEDE WITH CAUTION
	 */
	public abstract Set<Integer> getPassableIds();
	
	@Override
	public boolean shouldReplace(Class clazz, int id) 
	{
		for(Integer i : this.getPassableIds())
		{
			if(id == i)
				return true;
		}
		return false;
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

}

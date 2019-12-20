package com.jredfox.confighelper;

import java.util.HashSet;
import java.util.Set;

import com.jredfox.confighelper.Registry.DataType;

import net.minecraftforge.common.DimensionManager;

public class RegistryProvider extends Registry{
	
	public int lower = -2;//newId lower index counter
	public int lowerV = -2;//suggestedId lower index counter
	public int lowerFreeId = -2;//freeId lower index counter
	
	public RegistryProvider() 
	{
		super(DataType.PROVIDER);
	}
	
	
	@Override
	public int getNextFreeId(int id)
	{
		if(id >= 0)
			return super.getNextFreeId(id);
		else
		{
			for(int i=this.lowerFreeId;i>=RegistryConfig.searchDimLower;i--)
			{
				if(!this.containsOrg(this.lowerFreeId))
				{
					return lowerFreeId--;
				}
				lowerFreeId--;
			}
		}
		return -1;
	}
	
	@Override
	public void resetFreeIds()
	{
		super.resetFreeIds();
		this.lowerFreeId = 0;
	}
	
	@Override
	public int getNewId(int org) 
	{
		if(org >= 0)
		{
			return super.getNewId(org);
		}
		else
		{
			for(int i=this.lower;i>=RegistryConfig.searchDimLower;i--)
			{
				if(!this.containsId(this.lower))
				{
					return this.lower;
				}
				this.lower--;
			}
		}
		return -1;
	}
	
	/**
	 * checked vanilla ids
	 */
	protected Set<Integer> cvids = new HashSet();
	@Override
	protected int getSuggestedId(Object obj, int org) 
	{
		if(org >= 0)
		{
			super.getSuggestedId(obj, org);
		}
		else
		{
			if(this.isVanillaObj(obj))
				return org;
			
			for(int i = this.lowerV; i >= RegistryConfig.searchDimLower; i--)
			{
				if(!this.isVanillaId(this.lowerV))
				{
					return this.lowerV--;
				}
				this.lowerV--;
			}
		}
		return -1;
	}

}

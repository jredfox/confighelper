package com.jredfox.confighelper;

import java.util.HashSet;
import java.util.Set;

import com.jredfox.confighelper.Registry.DataType;

import net.minecraftforge.common.DimensionManager;

public class RegistryProvider extends Registry{
	
	//the actual id index counters
	public int upper = 2;
	public int lower = -2;
	
	//the virtual suggested ids
	public int upperV = 2;
	public int lowerV = -2;

	public RegistryProvider() 
	{
		super(DataType.PROVIDER);
	}
	
	@Override
	public boolean containsId(int id)
	{
		return DimensionManager.providers.containsKey(id);
	}
	
	@Override
	public int getFreeId(int org) 
	{
		if(org >= 0)
		{
			for(int i=this.upper;i<=RegistryConfig.searchDimUper;i++)
			{
				if(!this.containsId(this.upper))
				{
					return this.upper;
				}
				this.upper++;
			}
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
		return org;
	}
	
	/**
	 * checked vanilla ids
	 */
	protected Set<Integer> cvids = new HashSet();
	@Override
	protected int getSuggestedId(Object obj, int org) 
	{
		if(this.isVanillaObj(obj))
			return org;

		if(org >= 0)
		{
			for(int i = this.upperV; i <= RegistryConfig.searchDimUper; i++)
			{
				if(!this.isVanillaId(this.upperV))
				{
					return this.upperV++;
				}
				this.upperV++;
			}
		}
		else
		{
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

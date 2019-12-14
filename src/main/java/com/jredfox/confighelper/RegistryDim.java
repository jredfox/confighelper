package com.jredfox.confighelper;

import java.util.Set;

import com.jredfox.confighelper.Registry.DataType;

import net.minecraftforge.common.DimensionManager;

public class RegistryDim extends RegistryInt{

	//the actual id index counters
	public int upper = 2;
	public int lower = -2;
	
	//the virtual suggested ids
	public int upperV = 2;
	public int lowerV = -2;
	
	public RegistryDim()
	{
		super(DataType.DIMENSION);
	}
	
	@Override
	public boolean containsId(int id)
	{
		return DimensionManager.dimensions.containsKey(id);
	}
	
	@Override
	public Set<Integer> getPassableIds()
	{
		return RegistryConfig.passableDimIds;
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
	
	@Override
	protected int getSuggestedId(Object obj, int org) 
	{
		if(this.isVanillaId(org) && !this.cvids.contains(org))	
		{
			this.cvids.add(org);
			return org;
		}

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

package com.jredfox.confighelper;

import java.util.HashSet;
import java.util.Set;

import net.minecraftforge.common.DimensionManager;

public class RegistryDim extends Registry{
	
	//the actual id index counters
	public int upper = 2;
	public int lower = -2;
	
	//the virtual suggested ids
	public int lowerV = -2;
	public int upperV = 2;
	
	public RegistryDim(DataType type)
	{
		super(vallidDataType(type));
	}
	
	private static DataType vallidDataType(DataType type) {
		if(type != type.DIMENSION && type != type.PROVIDER)
			throw new IllegalArgumentException("DataType for RegistryDim must be DIMENSION or PROVIDER Data types");
		return type;
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
	public boolean containsId(int id)
	{
		if(this.dataType == DataType.PROVIDER)
			return DimensionManager.providers.containsKey(id);
		return DimensionManager.dimensions.containsKey(id);
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
		
		if(org >= 0)
		{
			for(int i=this.upperV;i<=RegistryConfig.searchDimUper;i++)
			{
				if(!this.vIds.contains(this.upperV) && !this.isVanillaId(this.upperV))
				{
					this.vIds.add(this.upperV);
					return this.upperV;
				}
				this.upperV++;
			}
		}
		else
		{
			for(int i=this.lowerV;i>=RegistryConfig.searchDimLower;i--)
			{
				if(!this.vIds.contains(this.lowerV) && !this.isVanillaId(this.lowerV))
				{
					this.vIds.add(this.lowerV);
					return this.lowerV;
				}
				this.lowerV--;
			}
		}
		return -1;
	}
}

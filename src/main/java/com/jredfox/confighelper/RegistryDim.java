package com.jredfox.confighelper;

import com.jredfox.confighelper.Registry.DataType;

import net.minecraftforge.common.DimensionManager;

public class RegistryDim extends Registry{
	
	public int lowerLimit = RegistryConfig.searchDimLower;
	public int lowerIndex = -1;
	public int uperLimit =  RegistryConfig.searchDimUper;
	public int uperIndex;
	
	public RegistryDim(boolean strict, DataType type)
	{
		super(strict, vallidDataType(type));
	}
	
	private static DataType vallidDataType(DataType type) {
		if(type != type.DIMENSION && type != type.PROVIDER)
			throw new IllegalArgumentException("DataType for RegistryDim must be DIMENSION or PROVIDER Data types");
		return type;
	}


	@Override
	protected int getFreeId(int org) 
	{
		if(org < 0)
		{
			for(int i = this.lowerIndex; i >= lowerLimit; i--)
			{
				this.lowerIndex = i;
				if(!this.containsDim(this.lowerIndex))
				{
					return this.lowerIndex;
				}
			}
			throw new IllegalArgumentException("no more ids for lower Dimension Limit try upping the amount of ids");
		}
		else
		{
			for(int i = this.uperIndex; i <= this.uperLimit; i++)
			{
				this.uperIndex = i;
				if(!this.containsDim(this.uperIndex))
				{
					return this.uperIndex;
				}
			}
			throw new IllegalArgumentException("no more ids for lower Dimension Limit try upping the amount of ids");
		}
	}
	
	public boolean containsDim(int id)
	{
		return this.dataType == DataType.DIMENSION ? DimensionManager.isDimensionRegistered(id) :  DimensionManager.providers.contains(id);
	}

}

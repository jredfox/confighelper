package com.jredfox.confighelper;

import com.jredfox.confighelper.Registry.DataType;

import net.minecraftforge.common.DimensionManager;

public class RegistryDim extends Registry{
	
	public static final int[] dimIds = new int[]{-1,0,1};
	public int upper = 2;
	public int lower = -2;
	
	public RegistryDim(boolean strict, DataType type)
	{
		super(strict, vallidDataType(type));
	}
	
	private static DataType vallidDataType(DataType type) {
		if(type != type.DIMENSION && type != type.PROVIDER)
			throw new IllegalArgumentException("DataType for RegistryDim must be DIMENSION or PROVIDER Data types");
		return type;
	}
	
	/**
	 * prevent vanilla ids from getting automated
	 */
	@Override
	public boolean canAuto(Object obj, int org)
	{
		return RegistryConfig.autoConfig && (org > 1 || org < -1);
	}


	@Override
	protected int getFreeId(int org) 
	{
		if(org >= 0)
		{
			for(int i=this.upper;i<=RegistryConfig.searchDimUper;i++)
			{
				if(!this.containsDim(this.upper))
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
				if(!this.containsDim(this.lower))
				{
					return this.lower;
				}
				this.lower--;
			}
		}
		return org;
	}
	
	public boolean containsDim(int id)
	{
		if(this.dataType == DataType.PROVIDER)
			return DimensionManager.providers.containsKey(id);
		return DimensionManager.dimensions.containsKey(id);
	}

}

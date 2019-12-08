package com.jredfox.confighelper;

import net.minecraftforge.common.DimensionManager;

public class RegistryDim extends Registry{
	
	public int upper = 2;
	public int lower = -2;
	
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
	protected int getFreeId(int org) 
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

	public static final int[] dimIds = new int[]{-1,0,1};
	public static boolean isVanillaId(int org) 
	{
		for(int v : dimIds)
			if(v == org)
				return true;
		return false;
	}

}

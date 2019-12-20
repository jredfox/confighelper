package com.jredfox.confighelper;

import java.util.Set;

import com.jredfox.confighelper.Registry.DataType;

import net.minecraftforge.common.DimensionManager;

public class RegistryDim extends RegistryInt{

	public int lower = -2;//lower newId index
	public int lowerV = -2;//lower suggestedId index
	public int lowerFreeId = -2;
	
	public RegistryDim()
	{
		super(DataType.DIMENSION);
	}
	
	@Override
	public Set<Integer> getPassableIds()
	{
		return RegistryConfig.passableDimIds;
	}
	
	@Override
	public int getNextFreeId(int newId)
	{
		if(newId >= 0)
			return super.getNextFreeId(newId);
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
		this.lowerFreeId = -2;
	}

	@Override
	public int getNewId(int org) 
	{
		if(org >= 0)
			return super.getNewId(org);
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
	
	@Override
	public int getNextSuggestedId(int newId)
	{
		if(this.vanillaIds.contains(newId))
			return newId;
		if(newId >= 0)
			return super.getNextSuggestedId(newId);
		else
		{
			for(int i=this.lowerV;i>=RegistryConfig.searchDimLower;i--)
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

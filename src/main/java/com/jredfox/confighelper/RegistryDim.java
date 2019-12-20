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
	protected int getSuggestedId(Object obj, int org) 
	{
		if(org >= 0)
			return super.getSuggestedId(obj, org);
		else
		{
			if(this.isVanillaId(org) && !this.cvids.contains(org))	
			{
				this.cvids.add(org);
				return org;
			}
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

package com.jredfox.confighelper.reg;

import java.util.HashSet;
import java.util.Set;

import com.jredfox.confighelper.RegistryConfig;
import com.jredfox.confighelper.reg.Registry.DataType;

import net.minecraftforge.common.DimensionManager;

public class RegistryProvider extends Registry{
	
	public RegistryProvider() 
	{
		super(DataType.PROVIDER);
	}
	
	public int lower = -2;//newId lower index counter
	@Override
	public int getNewId(int org) 
	{
		if(org >= 0)
		{
			return super.getNewId(org);
		}
		else
		{
			for(int i=this.lower;i>=RegistryConfig.dimLimitLower;i--)
			{
				if(!this.containsId(this.lower) && !this.isVanillaId(this.lower))
				{
					return this.lower--;
				}
				this.lower--;
			}
		}
		return -1;
	}
	
	public int lowerFreeId = -2;//freeId lower index counter
	@Override
	public int getNextFreeId(int id)
	{
		if(id >= 0)
			return super.getNextFreeId(id);
		else
		{
			for(int i=this.lowerFreeId;i>=RegistryConfig.dimLimitLower;i--)
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
		this.freeId = 2;
		this.lowerFreeId = -2;
	}
	
	public int lowerV = -2;//suggestedId lower index counter
	@Override
	public int getNextSuggestedId(int newId)
	{
		if(this.vanillaIds.contains(newId))
			return newId;
		if(newId >= 0)
			return super.getNextSuggestedId(newId);
		else
		{
			for(int i=this.lowerV;i>=RegistryConfig.dimLimitLower;i--)
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

package jml.confighelper.reg;

import java.util.Set;

import jml.confighelper.RegistryConfig;
import jml.confighelper.reg.Registry.DataType;
import net.minecraftforge.common.DimensionManager;

public class RegistryDim extends RegistryInt{
	
	public RegistryDim()
	{
		super(DataType.DIMENSION);
	}
	
	@Override
	public Set<Integer> getPassableIds()
	{
		return RegistryConfig.passableDimIds;
	}
	
	public int lower = -2;//lower newId index
	@Override
	public int getNewId(int org) 
	{
		if(org >= 0)
			return super.getNewId(org);
		else
		{
			for(int i=this.lower;i>=RegistryIds.limitDimLower;i--)
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
	
	public int lowerFreeId = -2;
	@Override
	public int getNextFreeId(int newId)
	{
		if(newId >= 0)
			return super.getNextFreeId(newId);
		else
		{
			for(int i=this.lowerFreeId;i>=RegistryIds.limitDimLower;i--)
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
	public void resetSuggestedIds()
	{
		super.resetSuggestedIds();
		this.lowerV = -2;
	}
	
	public int lowerV = -2;//lower suggestedId index
	@Override
	public int getNextSuggestedId(int newId)
	{
		if(newId >= 0)
			return super.getNextSuggestedId(newId);
		else
		{
			if(this.isVanillaId(newId))
				return newId;
			for(int i=this.lowerV;i>=RegistryIds.limitDimLower;i--)
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
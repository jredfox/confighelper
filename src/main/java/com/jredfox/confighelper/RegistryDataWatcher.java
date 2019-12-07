package com.jredfox.confighelper;

import net.minecraft.entity.DataWatcher;

public class RegistryDataWatcher extends Registry{

	public RegistryDataWatcher(boolean strict)
	{
		super(strict, DataType.DATAWATCHERPLAYER);
	}
	
	@Override
	public boolean canCrash()
	{
		return this.strict;
	}
	
	@Override
	public int getFreeId(int org)
	{
		for(int i=this.id;i<= RegistryConfig.dataWatchersLimit; i++)
		{
			if()
		}
	}

}

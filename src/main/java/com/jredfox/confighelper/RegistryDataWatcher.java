package com.jredfox.confighelper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.DataWatcher;

public class RegistryDatawatcher extends RegistryInt{

	public RegistryDatawatcher()
	{
		super(DataType.DATAWATCHER);
	}
	
	@Override
	public Set<Integer> getPassableIds()
	{
		return RegistryConfig.passableWatcherIds;
	}
	
	@Override
	public boolean canCrash()
	{
		return this.strict;
	}
}

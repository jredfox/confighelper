package com.jredfox.confighelper.reg;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.jredfox.confighelper.RegistryConfig;

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
}

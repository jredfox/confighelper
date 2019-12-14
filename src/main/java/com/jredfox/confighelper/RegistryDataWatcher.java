package com.jredfox.confighelper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.DataWatcher;

public class RegistryDatawatcher extends RegistryInt{
	
	public DataWatcher watcher;

	public RegistryDatawatcher(DataWatcher watcher)
	{
		super(DataType.DATAWATCHER);
		this.watcher = watcher;
	}
	
	@Override
	public boolean containsId(int id)
	{
		return this.watcher.containsId(id);
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

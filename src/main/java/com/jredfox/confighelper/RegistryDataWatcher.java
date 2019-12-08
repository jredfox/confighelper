package com.jredfox.confighelper;

import java.util.List;

import net.minecraft.entity.DataWatcher;

public class RegistryDataWatcher extends Registry{

	public static final int[] ids = {0,1,6,7,8,9,16,17,18};
	public DataWatcher watcher;

	public RegistryDataWatcher(boolean strict, DataWatcher watcher)
	{
		super(strict, DataType.DATAWATCHER);
		this.watcher = watcher;
	}
	
	@Override
	public int reg(Object obj, int org)
	{
		return super.reg(obj, org);
	}
	
	@Override
	public boolean canAuto(Object obj, int org)
	{
		if(isVanillaId(org))
			return RegistryConfig.autoConfig && this.containsId(org);//if vanilla id is already registered it can be auto configurable
		return RegistryConfig.autoConfig;
	}
	
	private boolean containsId(int org) 
	{
		return this.watcher.containsId(org);
	}

	public static boolean isVanillaId(int org) 
	{
		for(int v : ids)
			if(v == org)
				return true;
		return false;
	}

	@Override
	public boolean canCrash()
	{
		return this.strict;
	}
	
	@Override
	public int getFreeId(int org)
	{
		for(int i=this.id; i <= RegistryConfig.dataWatchersLimit; i++)
		{
			if(!this.watcher.containsId(this.id))
			{
				return this.id;
			}
			this.id++;
		}
		return -1;
	}
	
	@Override
    public String getDisplay(int id)
    {
    	StringBuilder builder = new StringBuilder();
    	builder.append('[');
    	List<Entry> list = this.reg.get(id);
    	for(Entry e : list)
    	{
    		builder.append("(name:" + e.name + "), ");
    	}
    	builder.append(']');
    	return builder.toString();
    }

}

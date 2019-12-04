package com.jredfox.confighelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ResourceLocation;

public class Registry {
	
	public HashMap<Integer,List<Class>> reg = new LinkedHashMap<Integer,List<Class>>();
	/**
	 * an automated integer
	 */
	public int id;
	public boolean strict;
	public String dataType;
	
	public Registry(){}
	public Registry(boolean strict, String dataType)
	{
		this.strict = strict;
		this.dataType = dataType.toLowerCase();
	}
	
	public int reg(Object obj, int id)
	{
		List<Class> list = reg.get(id);
		if(list == null)
		{
			list = new ArrayList<Class>();
			reg.put(id, list);
		}
		if(obj instanceof List)
		{
			List li = (List)obj;
			for(Object index : li)
			{
				Class clazz = getClass(index);
				list.add(clazz);
			}
		}
		else
		{
			list.add(getClass(obj));
		}
		if(list.size() > 1)
		{
			if(!RegistryTracker.startup || this.strict)
			{
				RegistryTracker.output();
				CrashReport crashreport = CrashReport.makeCrashReport(new RuntimeException(this.dataType + " Id conflict has caused the game to crash " + "id:" + id + "=" + list), "In Game");
				crashreport.makeCategory("In Game");
		        Minecraft.getMinecraft().displayCrashReport(Minecraft.getMinecraft().addGraphicsAndWorldToCrashReport(crashreport));
			}
		}
		return RegistryConfig.autoConfig ? this.id++ : getId(obj, id);
	}

	/**
	 * auto configure vanilla ids
	 */
	private int getId(Object obj, int id) 
	{
		if(getClass(obj).getName().startsWith("net.minecraft.") && !this.dataType.equals(RegistryTracker.dimensions.dataType))
			return this.id++;
		return id;
	}
	
	private Class getClass(Object entry)
	{
		if(entry instanceof Class)
			return (Class) entry;
		return entry.getClass();
	}

    public Integer getIndex(int org) 
    {
    	int index = 0;
		for(Integer compare : this.reg.keySet())
		{
			if(org == compare)
				return index;
			index++;
		}
		return -1;
	}

}

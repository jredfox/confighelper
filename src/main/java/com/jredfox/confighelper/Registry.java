package com.jredfox.confighelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.potion.Potion;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;

public class Registry {
	
	public Map<Integer,List<Registry.Entry>> reg = new HashMap<Integer,List<Registry.Entry>>();
	/**
	 * for very fast grabbing of the original ids
	 */
	public Map<Integer,Integer> newToOld = new HashMap<Integer,Integer>();
	
	/**
	 * an automated integer
	 */
	public int id;
	public boolean strict;
	public DataType dataType;
	
	public Registry(){}
	public Registry(boolean strict, DataType dataType)
	{
		this.strict = strict;
		this.dataType = dataType;
	}
	
	public int reg(Object obj, int id)
	{
		id = this.getId(obj, id);
		int newId = RegistryConfig.autoConfig ? this.id++ : id;
		this.newToOld.put(newId, id);
		List<Entry> list = this.getEntry(id);
		if(list == null)
		{
			list = new ArrayList<Entry>();
			this.reg.put(id, list);
		}
		list.add(new Entry(getClass(obj), newId));
		if(list.size() > 1)
		{
			RegistryTracker.hasConflicts = true;
			if(!RegistryTracker.startup || this.strict)
			{
				RegistryTracker.output();
				CrashReport crashreport = CrashReport.makeCrashReport(new RuntimeException(this.dataType + " Id conflict has caused the game to crash " + "id:" + id + "=" + list.toString()), "In Game");
				crashreport.makeCategory("In Game");
		        Minecraft.getMinecraft().displayCrashReport(Minecraft.getMinecraft().addGraphicsAndWorldToCrashReport(crashreport));
			}
		}
		return newId;
	}
	
	public int getOriginalId(int newId) 
	{
		return this.newToOld.get((Integer)newId);
	}

	/**
	 * configure vanilla ids or auto config specific data types(biomes,potion,enchantments)
	 */
	protected int getId(Object obj, int org)
	{
		if(isVanillaObj(obj))
		{
			return RegistryVanillaConfig.getId(this.dataType, org);
		}
		return org;
	}
	
	public static boolean isVanillaObj(Object obj)
	{
		return getClass(obj).getName().startsWith("net.minecraft.");
	}
	
	public static Class getClass(Object entry)
	{
		if(entry instanceof Class)
			return (Class) entry;
		return entry.getClass();
	}
	
	public boolean isConflicting(int org)
	{
		return this.getEntry(org).size() > 1;
	}
	
	public List<Entry> getEntry(int org)
	{
		return this.reg.get(org);
	}
    
    public static enum DataType{
    	BIOME(),
    	ENCHANTMENT(),
    	POTION(),
    	DIMENSION(),
    	PROVIDER(),
    	ENTITY(),
    	DATAWATCHERPLAYER(),
    	ITEM(),
    	BLOCK();
    }
    
    public static class Entry
    {
    	public int newId;
    	public Class clazz;
    	public String name;
    	
    	public Entry(Class c, int newId)
    	{
    		this.clazz = c;
    		this.newId = newId;
    	}
    	
    	public void setName(String name)
    	{
    		this.name = name;
    	}
    	
    	@Override
    	public String toString()
    	{
    		return "name:" + this.name + ",newId:" + this.newId + ",class:" + this.clazz;
    	}
    	
    	@Override
    	public boolean equals(Object obj)
    	{
    		if(!(obj instanceof Entry))
    			return false;
    		return this.newId == ((Entry)obj).newId;
    	}
    	
    	@Override
    	public int hashCode()
    	{
    		return ((Integer)this.newId).hashCode();
    	}
    }

}

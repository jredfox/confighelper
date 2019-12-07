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
import net.minecraftforge.common.DimensionManager;

public class Registry {
	
	public Map<Integer,List<Registry.Entry>> reg = new LinkedHashMap<Integer,List<Registry.Entry>>();
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
	/**
	 * derp id
	 */
	public static final int megaTaigaHills = 161;
	public static final int megaTaiga = 160;
	
	public Registry(boolean strict, DataType dataType)
	{
		this.strict = strict;
		this.dataType = dataType;
		if(this.dataType == DataType.BIOME)
			newToOld.put(megaTaigaHills, megaTaiga);//work around for vanilla's der biomeArr[160]=biomeArr[161]...	
	}
	
	public int reg(Object obj, int id)
	{
		int newId = this.canAuto(obj, id) ? this.getFreeId(id) : id;
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
			if(this.canCrash())
			{
				RegistryTracker.output();
				String inGame = !RegistryTracker.startup ? "In Game" : "Loading";
				CrashReport crashreport = CrashReport.makeCrashReport(new RuntimeException(this.dataType + " Id conflict during " +  inGame + " id:" + id + "=" + list.toString()), inGame);
				crashreport.makeCategory(inGame);
		        Minecraft.getMinecraft().displayCrashReport(Minecraft.getMinecraft().addGraphicsAndWorldToCrashReport(crashreport));
			}
		}
		return newId;
	}
	
	/**
	 * returns whether or not it should crash on the first sign of conflict
	 */
	public boolean canCrash()
	{
		return !RegistryTracker.startup || this.strict;
	}

	/**
	 * grabs the next automatic id that works around vanilla ids usually
	 * warning this will automatically set your id counter and return the next free id
	 */
	protected int getFreeId(int org) 
	{
		Object[] arr = this.getStaticArr();
		if(arr != null)
		{
			for(int i=this.id;i<arr.length;i++)
			{
				if(arr[i] == null)
				{
					this.id = i;
					return this.id;
				}
			}
		}
		if(this.dataType == DataType.ENTITY)
		{
			while(true)
			{
				if(EntityList.getClassFromID(this.id) == null)
				{
					return this.id;
				}
				this.id++;
			}
		}
		return -1;
	}
	
	protected Object[] getStaticArr() 
	{
		if(this.dataType == DataType.BIOME)
			return BiomeGenBase.biomeList;
		if(this.dataType == DataType.POTION)
			return Potion.potionTypes;
		if(this.dataType == DataType.ENCHANTMENT)
			return Enchantment.enchantmentsList;
		return null;
	}
	
	/**
	 * is this mod allowed to auto configure the ids based on data type and/or mode
	 */
	public boolean canAuto(Object obj, int org) 
	{
		if(isVanillaObj(obj))
			return false;
		return RegistryConfig.autoConfig;
	}
	
	public int getOriginalId(int newId) 
	{
		return this.newToOld.get((Integer)newId);
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
    		return "{name:" + this.name + ",newId:" + this.newId + ",class:" + this.clazz + "}";
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

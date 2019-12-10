package com.jredfox.confighelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityList;
import net.minecraft.potion.Potion;
import net.minecraft.world.biome.BiomeGenBase;

public class Registry {
	
	public Map<Integer,List<Registry.Entry>> reg = new LinkedHashMap<Integer,List<Registry.Entry>>();
	public Set<Integer> vanillaIds = new HashSet();
	public int suggestedId;//the suggestedId index
	public int limit;
	
	/**
	 * an automated integer
	 */
	public int freeId;
	public DataType dataType;
	/**
	 * turn this on to auto crash at the first sign of conflict
	 */
	public boolean strict;
	
	public Registry(DataType dataType)
	{
		this.dataType = dataType;
		this.vanillaIds = getVanillaIds();
		this.limit = getLimit();
	}
	
	private int getLimit()
	{
		if(dataType == DataType.BIOME)
			return RegistryConfig.biomeLimit;
		else if(dataType == DataType.POTION)
			return RegistryConfig.potionsLimit;
		else if(dataType == DataType.ENCHANTMENT)
			return RegistryConfig.enchantmentsLimit;
		else if(dataType == DataType.PROVIDER || dataType == DataType.DIMENSION)
			return Integer.MAX_VALUE;
		else if(dataType == DataType.ENTITY)
			return RegistryConfig.enchantmentsLimit;
		else if(dataType == DataType.DATAWATCHER)
			return RegistryConfig.dataWatchersLimit;
		return -1;
	}
	
	private Set<Integer> getVanillaIds()
	{
		if(dataType == DataType.BIOME)
			return RegistryIds.biomes;
		else if(dataType == DataType.POTION)
			return RegistryIds.potions;
		else if(dataType == DataType.ENCHANTMENT)
			return RegistryIds.enchantments;
		else if(dataType == DataType.PROVIDER || dataType == DataType.DIMENSION)
			return RegistryIds.dimensions;
		else if(dataType == DataType.ENTITY)
			return RegistryIds.entities;
		else if(dataType == DataType.DATAWATCHER)
			return RegistryIds.datawatchers;
		return null;
	}

	public int reg(Object obj, int id)
	{
		List<Entry> list = this.getEntry(id);
		if(list == null)
		{
			list = new ArrayList<Entry>();
			this.reg.put(id, list);
		}
		int suggested = this.getSuggestedId(obj, id);
		Entry entry = new Entry(getClass(obj), id, suggested);
		list.add(entry);
		if(list.size() > 1)
		{
			entry.newId = this.getFreeId(id);//if it's a duplicate id transform it into a newId
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
		return entry.newId;
	}
	
	/**
	 * get the next virtual free id if you were going to write your modpack from scratch
	 */
	protected int getSuggestedId(Object obj, int org) 
	{
		if(this.isVanillaObj(obj, org))
			return org;
		for(int i=this.suggestedId;i<=this.limit;i++)
		{
			if(!this.isVanillaId(this.suggestedId))
			{
				return this.suggestedId++;
			}
			this.suggestedId++;
		}
		return -1;
	}
	
	public boolean isVanillaObj(Object obj, int id)
	{
		return getClass(obj).getName().startsWith("net.minecraft.");
	}

	public boolean isVanillaId(int id) 
	{
		return this.vanillaIds.contains((Integer)id);
	}

	/**
	 * returns whether or not it should crash on the first sign of conflict
	 */
	public boolean canCrash()
	{
		return !RegistryTracker.startup || this.strict;
	}

	/**
	 * a live look to grab the next free id
	 */
	public int getFreeId(int org)
	{
		for(int i=this.freeId; i <= this.limit; i++)
		{
			if(!this.containsId(this.freeId))
			{
				return this.freeId;
			}
			this.freeId++;
		}
		return -1;
	}
	
	/**
	 * not used in the root class of Registry
	 * doesn't support data watchers or dimensions use the designated registries for them
	 */
	public boolean containsId(int org)
	{
		if(this.dataType == DataType.ENTITY)
			return EntityList.IDtoClassMapping.containsKey(org);
		return this.getStaticReg()[org] != null;
	}
	
	protected Object[] getStaticReg() 
	{
		if(this.dataType == DataType.BIOME)
			return BiomeGenBase.biomeList;
		if(this.dataType == DataType.POTION)
			return Potion.potionTypes;
		if(this.dataType == DataType.ENCHANTMENT)
			return Enchantment.enchantmentsList;
		return null;
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
    	DATAWATCHER(),
    	ITEM(),
    	BLOCK();
    }
    
    public static class Entry
    {
    	public int org;//the original id
    	public int newId;//the id in memory
    	public int suggested;//the suggested id for the modpack creator to use
    	public Class clazz;
    	public String name;
    	
    	public Entry(Class c, int org, int suggestedId)
    	{
    		this.clazz = c;
    		this.org = org;
    		this.newId = org;
    		this.suggested = suggestedId;
    	}
    	
    	public void setName(String str)
    	{
    		this.name = str;
    	}
    	
    	@Override
    	public String toString()
    	{
    		return "(name:" + this.name + ",newId:" + this.newId + ",class:" + this.clazz.getName() + ")";
    	}
    	
    	public String getDisplay()
    	{
    		return "(name:" + this.name + ", " + this.clazz.getName() + ")";
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
    
    @Override
    public String toString()
    {
    	return this.reg.toString();
    }
    
    public String getDisplay(int id)
    {
    	StringBuilder builder = new StringBuilder();
    	builder.append('[');
    	List<Entry> list = this.reg.get(id);
    	for(Entry e : list)
    	{
    		builder.append(e.getDisplay() + ", ");
    	}
    	builder.append(']');
    	return builder.toString();
    }

}

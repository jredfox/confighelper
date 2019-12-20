package com.jredfox.confighelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jredfox.confighelper.proxy.ClientProxy;
import com.jredfox.confighelper.proxy.ServerProxy;

import cpw.mods.fml.common.Loader;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityList;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.potion.Potion;
import net.minecraft.world.biome.BiomeGenBase;

public class Registry {
	
	public Map<Integer,List<Registry.Entry>> reg = new LinkedHashMap<Integer,List<Registry.Entry>>();
	public Set<Integer> vanillaIds = new HashSet();//the full list of vanilla ids per Registry
	public int suggestedId;//the virtual suggested id index
	public int newId;//the newId(semi-auto) index
	public int limit;//the registry limit
	public DataType dataType;//the data type this registry is for
	public boolean strict;//turn this on to automatically crash on the first sign of conflict
	
	public Registry(DataType dataType)
	{
		this.dataType = dataType;
		this.vanillaIds = getVanillaIds();
		this.limit = getLimit();
	}
	
	protected int getLimit()
	{
		if(dataType == DataType.BIOME)
			return RegistryConfig.biomeLimit;
		else if(dataType == DataType.POTION)
			return RegistryConfig.potionsLimit;
		else if(dataType == DataType.ENCHANTMENT)
			return RegistryConfig.enchantmentsLimit;
		else if(dataType == DataType.PROVIDER || dataType == DataType.DIMENSION)
			return RegistryConfig.dimensionLimit;
		else if(dataType == DataType.ENTITY)
			return RegistryConfig.entities;
		else if(dataType == DataType.DATAWATCHER)
			return RegistryConfig.dataWatchersLimit;
		return -1;
	}
	
	protected Set<Integer> getVanillaIds()
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
		
		Class clazz = getClass(obj);
		boolean replaced = this.shouldReplace(clazz, id);
		boolean conflicting = !list.isEmpty() || this.containsId(id);//needs to look at the live ids because of the newIds automation
		Entry entry = new Entry(clazz, id, replaced);
		list.add(entry);
		
		if(conflicting && !replaced)
		{
			entry.newId = this.getNewId(id);
			if(list.size() > 1)
			{
				System.out.println(this.dataType + " conflcit found for id:" + entry.org + "=" + list);
			}
			else
			{
				System.out.println("id re-assigment:" + entry.org + ">" + entry.newId);
			}
			Registries.hasConflicts = true;
			if(this.canCrash())
			{
				Registries.output();
				String cat = !Registries.startup ? "In Game" : "Loading";
				Registries.makeCrashReport(cat, this.dataType + " Id conflict during " +  cat + " id:" + id + "=" + list.toString());
			}
		}
		return entry.newId;
	}
	
	public boolean shouldReplace(Class clazz, int id) 
	{
		String name = clazz.getName();
		for(String s : RegistryConfig.passable)
		{
			if(name.equals(s))
				return true;
		}
		return false;
	}
	
	/**
	 * get the next newId in case the original id is conflicting
	 */
	public int getNewId(int org)
	{
		for(int i=this.newId; i <= this.limit; i++)
		{
			if(!this.containsId(this.newId) && !this.isVanillaId(this.newId))
			{
				return this.newId;
			}
			this.newId++;
		}
		return -1;
	}
	
	public int freeId;
	/**
	 * returns the next free id for users to use in the config
	 * WARNING DO NOT CALL TILL AFTER THE REGISTRIES ARE DONE
	 */
	public int getNextFreeId(int newId)
	{
		for(int i=this.freeId; i <= this.limit; i++)
		{
			if(!this.containsOrg(this.freeId))
			{
				return this.freeId++;
			}
			this.freeId++;
		}
		return -1;
	}
	
	public int getNextSuggestedId(int newId)
	{
		if(this.vanillaIds.contains(newId))
			return newId;
		for(int i=this.suggestedId; i <= this.limit; i++)
		{
			if(!this.isVanillaId(this.suggestedId))
			{
				return this.suggestedId++;
			}
			this.suggestedId++;
		}
		return -1;
	}
	
	public void resetFreeIds()
	{
		this.freeId = 0;
	}
	
	public boolean containsOrg(int org)
	{
		return this.reg.containsKey(org);
	}

	public boolean isVanillaId(int id) 
	{
		return this.vanillaIds.contains(id);
	}

	/**
	 * returns whether or not it should crash on the first sign of conflict
	 */
	public boolean canCrash()
	{
		return !Registries.startup || this.strict;
	}
	
	/**
	 * a live look to see if the id is in memory
	 */
	public boolean containsId(int newId)
	{
		for(List<Registry.Entry> li : this.reg.values())
		for(Registry.Entry entry : li)
		{
			if(entry.newId == newId)
				return true;
		}
		return false;
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
	
	/**
	 * is the original requested id conflicting with another original conflicted id
	 */
	public boolean isConflicting(int org)
	{
		return this.getEntry(org).size() > 1;
	}
	
	public List<Entry> getEntry(int org)
	{
		return this.reg.get(org);
	}
	
   	public String getDisplay(Registry.Entry e)
	{
		return "(name:" + e.name + ", " + (e.modName != null ? "mod:" + e.modName + ", " : "") + e.clazz.getName() + ", orgId:" + e.org + ")";
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
    	BLOCK(),
    	TILEENTITY();
    }
    
    public static class Entry
    {
    	public int org;//the original id
    	public int newId;//the id in memory
    	public boolean replaced;
    	public Class clazz;
    	public String name;
    	public String modName;
    	
    	public Entry(Class c, int org, boolean shouldReplace)
    	{
    		this.clazz = c;
    		this.modName = Registries.getModName(c);
    		this.org = org;
    		this.newId = org;
    		this.replaced = shouldReplace;
    	}
    	
    	public void setName(String str)
    	{
    		this.name = "" + str;
    	}
    	
    	@Override
    	public String toString()
    	{
    		return "(name:" + this.name + ",newId:" + this.newId + ",class:" + this.clazz.getName() + ")";
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

}

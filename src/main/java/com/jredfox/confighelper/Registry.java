package com.jredfox.confighelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.evilnotch.lib.util.JavaUtil;
import com.google.common.base.Objects;
import com.jredfox.confighelper.Registry.DataType;

import cpw.mods.fml.common.Loader;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityList;
import net.minecraft.potion.Potion;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenMutated;

public class Registry {
	
	public Map<Integer,List<Registry.Entry>> reg = new LinkedHashMap<Integer,List<Registry.Entry>>();
	public Set<Integer> vanillaIds = new HashSet();//the full list of vanilla ids per Registry
	public int limit;//the registry limit
	public int limitLower;
	public DataType dataType;//the data type this registry is for
	public boolean strict;//turn this on to automatically crash on the first sign of conflict
	
	public Registry(DataType dataType)
	{
		this.dataType = dataType;
		this.vanillaIds = getVanillaIds();
		this.limit = getLimit();
		this.limitLower = this.getLimitLower();
	}
	
	protected int getLimitLower()
	{
		if(this.dataType == DataType.DIMENSION || this.dataType == DataType.PROVIDER)
			return RegistryConfig.dimLimitLower;
		return 0;
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
			return RegistryConfig.dimLimitUpper;
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
		
		String clazz = getClass(obj).getName();
		boolean conflicting = this.containsId(id);
		Entry entry = new Entry(this.dataType, obj, clazz, id);
		if(this.isPassableSelf(clazz) && list.contains(entry))
		{
			Registry.Entry old = list.get(list.indexOf(entry));
			System.out.println("Self Conflict Found:" + id + " class:" + old.clazz);
			return old.newId;
		}
		list.add(entry);
		
		if(conflicting)
		{
			if(this.isPassable(clazz, id))
			{
				System.out.println("replacing index:" + id + " class:" + clazz);
				entry.replaced = true;
				return entry.newId;
			}
			entry.newId = this.getNewId(id);
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
	
	/**
	 * get used org Ids in order from least to greatest
	 */
	public Set<Integer> getOrgIds()
	{
		return new TreeSet(this.reg.keySet());
	}
	
	public Set<Integer> getNewIds()
	{
		Set<Integer> newIds = new TreeSet();
		for(List<Registry.Entry> list : this.reg.values())
			for(Registry.Entry entry : list)
				newIds.add(entry.newId);
		return newIds;
	}
	
	public List<Registry.Entry> getAllEntries()
	{
		List<Registry.Entry> list = new ArrayList();
		for(List<Registry.Entry> entries : this.reg.values())
			list.addAll(entries);
		return list;
	}
	
	public boolean isPassable(String clazz, int id) 
	{
		return JavaUtil.contains(RegistryConfig.passable, clazz);
	}
	               
	public boolean isPassableSelf(String clazz)
	{
		return JavaUtil.contains(RegistryConfig.passableSelf, clazz);
	}
	
	public int newId;//the newId(semi-auto) index
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
	
	public int suggestedId;//the virtual suggested id index
	public int getNextSuggestedId(int newId)
	{
		if(this.isVanillaId(newId))
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
	
	public static Class getClass(Object entry)
	{
		if(entry instanceof EntryEntity)
			return ((EntryEntity)entry).clazz;
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
	
   	public String getDisplay(Registry.Entry e, boolean name)
	{
		return "(name:" + e.name + ", " + (name ? e.modName + ", " : "") + e.clazz + ", orgId:" + e.org + ")";
	}
   	
   	/**
   	 * grabs the registry names and mod names of the Registry.Entry objects
   	 */
   	public void grabNames()
   	{
   		this.setNames();
   		this.setModNames();
   	}
   	
   	public void setNames()
   	{
   		for(List<Registry.Entry> li : this.reg.values())
   			for(Registry.Entry e : li)
   				e.setName();
   	}
   	
   	public void setModNames()
   	{
   		for(List<Registry.Entry> li : this.reg.values())
   			for(Registry.Entry e : li)
   				e.setModName();
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
    	
		public String getName() 
		{
			return this.getName(true);
		}

		public String getName(boolean plural) 
		{
			String str = this.toString().toLowerCase();
			if(plural)
			{
				if(str.endsWith("y"))
					str = str.substring(0, str.length()-1) + "ies";
				else
					str += "s";
			}
			return str;
		}
    }
    
    public static class Entry
    {
    	public DataType dataType;//the dataType of the object
    	public Object obj;//the object to register
    	public int org;//the original id
    	public int newId;//the id in memory
    	public boolean replaced;//if it replaces an index in memory
    	public String clazz;// the class of the object may be wrong if it's a wrapper class
    	public String name;//display name
    	public String modName;//display mod name
    	
    	public Entry(DataType type, Object obj, String c, int org)
    	{
       		this.dataType = type;
    		this.obj = obj;
    		this.clazz = c;
    		this.org = org;
    		this.newId = org;
    	}
    	
    	public void setName()
    	{
    		this.name = "" + this.grabName();
    	}
    	
    	private String grabName()
    	{
    		try
    		{
    			if(this.dataType == DataType.BIOME)
    			{
    				return ((BiomeGenBase)this.obj).biomeName;
    			}
    			else if(this.dataType == DataType.POTION)
    			{
    				return ((Potion)this.obj).getName();
    			}
    			else if(this.dataType == DataType.ENCHANTMENT)
    			{
    				return ((Enchantment)this.obj).getName();
    			}
    			else if(this.dataType == DataType.PROVIDER)
    			{
    				WorldProvider provider = (WorldProvider) ((Class)this.obj).newInstance();
    				return provider.getDimensionName();
    			}
    			else if(this.dataType == DataType.DIMENSION)
    			{
    				return Registries.dimensions.isVanillaId(this.newId) ? "vanilla" : "modded";
    			}
    			else if(this.dataType == DataType.ENTITY)
    			{
    				return ((EntryEntity)this.obj).name;
    			}
    			else if(this.dataType == DataType.DATAWATCHER)
    			{
    				return Registries.datawatchers.isVanillaId(this.newId) ? "vanilla" : "modded";
    			}
    		}
    		catch(Throwable t)
    		{
    			t.printStackTrace();
    		}
    		return null;
    	}
    	
		public void setModName()
		{
			this.modName = Registries.getModName(this.getDataTypeClass());
		}
    	
    	public String getDataTypeClass() 
    	{
			if(this.obj instanceof BiomeGenBase)
			{
				return ((BiomeGenBase)this.obj).getBiomeClass().getName();
			}
			return this.clazz;
		}

		@Override
    	public String toString()
    	{
    		return "(name:" + this.name + ",newId:" + this.newId + ",class:" + this.clazz + ")";
    	}
    	
    	@Override
    	public boolean equals(Object obj)
    	{
    		if(!(obj instanceof Entry))
    			return false;
    		Entry e = (Entry)obj;
    		return this.org == e.org && this.clazz.equals(e.clazz);
    	}
    	
    	@Override
    	public int hashCode()
    	{
    		return Objects.hashCode(this.org, this.clazz);
    	}
    }
    
    @Override
    public String toString()
    {
    	return this.reg.toString();
    }

}

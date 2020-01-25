package jml.confighelper.reg;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;

import jml.confighelper.RegistryConfig;
import jml.confighelper.datawatcher.WatcherDataType;
import jml.evilnotch.lib.JavaUtil;
import jml.evilnotch.lib.reflect.ReflectionHandler;
import net.minecraft.block.Block;
import net.minecraft.client.gui.Gui;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.DimensionManager;

public class Registry implements Iterable<Registry.Entry>{
	
	public Map<Integer,List<Registry.Entry>> reg = new LinkedHashMap<Integer,List<Registry.Entry>>();
	public Set<Integer> vanillaIds = new HashSet();//the full list of vanilla ids per Registry
	public int limit;//the registry limit
	public int limitLower;
	public DataType dataType;//the data type this registry is for
	public boolean strict;//turn this on to automatically crash on the first sign of conflict
	public boolean hasConflicts;
	
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
			return RegistryIds.limitDimLower;
		return 0;
	}

	protected int getLimit()
	{
		if(dataType == DataType.BIOME)
			return RegistryIds.limitBiomes;
		else if(dataType == DataType.POTION)
			return RegistryIds.limitPotions;
		else if(dataType == DataType.ENCHANTMENT)
			return RegistryIds.limitEnchantments;
		else if(dataType == DataType.PROVIDER || dataType == DataType.DIMENSION)
			return RegistryIds.limitDim;
		else if(dataType == DataType.ENTITY)
			return RegistryIds.limitEntities;
		else if(dataType == DataType.DATAWATCHER)
			return RegistryIds.limitDatawatchers;
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
		this.securityCheck();
		this.checkId(obj, id);
		List<Registry.Entry> list = this.getEntryOrg(id);
		if(list == null)
		{
			list = new ArrayList<Registry.Entry>();
			this.reg.put(id, list);
		}
		
		String clazz = getClass(obj).getName();
		boolean conflicting = this.containsId(id);
		Entry entry = new Entry(obj, clazz, id);
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
			this.checkId(obj, entry.newId);
			this.hasConflicts = true;
			if(this.canCrash())
			{
				Registries.write();
				String cat = Registries.getCat();
				Registries.makeCrashReport(cat, this.dataType + " Id conflict during " +  cat + " id:" + id + "=" + list.toString());
			}
		}
		return entry.newId;
	}
	
	public void securityCheck() 
	{
		int size = this.size();
		Object[] arr = this.getStaticArray();
		if(arr != null && arr.length != size)
		{
		    System.out.println("Patching " + this.dataType + "[] as it's size doesn't match:\t" + arr.length + " > " + size);
			Object[] newArray = (Object[]) Array.newInstance(this.dataType.clazz, size);
			for(Registry.Entry e : this)
			{
				System.out.println("assigning obj:" + e.obj + " > " + e.newId);
				newArray[e.newId] = this.dataType.clazz.cast(e.obj);
			}
			this.setStaticArray(newArray);
		}
	}
	
	private void setStaticArray(Object[] newArray) 
	{
		if(this.dataType == DataType.BIOME)
			BiomeGenBase.biomeList = (BiomeGenBase[]) newArray;
		else if(this.dataType == DataType.POTION)
			Potion.potionTypes = (Potion[]) newArray;
		else if(this.dataType == DataType.ENCHANTMENT)
			Enchantment.enchantmentsList = (Enchantment[]) newArray;
	}

	public Object[] getStaticArray()
	{
		if(this.dataType == DataType.BIOME)
			return BiomeGenBase.biomeList;
		else if(this.dataType == DataType.POTION)
			return Potion.potionTypes;
		else if(this.dataType == DataType.ENCHANTMENT)
			return Enchantment.enchantmentsList;
		return null;
	}

	public int size()
	{
		return this.limit + 1;
	}

	public void checkId(Object obj, int id)
	{
		if(id < this.limitLower || id > this.limit)
			Registries.makeCrashReport(Registries.getCat(), this.dataType + " ids must be between " + this.limitLower + "-" + this.limit + " id:" + id + ", " + getClass(obj).getName());
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
		Set<Integer> newIds = new TreeSet<Integer>();
		for(Registry.Entry entry : this)
				newIds.add(entry.newId);
		return newIds;
	}
	
	public boolean isPassable(String clazz, int id) 
	{
		return JavaUtil.contains(RegistryConfig.passable, clazz);
	}
	               
	public boolean isPassableSelf(String clazz)
	{
		return JavaUtil.contains(RegistryConfig.passableSelf, clazz);
	}
	
	public int getNewId()
	{
		return this.getNewId(true);
	}
	
	public int getNewId(boolean positive)
	{
		int id = positive ? 0 : -1;
		return this.getNewId(id);
	}
	
	public int newId;//the newId(semi-auto) index
	/**
	 * gets the next id
	 */
	public int getNewId(int org)
	{
		for(int i=this.newId; i <= this.limit; i++)
		{
			if(!this.containsId(this.newId) && !this.isVanillaId(this.newId))
			{
				return this.newId++;
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
	
	public void resetSuggestedIds()
	{
		this.suggestedId = 0;
	}
	
	public void resetFreeIds()
	{
		this.freeId = 0;
	}
	
	/**
	 * resets the suggested ids and the free ids
	 */
	public void resetInfoIds()
	{
		this.resetSuggestedIds();
		this.resetFreeIds();
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
		for(Registry.Entry entry : this)
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
		return this.strict;
	}
	
	public Class getClass(Object entry)
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
		return this.getEntryOrg(org).size() > 1;
	}
	
	/**
	 * returns an array of entries that are linked to the original requested id.
	 * if there is more then one it is conflicting
	 */
	public List<Registry.Entry> getEntryOrg(int org)
	{
		return this.reg.get(org);
	}
	
	/**
	 * get a registry entry based on newId
	 */
	public Registry.Entry getEntry(int newId)
	{
		for(Registry.Entry e : this)
			if(e.newId == newId)
				return e;
		return null;
	}
	
	/**
	 * get a list view of the Registry that can be sorted without messing with indexes here
	 */
	public List<Entry> getEntriesSortable() 
	{
		List<Registry.Entry> list = new ArrayList();
		for(Registry.Entry entry : this)
			list.add(entry);
		return list;
	}
	
	/**
	 * unregisters all ids attached to the newId
	 * if there is an intended conflict the newId will equal the org id and will occur multiple times
	 */
	public void unreg(int newId)
	{
		unreg(newId, true);
	}
	
	/**
	 * unregisters the first instanceof of the entry it finds
	 */
	public void unregFirst(int newId)
	{
		unreg(newId, false);
	}
	
	protected void unreg(int newId, boolean all)
	{
		for(List<Registry.Entry> li : this.reg.values())
		{
			Iterator<Registry.Entry> it = li.iterator();
			while(it.hasNext())
			{
				Registry.Entry e = it.next();
				if(e.newId == newId)
				{
					it.remove();
					if(!all)
						return;
				}
			}
		}
	}
	
   	public String getDisplay(Registry.Entry e)
	{
   		return this.getDisplay(e, false);
	}
	
   	public String getDisplay(Registry.Entry e, boolean name)
	{
		return "(" + (e.replaced ? "replaced:" + e.replaced + ", " : "") + "name:" + e.name + ", " + (name ? e.modName + ", " : "") + e.clazz + ", orgId:" + e.org + ")";
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
   		for(Registry.Entry e : this)
   			e.setName(this.grabName(e));
   	}
   	
   	public void setModNames()
   	{
   		for(Registry.Entry e : this)
			e.setModName();
   	}
   	
	protected String grabName(Registry.Entry entry)
	{
		try
		{
			if(this.dataType == DataType.BIOME)
			{
				return ((BiomeGenBase)entry.obj).biomeName;
			}
			else if(this.dataType == DataType.POTION)
			{
				return ((Potion)entry.obj).getName();
			}
			else if(this.dataType == DataType.ENCHANTMENT)
			{
				return ((Enchantment)entry.obj).getName();
			}
			else if(this.dataType == DataType.PROVIDER)
			{
				WorldProvider provider = (WorldProvider) ((Class)entry.obj).newInstance();
				try
				{
					int dimOrgId = Registries.guessDimOrgId(entry.newId);
					provider.setDimension(dimOrgId);
				}
				catch(Throwable t)
				{
					t.printStackTrace();
				}
				return provider.getDimensionName();
			}
			else if(this.dataType == DataType.DIMENSION)
			{
				return Registries.dimensions.isVanillaId(entry.newId) ? "vanilla" : "modded";
			}
			else if(this.dataType == DataType.ENTITY)
			{
				return ((EntryEntity)entry.obj).name;
			}
			else if(this.dataType == DataType.DATAWATCHER)
			{
				return Registries.datawatchers.isVanillaId(entry.newId) ? "vanilla" : "modded";
			}
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
		return null;
	}
    
    public static enum DataType{
    	BIOME(BiomeGenBase.class),
    	ENCHANTMENT(Enchantment.class),
    	POTION(Potion.class),
    	DIMENSION(DimensionManager.class),
    	PROVIDER(DimensionManager.class),
    	ENTITY(Entity.class),
    	DATAWATCHER(DataWatcher.class),
    	DATAWATCHERTYPE(WatcherDataType.class),
    	ITEM(Item.class),
    	BLOCK(Block.class),
    	TILEENTITY(TileEntity.class),
    	RECIPES(IRecipe.class),
    	GUI(ReflectionHandler.getClass("net.minecraft.client.gui.Gui"));//if you need another enum type use ReflectionHandler.addEnum when registering them
    	
    	public Class clazz;
    	private DataType(Class clazz)
    	{
    		this.clazz = clazz;
    	}
    	
		public String getName() 
		{
			return this.getName(true);
		}

		public String getName(boolean plural) 
		{
			String str = this.toString().toLowerCase();
			if(plural)
			{
				if(this == DataType.ENTITY)
					str = str.substring(0, str.length()-1) + "ies";
				else
					str += "s";
			}
			return str;
		}
    }
    
    public static class Entry
    {
    	public Object obj;//the object to register
    	public int org;//the original id
    	public int newId;//the id in memory
    	public boolean replaced;//if it replaces an index in memory
    	public String clazz;// the class of the object may be wrong if it's a wrapper class
    	public String name;//display name
    	public String modName;//display mod name
    	
    	public Entry(Object obj, String c, int org)
    	{
    		this.obj = obj;
    		this.clazz = c;
    		this.org = org;
    		this.newId = org;
    	}
    	
    	public void setName(String name)
    	{
    		this.name = "" + name;
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

	@Override
	public Iterator<Registry.Entry> iterator() 
	{
		return Iterables.concat(this.reg.values()).iterator();
	}
}

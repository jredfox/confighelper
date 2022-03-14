package com.jredfox.crashwconflicts.reg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jredfox.crashwconflicts.CrashWConflicts;
import com.jredfox.crashwconflicts.Passable;
import com.jredfox.crashwconflicts.reg.Registry.RegEntry;
import com.jredfox.util.RegTypes;
import com.jredfox.util.RegUtils;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.registry.EntityRegistry;
import net.minecraft.item.ItemBlock;

public class Registry {
	
	public static Registry items = new Registry(RegTypes.ITEM);
	public static Registry blocks = new Registry(RegTypes.BLOCK);
	public static Registry biomes = new Registry(RegTypes.BIOME);
	public static Registry enchantments = new Registry(RegTypes.ENCHANTMENT);
	public static Registry potions = new Registry(RegTypes.POTION);
	public static Registry entities = new Registry(RegTypes.ENTITY);
	public static Registry dimensions = new Registry(RegTypes.DIMENSION);
	public static Registry providers = new Registry(RegTypes.PROVIDER);
	public static List<Registry> regs = new ArrayList();//the registry of registries
	
	public Map<Integer, Set<RegEntry>> registered = new LinkedHashMap();
	public Set<Integer> orgIds = new HashSet();
	public Set<Integer> ghosting = new HashSet();
	public static Set<Passable> passables = new HashSet();//a global list of passable objects
	public RegTypes type;
	public int min;
	public int max;
	public int index;
	
	public Registry(RegTypes type)
	{
		this.type = type;
		this.min = RegUtils.getMin(type, true);
		this.max = RegUtils.getMax(type, true);
		this.index = this.max;
		this.regs.add(this);
	}
	
	public boolean isConflicted(int id)
	{
		return this.getReg(id).size() > 1;
	}
	
	public Set<RegEntry> getReg(int id)
	{
		Set<RegEntry> arr = this.registered.get(id);
		if(arr == null)
		{
			arr = new LinkedHashSet();
			this.registered.put(id, arr);
		}
		return arr;
	}
	
	public <T> int reg(int id, T obj, Object arr)
	{
		this.orgIds.add(id);
		if(this.isGhosted(id))
			return this.regDirect(id, new RegEntry(id, obj));//don't register as a ghost as this isn't a ghost but the slot that is reg is a ghost
		else if(this.isPassable(RegUtils.unshiftId(this.type, id), RegUtils.getOClass(obj)))
			return this.regPassable(id, obj);
		Set<RegEntry> entries = this.getReg(id);
		return entries.isEmpty() ? this.regDirect(id, new RegEntry(id, obj)) : this.regNextId(id, obj, arr);
	}

	public <T> int regNextId(int orgId, T obj, Object arr)
	{
		CrashWConflicts.hasConflicts = true;
		switch(this.type)
		{
			case ENTITY:
				return this.regNextIdMap(orgId, obj, (Map) arr);
			case PROVIDER:
				return this.regNextIdMap(orgId, obj, (Map) arr);
			case DIMENSION:
				return this.regNextIdMap(orgId, obj, (Map) arr);
			default:
				return this.regNextId(orgId, obj, (T[]) arr);
		}
	}

	public <T> int regNextIdMap(int orgId, T obj, Map<Integer, ?> arr)
	{
    	for(int i = this.index ; i>=this.min; i--)
    	{
			if(i < this.min)
				break;
    		if(!arr.containsKey(i))
    			return this.regGhost(i, orgId, obj);
    		this.index--;
    	}
    	throw new RuntimeException("out of free ids for:" + this.type + "!");
	}

	public <T> int regNextId(int orgId, T obj, T[] arr)
	{
		for(int i=this.index;i>=0;i--)
		{
			if(i < this.min)
				break;
			if(arr[i] == null)
				return this.regGhost(i, orgId, obj);
			this.index--;
		}
		throw new RuntimeException("out of free ids for:" + this.type + "!");
	}
	
	protected <T> int regGhost(int id, int orgId, T obj) 
	{
		this.ghosting.add(id);
		RegEntry entry = new RegEntry(id, orgId, obj);
		entry.isGhost = true;
		return this.regDirect(id, entry);
	}
	
	protected <T> int regPassable(int id, T obj) 
	{
		RegEntry entry = new RegEntry(id, obj);
		entry.passable = true;
		return this.regDirect(id, entry);
	}

	protected <T> int regDirect(int id, RegEntry entry) 
	{
		this.getReg(id).add(entry);
		return id;
	}
	
	/**
	 * returns if the id occupied in in memory is an auto id(ghost) or a real one
	 */
	public boolean isGhosted(int id)
	{
		return this.ghosting.contains(id) && this.getReg(id).size() < 2;
	}

	public boolean isPassable(int id, Class<?> nc)
	{
		boolean p = this.passables.contains(new Passable(id, nc.getName(), Registry.getMod().getModId()));
		if(p)
			System.out.println("skipping passable:" + id + " " + nc);
		return p;
	}
	
	public static ModContainer getMod()
	{
		ModContainer mc = Loader.instance().activeModContainer();
		return mc != null ? mc : Loader.instance().getMinecraftModContainer();
	}
	
	public class RegEntry
	{
		public int id;
		public int orgId;
		public Class<?> oClass;
		public Object obj;
		public String name;//will be null until it's time to write conflicts
		public String modid;
		public String modname;
		public boolean isGhost;
		public boolean passable;
		public static final String MINECRAFT = "minecraft";
		
		public RegEntry(int id, Object obj)
		{
			this(id, id, obj);
		}
		
		public RegEntry(int id, int orgId, Object obj)
		{
			this.id = id;
			this.orgId = orgId;
			this.oClass = RegUtils.getOClass(obj);
			this.obj = obj;
			ModContainer mc = Registry.getMod();
			this.modid = mc.getModId();
			this.modname = mc.getName();
		}
		
		@Override
		public int hashCode()
		{
			return this.id;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if(!(obj instanceof RegEntry))
				return false;
			RegEntry o = (RegEntry)obj;
			return this.id == o.id && this.orgId == o.orgId && this.oClass.equals(o.oClass) && this.modid.equals(o.modid);
		}

		public String getName()
		{
			try
			{
				return RegUtils.getName(this.id, Registry.this.type, this.obj);
			}
			catch(Throwable t)
			{
				return t.getClass().getName();
			}
		}
	}

	public boolean hasItemBlock(Set<RegEntry> entries) 
	{
		for(RegEntry e : entries)
			if(e.obj instanceof ItemBlock)
				return true;
		return false;
	}
}

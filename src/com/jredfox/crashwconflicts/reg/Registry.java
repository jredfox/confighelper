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
import com.jredfox.util.RegTypes;
import com.jredfox.util.RegUtils;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.registry.EntityRegistry;
import net.minecraft.item.ItemBlock;

public class Registry {
	
	//TODO: ghosted, itemBlock flag
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
		if(this.isGhosted(id) || this.isPassable(RegUtils.unshiftId(this.type, id), RegUtils.getOClass(obj)))
			return this.regDirect(id, obj);
		Set<RegEntry> entries = this.getReg(id);
		return entries.isEmpty() ? this.regDirect(id, obj) : this.regNextId(obj, arr);
	}

	public <T> int regNextId(T obj, Object arr)
	{
		CrashWConflicts.hasConflicts = true;
		switch(this.type)
		{
			case ENTITY:
				return this.regNextIdMap(obj, (Map) arr);
			case PROVIDER:
				return this.regNextIdMap(obj, (Map) arr);
			case DIMENSION:
				return this.regNextIdMap(obj, (Map) arr);
			default:
				return this.regNextId(obj, (T[]) arr);
		}
	}

	public <T> int regNextIdMap(T obj, Map<Integer, ?> arr)
	{
    	for(int i = this.index ; i>=this.min; i--)
    	{
    		if(!arr.containsKey(i))
    		{
    			if(i < this.min)
    				break;
    			this.ghosting.add(i);
    			return this.regDirect(i, obj);
    		}
    		this.index--;
    	}
    	throw new RuntimeException("out of free ids for:" + this.type + "!");
	}

	public <T> int regNextId(T obj, T[] arr) 
	{
		for(int i=this.index;i>=0;i--)
		{
			if(i < this.min)
				break;
			if(arr[i] == null)
			{
				this.ghosting.add(i);
				return this.regDirect(i, obj);
			}
			this.index--;
		}
		throw new RuntimeException("out of free ids for:" + this.type + "!");
	}

	protected <T> int regDirect(int id, T obj) 
	{
		this.getReg(id).add(new RegEntry(obj));
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
		public Class<?> oClass;
		public String unlocal;
		public String modid;
		public String modname;
		public static final String MINECRAFT = "minecraft";
		
		public RegEntry(Object obj)
		{
			this.oClass = RegUtils.getOClass(obj);
			ModContainer mc = Registry.getMod();
			this.modid = mc.getModId();
			this.modname = mc.getName();
			this.unlocal = this.modid.equals(MINECRAFT) ? "" : ((IUnlocalizedName)obj).getUnlocalizedName();
		}
	}
	
	public interface IUnlocalizedName
	{
		public String getUnlocalizedName();
	}
}

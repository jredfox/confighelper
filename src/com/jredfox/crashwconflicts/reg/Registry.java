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
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

public class Registry {
	
	public static Registry items = new Registry(RegTypes.ITEM);
	public static Registry blocks = new Registry(RegTypes.BLOCK);
	public static Registry biomes = new Registry(RegTypes.BIOME);
	public static Registry enchantments = new Registry(RegTypes.ENCHANTMENT);
	public static Registry potions = new Registry(RegTypes.POTION);
	
	//TODO: get these bad bois working
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
	
	public <T> int reg(int id, T obj, T[] arr)
	{
		this.orgIds.add(id);
		if(this.isGhosted(id) || this.isPassable(RegUtils.unshiftId(this.type, id), RegUtils.getOClass(obj)))
			return this.reg(id, obj);
		Set<RegEntry> entries = this.getReg(id);
		return entries.isEmpty() ? this.reg(id, obj) : this.regNextId(obj, arr);
	}

	public <T> int regNextId(T obj, T[] arr) 
	{
		CrashWConflicts.hasConflicts = true;
		for(int i=this.index;i>=0;i--)
		{
			if(i < this.min)
				break;
			if(arr[i] == null)
			{
				this.ghosting.add(i);
				return this.reg(i, obj);
			}
			this.index--;
		}
		throw new RuntimeException("out of free ids for:" + this.type + "!");
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

	protected <T> int reg(int id, T obj) 
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

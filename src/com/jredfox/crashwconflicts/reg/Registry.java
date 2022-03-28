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
import com.jredfox.crashwconflicts.cfg.ConfigVarBlock;
import com.jredfox.crashwconflicts.cfg.ConfigVarItem;
import com.jredfox.util.RegTypes;
import com.jredfox.util.RegUtils;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import net.minecraft.item.ItemBlock;

public class Registry {
	
	public static List<Registry> regs = new ArrayList();//the registry of registries
	public static Registry items = new Registry(RegTypes.ITEM);
	public static Registry blocks = new Registry(RegTypes.BLOCK);
	public static Registry biomes = new Registry(RegTypes.BIOME);
	public static Registry enchantments = new Registry(RegTypes.ENCHANTMENT);
	public static Registry potions = new Registry(RegTypes.POTION);
	public static Registry entities = new Registry(RegTypes.ENTITY);
	public static Registry dimensions = new Registry(RegTypes.DIMENSION);
	public static Registry providers = new Registry(RegTypes.PROVIDER);
	
	public Map<Integer, Set<RegEntry>> registered = new LinkedHashMap();
	public Set<Integer> orgIds = new HashSet();
	public List<Integer> bl = RegUtils.asArr(new int[]{Short.MAX_VALUE});//blacklisted ids ItemStack GameRegistry
	public Set<Integer> unconfiguredIds = new HashSet();//TODO: a list of configured ids to avoid when using the autoconfig
	public static Set<Passable> passables = new HashSet();//a global list of passable objects
	public RegTypes type;
	public int min;
	public int max;
	public int index;
	public boolean hasConflicts;//TODO: in crash report display what registries are crashing
	public boolean initMc;//returns false if vanilla isn't pre-initialized
	
	public Registry(RegTypes type)
	{
		this.type = type;
		this.regs.add(this);
	}
	
	protected boolean hasInit;
	/**
	 * don't initialize in clinit as that will cause initialization exceptions for example item class tries to load before clinit is done on block class
	 */
	protected void init()
	{
		this.min = RegUtils.getMin(this.type, true);
		this.max = RegUtils.getMax(this.type, true);
		this.index = this.max;
		this.hasInit = true;
	}
	
	public boolean isConflicted(int id)
	{
		return this.getReg(id).size() > 1;
	}
	
	/**
	 * @return true if it's only a passable conflict
	 */
	public boolean isPassableConflict(int id)
	{
		Set<RegEntry> li = this.getReg(id);
		RegEntry first = RegUtils.getFirst(li);
		for(RegEntry r : li)
			if(r != first && !r.passable)
				return false;
		return li.size() > 1;
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
		this.sanityCheck(id);
		this.orgIds.add(id);
		this.regUncfg(id, obj);
		if(this.isPassable(id, RegUtils.getOClass(obj), arr))
			return this.regPassable(id, obj);
		Set<RegEntry> entries = this.getReg(id);
		return entries.isEmpty() ? this.regDirect(new RegEntry(id, obj)) : this.regNextId(id, obj, arr);
	}

	public <T> void regUncfg(int id, T obj)
	{
		if(this.type == RegTypes.ITEM)
		{
			if(!ConfigVarItem.mark_items[id])
			{
				this.unconfiguredIds.add(id);
				if(this.getMod() != vmc && !(obj instanceof ItemBlock))
					System.err.println("unconfigured id:" + this.type + " " + this.getMod().getName() + " id:" + id + " " + obj.getClass().getName());
			}
		}
		else if(this.type == RegTypes.BLOCK)
		{
			if(!ConfigVarBlock.mark_blocks[id])
			{
				this.unconfiguredIds.add(id);
				if(this.getMod() != vmc)
					System.err.println("unconfigured id:" + this.type + " " + this.getMod().getName() + " id:" + id + " " + obj.getClass().getName());
			}
		}
	}

	public void sanityCheck(int id) 
	{
		if(this.bl.contains(id))
			throw new RuntimeException("invalid id for:" + this.type + " id:" + id);
		if(!this.hasInit || this.max != RegUtils.getMax(this.type))
			this.init();//TODO: when ids are extended protect all vanilla static array's data from bad mods(either improperly extending ids or on purpose). but since I don't extend them yet most I can do is re-sync the init
	}

	public <T> int regNextId(int orgId, T obj, Object arr)
	{
		CrashWConflicts.hasConflicts = true;
		this.hasConflicts = true;
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
    	for(int i = this.index; i >= this.min; i--)
    	{
			if(i < this.min)
				break;
    		if(!arr.containsKey(i) && !this.bl.contains(i))
    			return this.regAuto(i, orgId, obj);
    		this.index--;
    	}
    	throw new RuntimeException("out of free ids for:" + this.type + "!");
	}

	public <T> int regNextId(int orgId, T obj, T[] arr)
	{
		for(int i = this.index; i >= 0; i--)
		{
			if(i < this.min)
				break;
			if(arr[i] == null && !this.bl.contains(i))
				return this.regAuto(i, orgId, obj);
			this.index--;
		}
		throw new RuntimeException("out of free ids for:" + this.type + "!");
	}
	
	protected <T> int regAuto(int id, int orgId, T obj) 
	{
		RegEntry entry = new RegEntry(id, orgId, obj);
		return this.regDirect(entry);
	}
	
	protected <T> int regPassable(int id, T obj) 
	{
		RegEntry entry = new RegEntry(id, obj);
		entry.passable = true;
		return this.regDirect(entry);
	}

	protected <T> int regDirect(RegEntry entry) 
	{
		this.getReg(entry.orgId).add(entry);
		return entry.newId;
	}

	/**
	 * determines whether or not an object is allowed to override as an intended conflict
	 */
	public boolean isPassable(int orgId, Class<?> nc, Object arr)
	{
		int unshifted = RegUtils.unshiftId(this.type, orgId);
		boolean p = this.passables.contains(new Passable(unshifted, nc.getName(), this.getMod().getModId())) || CrashWConflicts.passableNullables && this.registered.containsKey(orgId) && isNull(orgId, arr);
		if(p)
			System.out.println("skipping passable:" + unshifted + " " + nc);
		return p;
	}
	
	public <T> boolean isNull(int id, Object arr)
	{
		switch(this.type)
		{
			case ENTITY:
				return this.isNullMap(id, (Map) arr);
			case PROVIDER:
				return this.isNullMap(id, (Map) arr);
			case DIMENSION:
				return this.isNullMap(id, (Map) arr);
			default:	
				return ((T[])arr)[id] == null;
		}
	}

	public boolean isNullMap(int id, Map arr) 
	{
		return !arr.containsKey(id);
	}

	/**
	 * unregister the registration from the original id. doesn't work when there are conflicts
	 */
	public void unregOrg(int orgId)
	{
		if(this.isConflicted(orgId))
		{
			System.err.println("cannot unregister a conflicted domain:" + orgId + " for:" + this.type);
			return;
		}
		System.out.println("unregistering orgId:" + orgId + " type:" + this.type);
		this.registered.remove(orgId);
	}
	
	public boolean hasItemBlock(Set<RegEntry> entries) 
	{
		for(RegEntry e : entries)
			if(e.obj instanceof ItemBlock)
				return true;
		return false;
	}
	
	public static final ModContainer vmc = Loader.instance().getMinecraftModContainer();//vanilla mod container
	public ModContainer getMod()
	{
		ModContainer mc = Loader.instance().activeModContainer();
		return  mc != null && this.initMc ? mc : vmc;
	}
	
	/**
	 * sets the initMc to true used to determine when to used to determine whether or not the class this registry is for has been initialized yet
	 */
	public void initMc()
	{
		this.initMc = true;
	}
	
	public class RegEntry
	{
		public int newId;
		public int orgId;
		public Class<?> oClass;
		public Object obj;
		public String modid;
		public String modname;
		public boolean passable;
		public static final String MINECRAFT = "minecraft";
		
		public RegEntry(int id, Object obj)
		{
			this(id, id, obj);
		}
		
		public RegEntry(int newId, int orgId, Object obj)
		{
			this.newId = newId;
			this.orgId = orgId;
			this.oClass = RegUtils.getOClass(obj);
			this.obj = obj;
			ModContainer mc = Registry.this.getMod();
			this.modid = mc.getModId();
			this.modname = mc.getName();
		}
		
		@Override
		public int hashCode()
		{
			return this.newId;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if(!(obj instanceof RegEntry))
				return false;
			RegEntry o = (RegEntry)obj;
			return this.newId == o.newId && this.orgId == o.orgId && this.oClass.equals(o.oClass) && this.modid.equals(o.modid);
		}

		public String getName()
		{
			try
			{
				return RegUtils.getName(this.newId, Registry.this.type, this.obj);
			}
			catch(Throwable t)
			{
				System.err.println("error while getting name:" + Registry.this.type + " obj:" + this.obj);
				t.printStackTrace();
				return t.getClass().getName();
			}
		}
	}
}

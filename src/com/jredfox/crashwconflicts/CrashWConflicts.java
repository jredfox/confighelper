package com.jredfox.crashwconflicts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.jredfox.crashwconflicts.proxy.Proxy;
import com.jredfox.util.IdChunk;
import com.jredfox.util.RegTypes;
import com.jredfox.util.RegUtils;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.crash.CrashReport;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.DimensionManager;

@Mod(modid = "crash-w-conflicts", name = "Crash With Conflicts", version = "b43")
public class CrashWConflicts implements ITickHandler{
	
	public static boolean hasConflicts;
	public static Map<Integer, String> items = new HashMap();
	public static Map<Integer, Boolean> itemBlockFlags = new HashMap();
	public static Map<Integer, String> blocks = new HashMap();
	public static Map<Integer, String> biomes = new HashMap();
	public static Map<Integer, String> enchantments = new HashMap();
	public static Map<Integer, String> potions = new HashMap();
	public static Map<Integer, String> entities = new HashMap();
	public static Map<Integer, String> providers = new HashMap();
	public static Map<Integer, String> dimensions = new HashMap();
	public static Set<Integer> itemsOrg = new HashSet();
	public static Set<Integer> blocksOrg = new HashSet();
	public static Set<Integer> biomesOrg = new HashSet();
	public static Set<Integer> enchantmentsOrg = new HashSet();
	public static Set<Integer> potionsOrg = new HashSet();
	public static Set<Integer> entitiesOrg = new HashSet();
	public static Set<Integer> providersOrg = new HashSet();
	public static Set<Integer> dimensionsOrg = new HashSet();
	public static Set<Passable> passables = new HashSet();
	public static int entId = 127;
	public static boolean writeFreeIds;
	public static boolean autocfg;
	public static File cwcMain = new File("config/cwc").getAbsoluteFile();
	public static File cwcDir = new File(cwcMain, "dumpIds");
	@SidedProxy(clientSide="com.jredfox.crashwconflicts.proxy.ClientProxy", serverSide="com.jredfox.crashwconflicts.proxy.Proxy")
	public static Proxy proxy;
	
	static
	{
		initCfg();//called in mod's conctructor to prevent other conflicts from happening in pre-init before this mod is loaded. <clinit> can cause class initialization errors if the RegTypes's class isn't initialized yet
	}
	
	@PreInit
	public static void preInit(FMLPreInitializationEvent pi)
	{	
//		//DimensionTest
//		DimensionManager.registerDimension(0, 0);
//		DimensionManager.registerDimension(-1, -1);
//		DimensionManager.registerProviderType(-1, WorldProvider.class, true);
//		DimensionManager.registerProviderType(0, WorldProvider.class, true);
		
		//conflict test
//		new Item(RegUtils.getMax(RegTypes.ITEM)).setUnlocalizedName("item.tst");
//		new Block(1, Material.anvil).setUnlocalizedName("tile.tst");
//		new BiomeGenOcean(3);
//		new EnchantmentProtection(1, 5, 1);
//		new Potion(3, false, 400);
//		EntityRegistry.registerGlobalEntityID(D.class, "a", 14);
//		EntityList.addMapping(E.class, "a", 14);
//		new Item(69).setUnlocalizedName("item.tst");
//		new Block(1, Material.anvil).setUnlocalizedName("tile.tst");
//		new BiomeGenOcean(3);
//		new EnchantmentProtection(1, 5, 1);
//		new Potion(3, false, 400);
//		ItemBlock
		RegUtils.init();
		TickRegistry.registerTickHandler(new CrashWConflicts(), Side.CLIENT);
	}

	public static void initCfg()
	{
		Configuration cfg = new Configuration(new File(cwcMain, "cwc.cfg"));
		cfg.load();
		entId = cfg.get("global", "entityIdLimit", entId).getInt();
		writeFreeIds = cfg.get("global", "writeFreeIds", true).getBoolean(true);
		autocfg = cfg.get("global", "autoConfig", false).getBoolean(false);
		String[] arr = cfg.get("global", "passable", new String[0], "for dimensions use null as the class. Format=num:class:modid").getStringList();
		for(String s : arr)
		{
			try
			{
				String[] parts = s.split(":");
				String c = parts[1].trim();
				Passable p = new Passable(Integer.parseInt(parts[0]), c.equals("null") ? Passable.class.getName() : parts[1], parts[2]);
				System.out.println("adding passable:" + p);
				passables.add(p);
			}
			catch (Throwable t)
			{
				System.err.println("skipping passable:" + s);
				t.printStackTrace();
			}
		}
		if(cfg.hasChanged())
			cfg.save();
	}
	
	public static boolean isPassable(int id, Class<?> nc) 
	{
		boolean p = passables.contains(new Passable(id, nc.getName(), Loader.instance().activeModContainer().getModId()));
		if(p)
			System.out.println("skipping passable:" + id + " " + nc);
		return p;
	}

	public static <T> int getFreeId(Map<Integer, String> conflicts, T[] arr, int id, Class<?> nc, Class<?> oldreg) 
	{
		if(isPassable(conflicts == CrashWConflicts.items ? id - 256 : id, nc))
			return id;
    	CrashWConflicts.hasConflicts = true;
    	String newreg = nc.getName();
    	if(arr == Item.itemsList && (RegUtils.isClassExtending(ItemBlock.class, nc) || RegUtils.isClassExtending(ItemBlock.class, oldreg)))
    		itemBlockFlags.put(id, true);
		newreg = newreg + " modid:\"" + Loader.instance().activeModContainer().getModId() + "\"" +" modName:\"" + Loader.instance().activeModContainer().getName() + "\"";
		conflicts.put((Integer)id, conflicts.containsKey(id) ? conflicts.get(id) + ", " + newreg : oldreg.getName() + ", " + newreg);
    	for(int i = arr.length-1; i>=0 ;i--)
    	{
    		if(arr[i] == null)
    			return i;
    	}
    	throw new RuntimeException("out of free ids!");
	}

	public static int getFreeDimId(boolean provider, int id, Class<?> nc, String newreg, String oldreg)
	{
		if(isPassable(id, nc))
			return id;
		CrashWConflicts.hasConflicts = true;
		newreg = !provider ? "" : newreg + " modid:\"" + Loader.instance().activeModContainer().getModId() + "\"" +" modName:\"" + Loader.instance().activeModContainer().getName() + "\"";
		Map<Integer, String> conflicts = provider ? providers : dimensions;
		String reg = provider ? (conflicts.containsKey(id) ? conflicts.get(id) + ", " + newreg : oldreg + ", " + newreg) : "";
		conflicts.put(id, reg);
		for(int i=Short.MIN_VALUE;i<Short.MAX_VALUE;i++)
		{
			if(provider ? !DimensionManager.getProviders().contains(i) : !DimensionManager.getDimensions().contains(i))
				return i;
		}
		throw new RuntimeException("out of free ids!");
	}
	
	public static int getFreeEntId(Map<Integer, String> conflicts, Set<Integer> keySet, int id, Class<?> nc, String oldreg) 
	{
		if(isPassable(id, nc))
			return id;
		String newreg = nc.getName();
    	CrashWConflicts.hasConflicts = true;
		newreg = newreg + " modid:\"" + Loader.instance().activeModContainer().getModId() + "\"" +" modName:\"" + Loader.instance().activeModContainer().getName() + "\"";
    	conflicts.put((Integer)id, conflicts.containsKey(id) ? conflicts.get(id) + ", " + newreg : oldreg + ", " + newreg);
    	for(int i = RegUtils.getMax(RegTypes.ENTITY) ; i>=0; i--)
    	{
    		if(!keySet.contains(i))
    			return i;
    	}
    	throw new RuntimeException("out of free ids!");
	}

	/**
	 * dump conflicts and free ids then crash the game
	 */
	public static void dumpIds()
	{
		try 
		{
			long ms = System.currentTimeMillis();
			cwcDir.mkdirs();
			if(writeFreeIds)
				writeFreeIds();
			writeConflicts(new RegTypes[]{
					RegTypes.ITEM, RegTypes.BLOCK, RegTypes.BIOME, RegTypes.ENCHANTMENT, RegTypes.POTION, RegTypes.ENTITY, RegTypes.PROVIDER, RegTypes.DIMENSION},
					items, blocks, biomes, enchantments, potions, entities, providers, dimensions);
			System.out.println("done dumpingIds:" + (System.currentTimeMillis() - ms) + "ms");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		if(hasConflicts)
		{
			CrashReport c = CrashReport.makeCrashReport(new RuntimeException("id conflict"), "minecraft cannot continue with id conflicts shutting down! reconfigure your modpack ;)");
			proxy.displayCrash(c);
		}
		else
			System.out.println("\n\n----> conflict free UwU\n");
	}
	
	public static void writeFreeIds() throws IOException
	{
		for(RegTypes type : RegTypes.values())
		{
			int min = RegUtils.getMin(type, true);
			int max = RegUtils.getMax(type, true);
			Set<Integer> org = RegUtils.getOrgIds(type);
			Set<IdChunk> chunky = IdChunk.fromAround(min, max, org);
			BufferedWriter fw = RegUtils.getWriter(new File(cwcDir, "freeids-" + type.name().substring(0, 1) + type.name().toLowerCase().substring(1) + ".txt"));
			int count = 0;
			for(IdChunk chunk : chunky)
			{
				fw.write(RegUtils.unshiftIdChunk(type, chunk) + System.lineSeparator());
				if(count++ % 300 == 0)
					fw.flush();
			}
			fw.close();
		}
	}
	
	public static void writeConflicts(RegTypes[] types, Map<Integer, String>... lists) throws IOException
	{
		int index = 0;
		for(Map<Integer, String> arr : lists)
		{
			RegTypes type = types[index];
			BufferedWriter fw = RegUtils.getWriter(new File(cwcDir, "conflicts-" + type.name().substring(0, 1) + type.name().toLowerCase().substring(1) + ".txt"));
			int j = 0;
			for(Integer id : arr.keySet())
			{
				String cfid = arr.get(id);
				fw.write("" + (index == 0 ? (id - RegUtils.ITEM_SHIFT + (itemBlockFlags.containsKey(id) ? " blockId:" + id : "")) : id) + (cfid.trim().isEmpty() ? "" : " = " + cfid) + System.lineSeparator());//ids for some reason are +256
				if(j++ % 100 == 0)
					fw.flush();
			}
			fw.close();
			index++;
		}
	}

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData)
	{
		
	}

	public boolean firstTick = true;
	public static boolean isCrashing;
	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) 
	{
		if(hasConflicts || firstTick)
			postLoad();
	}

	public void postLoad()
	{
		if(autocfg)
			new AutoConfig().load().run();
		dumpIds();
		firstTick = false;
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.CLIENT);
	}

	@Override
	public String getLabel() 
	{
		return this.getClass().getSimpleName();
	}
}

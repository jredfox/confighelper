package com.jredfox.crashwconflicts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.jredfox.crashwconflicts.proxy.Proxy;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.block.Block;
import net.minecraft.crash.CrashReport;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.util.ReportedException;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.DimensionManager;

@Mod(modid = "crash-w-conflicts", name = "Crash With Conflicts", version = "b30")
public class CrashWConflicts implements ITickHandler{
	
	public static boolean hasConflicts;
	public static Map<Integer, String> items = new HashMap();
	public static Map<Integer, String> blocks = new HashMap();
	public static Map<Integer, String> biomes = new HashMap();
	public static Map<Integer, String> enchantments = new HashMap();
	public static Map<Integer, String> potions = new HashMap();
	public static Map<Integer, String> entities = new HashMap();
	public static Map<Integer, String> providers = new HashMap();
	public static Map<Integer, String> dimensions = new HashMap();
	public static Set<Passable> passables = new HashSet();
	public static String[] types = {"items", "blocks", "biomes", "enchantments", "potions", "entities", "providers", "dimensions"};//TE's are auto done and DataWatchers force crash if they do in fact conflict
	public static int entId = 127;
	public static boolean writeFreeIds;
	public static boolean autocfg;
	public static File cwcMain = new File("config/cwc").getAbsoluteFile();
	public static File cwcDir = new File(cwcMain, "dumpIds");
	@SidedProxy(clientSide="com.jredfox.crashwconflicts.proxy.ClientProxy", serverSide="com.jredfox.crashwconflicts.proxy.Proxy")
	public static Proxy proxy;
	
	static
	{
		initCfg();//called in <clinit> to prevent other conflicts from happening in pre-init before this mod is loaded
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
//		new Item(69).setUnlocalizedName("item.tst");
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
		TickRegistry.registerTickHandler(new CrashWConflicts(), Side.CLIENT);
	}

	public static void initCfg()
	{
		Configuration cfg = new Configuration(new File(cwcMain, "cwc.cfg"));
		cfg.load();
		entId = cfg.get("global", "entityIdLimit", entId).getInt();
		writeFreeIds = cfg.get("global", "writeFreeIds", true).getBoolean(true);
//		autocfg = cfg.get("global", "auto-config", false).getBoolean(false);
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

	public static <T> int getFreeId(Map<Integer, String> conflicts, T[] arr, int id, Class<?> nc, String oldreg) 
	{
		if(isPassable(conflicts == CrashWConflicts.items ? id - 256 : id, nc))
			return id;
    	CrashWConflicts.hasConflicts = true;
    	String newreg = nc.getName();
		newreg = newreg + " modid:\"" + Loader.instance().activeModContainer().getModId() + "\"" +" modName:\"" + Loader.instance().activeModContainer().getName() + "\"";
		conflicts.put((Integer)id, conflicts.containsKey(id) ? conflicts.get(id) + ", " + newreg : oldreg + ", " + newreg);
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
		for(int i=-4096;i<4097;i++)
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
    	for(int i = entId ; i>=0; i--)
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
			writeConflicts(items, blocks, biomes, enchantments, potions, entities, providers, dimensions);
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
			throw new ReportedException(c);
		}
		else
			System.out.println("\n\n----> conflict free UwU\n");
	}
	
	public static void writeFreeIds() throws IOException
	{
		int index = 0;
		for(String type : types)
		{
			Object[] arr = getArr(index);
			BufferedWriter fw = getWriter(new File(cwcDir, "freeids-" + type + ".txt"));
			if(arr != null)
			{
				int j = 0;
				for(Object o : arr)
				{
					int num = index == 0 ? j - 256 : j;
					if(num <= 0)
					{
						j++;
						continue;//0 seems to be a reserved id everywhere so don't use it as a free id
					}
					if(o == null)
					{
						fw.write("" + num + " ");
						if(j % 400 == 0)
							fw.flush();
					}
					j++;
				}
			}
			else
			{
				boolean isDim = index == types.length - 1;
				for(int j = -4096; j < 4097; j++)
				{
					if(isDim ? !DimensionManager.getDimensions().keySet().contains(j) : !DimensionManager.getProviders().keySet().contains(j))
					{	
						fw.write("" + j + " ");
						if(j % 400 == 0)
							fw.flush();
					}
				}
			}
			fw.close();
			index++;
		}
	}
	
	private static Object[] getArr(int i)
	{
		switch (i)
		{
			case 0:
				return Item.itemsList;
			case 1:
				return Block.blocksList;
			case 2:
				return BiomeGenBase.biomeList;
			case 3:
				return Enchantment.enchantmentsList;
			case 4:
				return Potion.potionTypes;
			case 5:
				return getArr(EntityList.IDtoClassMapping.keySet(), entId + 1);
			default:
				return null;
		}
	}

	public static Object[] getArr(Collection<Integer> col, int size) 
	{
		Integer[] arr = new Integer[size];
		for(Integer i : col)
			if(i < arr.length)
				arr[i] = (Integer)i;//take the index of the id and turn it into a non null object aka itself as a non free id
		return arr;
	}
	
	public static void writeConflicts(Map<Integer, String>... lists) throws IOException
	{
		int index = 0;
		for(Map<Integer, String> arr : lists)
		{
			String type = types[index];
			BufferedWriter fw = getWriter(new File(cwcDir, "conflicts-" + type + ".txt"));
			int j = 0;
			for(Integer id : arr.keySet())
			{
				String cfid = arr.get(id);
				fw.write("" + (index == 0 ? (id - 256) : id) + (cfid.trim().isEmpty() ? "" : " = " + cfid) + System.lineSeparator());//ids for some reason are +256
				if(j++ % 100 == 0)
					fw.flush();
			}
			fw.close();
			index++;
		}
	}

	private static BufferedWriter getWriter(File f) throws FileNotFoundException
	{
		return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8));
	}

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData)
	{
		
	}

	public boolean firstTick = true;
	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) 
	{
		if(hasConflicts || firstTick)
		{
			firstTick = false;
			if(autocfg)
				AutoConfig.autocfg();
			dumpIds();
		}
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

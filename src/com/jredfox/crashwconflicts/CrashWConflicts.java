package com.jredfox.crashwconflicts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
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

@Mod(modid = "crash-w-conflicts", name = "Crash With Conflicts", version = "1.0.0")
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
	public static String[] types = {"items", "blocks", "biomes", "enchantments", "potions", "entities", "providers", "dimensions"};//TE's are auto done and DataWatchers force crash if they do in fact conflict
	
	@PreInit
	public static void preInit(FMLPreInitializationEvent pi)
	{
		//DimensionTest
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
//		EntityList.addMapping(E.class, "a", 14);
//		EntityRegistry.registerGlobalEntityID(D.class, "a", 14);
//		new Item(69).setUnlocalizedName("item.tst");
//		new Block(1, Material.anvil).setUnlocalizedName("tile.tst");
//		new BiomeGenOcean(3);
//		new EnchantmentProtection(1, 5, 1);
//		new Potion(3, false, 400);
		TickRegistry.registerTickHandler(new CrashWConflicts(), Side.CLIENT);
	}

	@PostInit
	public static void postInit(FMLPostInitializationEvent pi)
	{
		dumpIds();
//		for(int i=0;i<Item.itemsList.length;i++)
//		{
//			if(Item.itemsList[i] != null)
//				System.out.println((i - 256) + " name:" + Item.itemsList[i].getUnlocalizedName());
//		}
	}
	
	public static <T> int getFreeId(Map<Integer, String> conflicts, T[] arr, int id, String newreg, String oldreg) 
	{
    	CrashWConflicts.hasConflicts = true;
    	conflicts.put((Integer)id, conflicts.containsKey(id) ? conflicts.get(id) + ", " + newreg : oldreg + ", " + newreg);
    	for(int i = arr.length-1; i>=0 ;i--)
    	{
    		if(arr[i] == null)
    			return i;
    	}
    	throw new RuntimeException("out of free ids!");
	}
	
	public static int getFreeDimId(int id, boolean provider, String newreg, String oldreg)
	{
		CrashWConflicts.hasConflicts = true;
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
	
	public static int getFreeEntId(Map<Integer, String> conflicts, Set<Integer> keySet, int id, String newreg, String oldreg) 
	{
    	CrashWConflicts.hasConflicts = true;
    	conflicts.put((Integer)id, conflicts.containsKey(id) ? conflicts.get(id) + ", " + newreg : oldreg + ", " + newreg);
    	for(int i = 255; i>=0; i--)
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
			writeFreeIds();
			writeConflicts(items, blocks, biomes, enchantments, potions, entities, providers, dimensions);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		if(hasConflicts)
		{
			CrashReport c = CrashReport.makeCrashReport(new RuntimeException("id conflict"), "minecraft cannot continue with id conflicts shutting down! reconfigure your modpack ;)");
			throw new ReportedException(c);
		}
		else
			System.out.println("\n\n----> conflict free UwU\n");
	}
	
	public static void writeFreeIds() throws IOException
	{
		File dir = new File(new File("").getAbsoluteFile(), "crashwconflicts");
		dir.mkdirs();
		int index = 0;
		for(String type : types)
		{
			Object[] arr = getArr(index);
			BufferedWriter fw = getWriter(new File(dir, "freeids-" + type + ".txt"));
			
			//hardcoded dim s***
			if(arr == null && index >= types.length - 2)
			{
				boolean isDim = index == types.length - 1;
				for(int j = -4096; j < 4097; j++)
				{
					if(isDim && !DimensionManager.getDimensions().keySet().contains(j) || !isDim && !DimensionManager.getProviders().keySet().contains(j))
					{	
						fw.write("" + j + " ");
						fw.flush();
					}
				}
				continue;
			}
			
			if(arr == null)
				continue;
			
			int j = 0;
			for(Object o : arr)
			{
				int num = index == 0 ? j - 256 : j;
				if(o == null)
					fw.write("" + num + " ");
				j++;
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
				return getArr(EntityList.IDtoClassMapping.keySet(), 256);
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
		File dir = new File(new File("").getAbsoluteFile(), "crashwconflicts");
		dir.mkdirs();
		int index = 0;
		for(Map<Integer, String> arr : lists)
		{
			String type = types[index];
			BufferedWriter fw = getWriter(new File(dir, "conflicts-" + type + ".txt"));
			for(Integer id : arr.keySet())
			{
				String cfid = arr.get(id);
				fw.write("" + (index == 0 ? (id - 256) : id) + (cfid.trim().isEmpty() ? "" : " = " + cfid) + System.lineSeparator());//ids for some reason are +256
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
		if(hasConflicts)
		{
			dumpIds();
		}
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {}

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

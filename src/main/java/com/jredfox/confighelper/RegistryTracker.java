package com.jredfox.confighelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityList;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.DimensionManager;

public class RegistryTracker {
	
	static
	{
		RegistryConfig.load();
	}
	
	public static boolean hasConflicts;
	public static boolean startup = true;
	
	public static Registry biomes = new Registry(!RegistryConfig.autoConfig,"biome");
	public static Registry enchantments = new Registry(!RegistryConfig.autoConfig,"enchantment");
	public static Registry potions = new Registry(!RegistryConfig.autoConfig,"potion");
	public static Registry dimensions = new Registry(!RegistryConfig.autoConfig,"dimension");
	public static Registry providers = new Registry(!RegistryConfig.autoConfig,"provider");
	public static Registry entities = new Registry(!RegistryConfig.autoConfig,"entity");
	
	public static Registry dataWatcherPlayers = new Registry(!RegistryConfig.autoConfig,"dataWatcherPlayers");
	
	public static int registerBiome(BiomeGenBase biome, int id)
	{
		return register(biome, id, biomes);
	}
	
	public static int registerProvider(Class providerObj, int providerId)
	{
		return register(providerObj, providerId, providers);
	}
	
	public static int registerDimension(List<Class> providerObj, int dimId)
	{
		return register(providerObj, dimId, dimensions);
	}
	
	public static int registerPotion(Potion p, int id)
	{
		return register(p, id, potions);
	}
	
	public static int registerEnchantment(Enchantment ench, int id)
	{
		return register(ench, id, enchantments);
	}
	
	public static int registerEntity(Class entity, int id) 
	{
		return register(entity, id, entities);
	}
	
	public static int register(Object obj, int id, Registry reg)
	{
		return reg.reg(obj, id);
	}
	
	/**
	 * a live look at the arrays
	 * @param dir
	 */
	public static final File root = new File("./config/confighelper");
	public static final File dirBiomes = new File(root, "biomes");
	public static final File dirDimensions = new File(root, "dimensions");
	public static final File dirPotions = new File(root, "potions");
	public static final File dirEnchantments = new File(root, "enchantments");
	public static final File dirEntities = new File(root, "entities");
	
	public static void outputConfigIds()
	{
		try
		{
		mkdirs();
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dirBiomes, "suggested.txt")));
		writeBiomes(writer);
		writer.close();
		writer = new BufferedWriter(new FileWriter(new File(dirDimensions, "suggested.txt")));
		writeProviders(writer);
		writer.close();
		writer = new BufferedWriter(new FileWriter(new File(dirPotions, "suggested.txt")));
		writePotions(writer);
		writer.close();
		writer = new BufferedWriter(new FileWriter(new File(dirEnchantments, "suggested.txt")));
		writeEnchantments(writer);
		writer.close();
		writer = new BufferedWriter(new FileWriter(new File(dirEntities, "suggested.txt")));
		writeEntities(writer);
		writer.close();
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
	}
	
	public static void mkdirs() 
	{
		if(!root.exists())
			root.mkdirs();
		if(!dirBiomes.exists())
			dirBiomes.mkdir();
		if(!dirDimensions.exists())
			dirDimensions.mkdir();
		if(!dirPotions.exists())
			dirPotions.mkdir();
		if(!dirEnchantments.exists())
			dirEnchantments.mkdir();
		if(!dirEntities.exists())
			dirEntities.mkdir();
	}

	private static void writeEntities(BufferedWriter writer) throws IOException 
	{
		Map<Integer,Class> map = EntityList.IDtoClassMapping;
		for(Map.Entry<Integer, Class> entry : map.entrySet())
		{
			writer.write("id:" + entry.getKey() + " = " + entry.getValue() + "\r\n");
		}
	}

	private static void writePotions(BufferedWriter writer) throws IOException
	{
		writeStaticRegistry(writer, Potion.potionTypes);
	}
	
	private static void writeBiomes(BufferedWriter writer) throws IOException
	{
		writeStaticRegistry(writer, BiomeGenBase.biomeList);
	}
	
	private static void writeEnchantments(BufferedWriter writer) throws IOException
	{
		writeStaticRegistry(writer, Enchantment.enchantmentsList);
	}

	private static void writeStaticRegistry(BufferedWriter writer, Object[] arr) throws IOException
	{
		for(int i=0;i<arr.length;i++)
		{
			Object p = arr[i];
			if(p != null)
				writer.write("id:" + i + " = " + p.getClass() + "\r\n");
		}
	}
	
	private static void writeProviders(BufferedWriter writer) throws IOException
	{
		TreeSet<Integer> map = new TreeSet(DimensionManager.providers.keySet());
		for(Integer id : map)
		{
			writer.write("id:" + id + " = " + DimensionManager.providers.get(id).getName() + "\r\n");
		}
	}

	public static void outputConflictedIds()
	{
		try{
		mkdirs();
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dirBiomes,"conflicts.txt")));
		String spacer = "___________________________________";
		writer.write("biomes:\r\n" + spacer + "\r\n");
		writeRegistry(writer, biomes);
		writer.close();
		
		writer = new BufferedWriter(new FileWriter(new File(dirDimensions,"conflicts.txt")));
		writer.write("providers:\r\n" + spacer + "\r\n");
		writeRegistry(writer, providers);
		writer.write(spacer + "\r\n");
		writer.write("dimensions:\r\n" + spacer + "\r\n");
		writeRegistry(writer, dimensions);
		writer.close();
		
		writer = new BufferedWriter(new FileWriter(new File(dirEntities,"conflicts.txt")));
		writer.write("entities:\r\n" + spacer + "\r\n");
		writeRegistry(writer, entities);
		writer.close();
		
		writer = new BufferedWriter(new FileWriter(new File(dirPotions,"conflicts.txt")));
		writer.write("potions:\r\n" + spacer + "\r\n");
		writeRegistry(writer, potions);
		writer.close();
		
		writer = new BufferedWriter(new FileWriter(new File(dirEnchantments,"conflicts.txt")));
		writer.write("enchantments:\r\n" + spacer + "\r\n");
		writeRegistry(writer, enchantments);
		writer.close();
		
//		writer = new BufferedWriter(new FileWriter(new File(dirDataWatchers,"conflicts.txt")));
//		writer.write("dataWatcherPlayers:\r\n" + spacer + "\r\n");
//		writeRegistry(writer, dataWatcherPlayers);
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
	}
	
	public static void outputFreeIds()
	{
		try
		{
			mkdirs();
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dirBiomes, "freeids.txt")));
			writeFreeBiomeIds(writer);
			writer.close();
			
			writer = new BufferedWriter(new FileWriter(new File(dirPotions, "freeids.txt")));
			writeFreePotionIds(writer);
			writer.close();
			
			writer = new BufferedWriter(new FileWriter(new File(dirEnchantments, "freeids.txt")));
			writeFreeEnchantmentIds(writer);
			writer.close();
			
			writer = new BufferedWriter(new FileWriter(new File(dirDimensions, "freeids.txt")));
			writeFreeDimIds(writer);
			writer.close();
			
			writer = new BufferedWriter(new FileWriter(new File(dirEntities, "freeids.txt")));
			writeFreeEntityIds(writer);
			writer.close();
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
	}
	
	private static void writeFreeEntityIds(BufferedWriter writer) throws IOException 
	{
		for(int i=0;i<=RegistryConfig.entitiesLimit;i++)
		{
			if(entities.reg.get(i) == null)
				writer.write("id:" + i + "\r\n");
		}
	}

	private static void writeFreeDimIds(BufferedWriter writer) throws IOException 
	{
		for(int i=0;i<=RegistryConfig.searchDim;i++)
		{
			if(dimensions.reg.get(i) == null && providers.reg.get(i) == null)
				writer.write("id:" + i + "\r\n");
		}
	}

	private static void writeFreePotionIds(BufferedWriter writer) throws IOException 
	{
		writeFreeIds(writer, Potion.potionTypes, potions);
	}
	
	private static void writeFreeEnchantmentIds(BufferedWriter writer) throws IOException 
	{
		writeFreeIds(writer, Enchantment.enchantmentsList, enchantments);
	}
	
	private static void writeFreeBiomeIds(BufferedWriter writer) throws IOException 
	{
		writeFreeIds(writer, BiomeGenBase.biomeList, biomes);
	}

	private static void writeFreeIds(BufferedWriter writer, Object[] arr, Registry reg) throws IOException 
	{
		for(int i=0;i<arr.length;i++)
		{
			if(reg.reg.get(i) == null)
				writer.write("id:" + i + "\r\n");
		}
	}
	
	public static void writeRegistry(BufferedWriter writer, Registry reg) throws IOException
	{
		for(Entry<Integer, List<Class>> map : reg.reg.entrySet())
		{
			int id = map.getKey();
			List<Class> li = map.getValue();
			if(li.size() > 1)
			{
				writer.write(id +  "=" + getListClass(li) + "\r\n");
				hasConflicts = true;
			}
		}
	}

	private static String getListClass(List<Class> li) 
	{
		StringBuilder str = new StringBuilder();
		str.append("[");
		for(Class c : li)
			str.append(c.getName() + ",");
		String built = str.toString();
		return built.toString().substring(0, built.length()-1) + "]";
	}

	public static void output() 
	{
		RegistryTracker.outputConfigIds();
		RegistryTracker.outputConflictedIds();
		RegistryTracker.outputFreeIds();
	}

}

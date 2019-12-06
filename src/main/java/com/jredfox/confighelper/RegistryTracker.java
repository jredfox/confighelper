package com.jredfox.confighelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import com.jredfox.confighelper.Registry.DataType;

import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityList;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.DimensionManager;

public class RegistryTracker {
	
	public static boolean hasConflicts;
	public static boolean startup = true;
	
	public static Registry biomes = new Registry(!RegistryConfig.autoConfig, DataType.BIOME);
	public static Registry enchantments = new Registry(!RegistryConfig.autoConfig, DataType.ENCHANTMENT);
	public static Registry potions = new Registry(!RegistryConfig.autoConfig, DataType.POTION);
	public static Registry dimensions = new RegistryDim(!RegistryConfig.autoConfig, DataType.DIMENSION);
	public static Registry providers = new RegistryDim(!RegistryConfig.autoConfig, DataType.PROVIDER);
	public static Registry entities = new Registry(!RegistryConfig.autoConfig, DataType.ENTITY);
	//TODO:
	public static Registry dataWatcherPlayers = new Registry(!RegistryConfig.autoConfig, DataType.DATAWATCHERPLAYER);
	
	public static int registerBiome(BiomeGenBase biome, int id)
	{
		return register(biome, id, biomes);
	}
	
	public static int registerProvider(Class providerObj, int providerId)
	{
		return register(providerObj, providerId, providers);
	}
	
	public static int registerDimension(int dimId)
	{
		return register(Integer.class, dimId, dimensions);
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
	
	public static void output() 
	{
		RegistryTracker.outputConfigIds();
		RegistryTracker.outputConflictedIds();
		RegistryTracker.outputFreeIds();
	}
	
	public static void outputConfigIds()
	{
		try
		{
		mkdirs();
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dirBiomes, "suggested.txt")));
		writeBiomes(writer);
		writer.close();
		writer = new BufferedWriter(new FileWriter(new File(dirPotions, "suggested.txt")));
		writePotions(writer);
		writer.close();
		writer = new BufferedWriter(new FileWriter(new File(dirEnchantments, "suggested.txt")));
		writeEnchantments(writer);
		writer.close();
		writer = new BufferedWriter(new FileWriter(new File(dirDimensions, "suggested.txt")));
		writeProviders(writer);
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
	
	private static void writeBiomes(BufferedWriter writer) throws IOException
	{
		writeStaticRegistry(writer, BiomeGenBase.biomeList, biomes);
	}
	
	private static void writePotions(BufferedWriter writer) throws IOException
	{
		writeStaticRegistry(writer, Potion.potionTypes, potions);
	}
	
	private static void writeEnchantments(BufferedWriter writer) throws IOException
	{
		writeStaticRegistry(writer, Enchantment.enchantmentsList, enchantments);
	}
	
	private static void writeStaticRegistry(BufferedWriter writer, Object[] arr, Registry reg) throws IOException
	{
		for(int i=0;i<arr.length;i++)
		{
			Object obj = arr[i];
			if(obj != null)
			{
				writer.write("id:" + i + " = " + reg.getEntry(reg.getOriginalId(i)) + "\r\n");
			}
		}
	}
	
	private static void writeProviders(BufferedWriter writer) throws IOException
	{
		TreeSet<Integer> map = new TreeSet(DimensionManager.providers.keySet());
		for(Integer id : map)
		{
			writer.write("id:" + id + " = " + providers.getEntry(providers.getOriginalId(id)) + "\r\n");
		}
	}

	private static void writeEntities(BufferedWriter writer) throws IOException 
	{
		Map<Integer,Class> map = EntityList.IDtoClassMapping;
		for(Map.Entry<Integer, Class> entry : map.entrySet())
		{
			int id = entry.getKey();
			writer.write("id:" + id + " = " + entities.getEntry(entities.getOriginalId(id)) + "\r\n");
		}
	}

	public static void outputConflictedIds()
	{
		try{
		mkdirs();
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dirBiomes,"conflicts.txt")));
		String spacer = "___________________________________";
		writer.write("biomes:\r\n" + spacer + "\r\n");
		writeConflicts(writer, biomes);
		writer.close();
		
		writer = new BufferedWriter(new FileWriter(new File(dirDimensions,"conflicts.txt")));
		writer.write("providers:\r\n" + spacer + "\r\n");
		writeConflicts(writer, providers);
		writer.write(spacer + "\r\n");
		writer.write("dimensions:\r\n" + spacer + "\r\n");
		writeConflicts(writer, dimensions);
		writer.close();
		
		writer = new BufferedWriter(new FileWriter(new File(dirEntities,"conflicts.txt")));
		writer.write("entities:\r\n" + spacer + "\r\n");
		writeConflicts(writer, entities);
		writer.close();
		
		writer = new BufferedWriter(new FileWriter(new File(dirPotions,"conflicts.txt")));
		writer.write("potions:\r\n" + spacer + "\r\n");
		writeConflicts(writer, potions);
		writer.close();
		
		writer = new BufferedWriter(new FileWriter(new File(dirEnchantments,"conflicts.txt")));
		writer.write("enchantments:\r\n" + spacer + "\r\n");
		writeConflicts(writer, enchantments);
		writer.close();
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
	}
	
	public static void writeConflicts(BufferedWriter writer, Registry reg) throws IOException
	{
		for(Entry<Integer, List<Registry.Entry>> map : reg.reg.entrySet())
		{
			int id = map.getKey();
			if(reg.isConflicting(id))
			{
				List<Registry.Entry> list = map.getValue();
				writer.write(id +  "=" + list + "\r\n");
			}
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
		for(int i=0;i<=RegistryConfig.searchEntities;i++)
		{
			if(entities.getEntry(i) == null)
				writer.write("id:" + i + "\r\n");
		}
	}

	private static void writeFreeDimIds(BufferedWriter writer) throws IOException 
	{
		for(int i=RegistryConfig.searchDimLower;i<=RegistryConfig.searchDimUper;i++)
		{
			if(dimensions.getEntry(i) == null && providers.getEntry(i) == null)
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
			if(reg.getEntry(i) == null)
				writer.write("id:" + i + "\r\n");
		}
	}

}

package com.jredfox.confighelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import net.minecraft.world.WorldProvider;
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
	/**
	 * the last EntityPlayer data watcher object list
	 */
	public static Registry datawatchers;
	
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
	
	public static int registerDataWatcher(Class entityClass, int id, Registry reg)
	{
		return register(entityClass, id, reg);
	}
	
	public static int register(Object obj, int id, Registry reg)
	{
		return reg.reg(obj, id);
	}
	
	/**
	 * a live look at the arrays
	 */
	public static final File root = new File("./config/confighelper");
	public static final File dirBiomes = new File(root, "biomes");
	public static final File dirDimensions = new File(root, "dimensions");
	public static final File dirPotions = new File(root, "potions");
	public static final File dirEnchantments = new File(root, "enchantments");
	public static final File dirEntities = new File(root, "entities");
	public static final File dirDatawatchers = new File(root,"datawatchers");
	
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
		if(!dirDatawatchers.exists())
			dirDatawatchers.mkdir();
	}
	
	public static void output() 
	{
		RegistryTracker.grabNames();
		RegistryTracker.outputConfigIds();
		RegistryTracker.outputConflictedIds();
		RegistryTracker.outputFreeIds();
	}
	
	private static void grabNames()
	{
		addNames(biomes, BiomeGenBase.biomeList);
		addNames(enchantments, Enchantment.enchantmentsList);
		addNames(potions, Potion.potionTypes);
		
		for(List<Registry.Entry> li : providers.reg.values())
		{
			for(Registry.Entry entry : li)
			{
				entry.setName(getNameProvider(entry.newId));
			}
		}
		
		for(List<Registry.Entry> li : dimensions.reg.values())
		{
			for(Registry.Entry e : li)
			{
				e.setName(RegistryDim.isVanillaId(e.newId) ? "vanilla" : "modded");
			}
		}
		
		for(List<Registry.Entry> li : entities.reg.values())
		{
			for(Registry.Entry entry : li)
			{
				entry.setName(EntityList.getStringFromID(entry.newId));
			}
		}
		
		if(datawatchers != null)
		for(List<Registry.Entry> li : datawatchers.reg.values())
		{
			for(Registry.Entry e : li)
			{
				e.setName(RegistryDataWatcher.isVanillaId(e.newId) ? "vanilla" : "modded");
			}
		}
	}

	public static String getNameProvider(int newId) 
	{
		try
		{
			WorldProvider provider = DimensionManager.providers.get(newId).newInstance();
			return provider.getDimensionName();
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
		return null;
	}

	private static void addNames(Registry reg, Object[] arr) 
	{
		for(List<Registry.Entry> li : reg.reg.values())
		{
			for(Registry.Entry entry : li)
			{
				entry.setName(getName(arr[entry.newId]) );
			}
		}
	}
	
	private static String getName(Object obj)
	{
		if(obj instanceof BiomeGenBase)
			return ((BiomeGenBase)obj).biomeName;
		else if(obj instanceof Enchantment)
			return ((Enchantment)obj).getName();
		else if(obj instanceof Potion)
			return ((Potion)obj).getName();
		return null;
	}

	public static void outputConfigIds()
	{
		try
		{
		mkdirs();
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dirBiomes, "suggested.txt")));
		writeSuggested(writer, biomes);
		writer.close();
		writer = new BufferedWriter(new FileWriter(new File(dirPotions, "suggested.txt")));
		writeSuggested(writer, potions);
		writer.close();
		writer = new BufferedWriter(new FileWriter(new File(dirEnchantments, "suggested.txt")));
		writeSuggested(writer, enchantments);
		writer.close();
		writer = new BufferedWriter(new FileWriter(new File(dirDimensions, "suggested.txt")));
		writeSuggested(writer, providers);
		writer.close();
		writer = new BufferedWriter(new FileWriter(new File(dirEntities, "suggested.txt")));
		writeSuggested(writer, entities);
		writer.close();
		
		if(datawatchers != null)
		{
			writer = new BufferedWriter(new FileWriter(new File(dirDatawatchers, "suggested.txt")));
			writeDatawatchers(writer);
			writer.close();
		}
		
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
	}
	
	private static void writeSuggested(BufferedWriter writer, Registry reg) throws IOException
	{
		List<Registry.Entry> entries = new ArrayList();
		for(List<Registry.Entry> list : reg.reg.values())
		{
			entries.addAll(list);
		}
		Collections.sort(entries, new Comparator()
		{
			@Override
			public int compare(Object arg0, Object arg1) 
			{
				Integer i1 = ((Registry.Entry)arg0).newId;
				Integer i2 = ((Registry.Entry)arg1).newId;
				return i1.compareTo(i2);
			}
		});
		
		for(Registry.Entry e : entries)
		{
			if(e.dupe)
				continue;
			writer.write(e.newId + " " + e.getDisplay() + "\r\n");
		}
	}
	
	private static void writeDatawatchers(BufferedWriter writer) throws IOException
	{
		List<Registry.Entry> entries = new ArrayList();
		for(List<Registry.Entry> list : datawatchers.reg.values())
		{
			entries.addAll(list);
		}
		Collections.sort(entries, new Comparator()
		{
			@Override
			public int compare(Object arg0, Object arg1) 
			{
				Integer i1 = ((Registry.Entry)arg0).newId;
				Integer i2 = ((Registry.Entry)arg1).newId;
				return i1.compareTo(i2);
			}
		});
		for(Registry.Entry e : entries)
		{
			if(e.dupe)
				continue;
			writer.write(e.newId + " (" + e.name + ")\r\n");
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
		
		if(datawatchers != null)
		{
			writer = new BufferedWriter(new FileWriter(new File(dirDatawatchers,"conflicts.txt")));
			writer.write("datawatchers:\r\n" + spacer + "\r\n");
			writeConflicts(writer, datawatchers);
			writer.close();
		}
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
				writer.write(id +  " = " + reg.getDisplay(id) + "\r\n");
			}
		}
	}
	
	public static void outputFreeIds()
	{
		try
		{
			mkdirs();
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dirBiomes, "freeids.txt")));
			writeFreeIds(writer, RegistryConfig.biomeLimit, biomes);
			writer.close();
			
			writer = new BufferedWriter(new FileWriter(new File(dirPotions, "freeids.txt")));
			writeFreeIds(writer, RegistryConfig.potionsLimit, potions);
			writer.close();
			
			writer = new BufferedWriter(new FileWriter(new File(dirEnchantments, "freeids.txt")));
			writeFreeIds(writer, RegistryConfig.enchantmentsLimit, enchantments);
			writer.close();
			
			writer = new BufferedWriter(new FileWriter(new File(dirDimensions, "freeids.txt")));
			writeFreeDimIds(writer);
			writer.close();
			
			writer = new BufferedWriter(new FileWriter(new File(dirEntities, "freeids.txt")));
			writeFreeIds(writer, RegistryConfig.entities, entities);
			writer.close();
			
			if(datawatchers != null)
			{
				writer = new BufferedWriter(new FileWriter(new File(dirDatawatchers, "freeids.txt")));
				writeFreeIds(writer, RegistryConfig.dataWatchersLimit, datawatchers);
				writer.close();
			}
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
	}

	private static void writeFreeIds(BufferedWriter writer, int limit, Registry reg) throws IOException
	{
		for(int i=0;i<=limit;i++)
		{
			if(reg.getEntry(i) == null)
				writer.write(i + "\r\n");
		}
	}
	
	private static void writeFreeDimIds(BufferedWriter writer) throws IOException 
	{
		for(int i=RegistryConfig.searchDimLower;i<=RegistryConfig.searchDimUper;i++)
		{
			if(dimensions.getEntry(i) == null && providers.getEntry(i) == null)
				writer.write(i + "\r\n");
		}
	}

}

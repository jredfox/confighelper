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

import org.ralleytn.simple.json.JSONArray;
import org.ralleytn.simple.json.JSONObject;
import org.ralleytn.simple.json.JSONParseException;

import com.evilnotch.lib.util.JavaUtil;
import com.jredfox.confighelper.Registry.DataType;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityList;
import net.minecraft.potion.Potion;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.DimensionManager;

public class RegistryWriter {
	
	/**
	 * a live look at the arrays
	 */
	public static final File root = new File("./config/confighelper");
	public static final File dirBiomes = new File(root, "biomes");
	public static final File dirPotions = new File(root, "potions");
	public static final File dirEnchantments = new File(root, "enchantments");
	public static final File dirDimensions = new File(root, "dimensions");
	public static final File dirDatawatchers = new File(root,"datawatchers");
	public static final File dirEntities = new File(root, "entities");
	
	public static void mkdirs() 
	{
		mkdirs(root);
		mkdirs(dirBiomes);
		mkdirs(dirPotions);
		mkdirs(dirEnchantments);
		mkdirs(dirDimensions);
		mkdirs(dirDatawatchers);
		mkdirs(dirEntities);
	}
	
	private static void mkdirs(File dir){
		if(!dir.exists())
			dir.mkdir();
	}
	
	public static void output() 
	{
		try
		{
			RegistryWriter.grabNames();
			RegistryWriter.outputSuggestions();
			RegistryWriter.outputConflictedIds();
			RegistryWriter.outputFreeIds();
			RegistryWriter.outputWatcher();
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
	}
	
	public static void outputWatcher()
	{
		if(Registries.datawatchers == null)
			return;
		grabWatcherNames();
		outputWatcherSuggestions();
		outputWatcherConflicts();
		outputWatcherFreeIds();
	}
	
	private static void grabNames()
	{
		grabNames(Registries.biomes);
		grabNames(Registries.potions);
		grabNames(Registries.enchantments);
		grabNames(Registries.dimensions);
		grabNames(Registries.providers);
		grabNames(Registries.entities);
	}
	
	private static void grabNames(Registry reg)
	{
		for(List<Registry.Entry> li : reg.reg.values())
		{
			for(Registry.Entry entry : li)
			{
				entry.setName(grabName(reg.dataType, entry.newId));
			}
		}
	}
	
	private static String grabName(DataType dataType, int newId)
	{
		try
		{
			if(dataType == DataType.BIOME)
			{
				return BiomeGenBase.biomeList[newId].biomeName;
			}
			else if(dataType == DataType.POTION)
			{
				return Potion.potionTypes[newId].getName();
			}
			else if(dataType == DataType.ENCHANTMENT)
			{
				return Enchantment.enchantmentsList[newId].getName();
			}
			else if(dataType == DataType.PROVIDER)
			{
				WorldProvider provider = DimensionManager.providers.get(newId).newInstance();
				return provider.getDimensionName();
			}
			else if(dataType == DataType.DIMENSION)
			{
				return Registries.dimensions.isVanillaId(newId) ? "vanilla" : "modded";
			}
			else if(dataType == DataType.ENTITY)
			{
				return EntityList.getStringFromID(newId);
			}
			else if(dataType == DataType.DATAWATCHER)
			{
				return Registries.datawatchers.isVanillaId(newId) ? "vanilla" : "modded";
			}
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
		return null;
	}

	public static void outputSuggestions()
	{
		try
		{
			mkdirs();
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dirBiomes, "suggested.txt")));
			writeSuggested(writer, Registries.biomes);
			writer.close();
			writer = new BufferedWriter(new FileWriter(new File(dirPotions, "suggested.txt")));
			writeSuggested(writer, Registries.potions);
			writer.close();
			writer = new BufferedWriter(new FileWriter(new File(dirEnchantments, "suggested.txt")));
			writeSuggested(writer, Registries.enchantments);
			writer.close();
			writer = new BufferedWriter(new FileWriter(new File(dirDimensions, "suggested.txt")));
			writeSuggested(writer, Registries.providers);
			writer.close();
			writer = new BufferedWriter(new FileWriter(new File(dirEntities, "suggested.txt")));
			writeSuggested(writer, Registries.entities);
			writer.close();
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
				Integer i1 = ((Registry.Entry)arg0).suggested;
				Integer i2 = ((Registry.Entry)arg1).suggested;
				return i1.compareTo(i2);
			}
		});
		
		for(Registry.Entry e : entries)
		{
			if(!RegistryConfig.showVanillaIds && reg.isVanillaId(e.suggested) || e.replaced)
				continue;
			writer.write(e.suggested + " " + reg.getDisplay(e) + "\r\n");
		}
	}

	public static void outputConflictedIds()
	{
		try
		{
			mkdirs();
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dirBiomes,"conflicts.json")));
			writeConflicts(writer, Registries.biomes);
			writer.close();
		
			writer = new BufferedWriter(new FileWriter(new File(dirDimensions,"conflicts-providers.json")));
			writeConflicts(writer, Registries.providers);
			writer.close();
		
			writer = new BufferedWriter(new FileWriter(new File(dirDimensions,"conflicts-dimensions.json")));
			writeConflicts(writer, Registries.dimensions);
			writer.close();
		
			writer = new BufferedWriter(new FileWriter(new File(dirEntities,"conflicts.json")));
			writeConflicts(writer, Registries.entities);
			writer.close();
		
			writer = new BufferedWriter(new FileWriter(new File(dirPotions,"conflicts.json")));
			writeConflicts(writer, Registries.potions);
			writer.close();
		
			writer = new BufferedWriter(new FileWriter(new File(dirEnchantments,"conflicts.json")));
			writeConflicts(writer, Registries.enchantments);
			writer.close();
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
	}
	
	public static void writeConflicts(BufferedWriter writer, Registry reg) throws IOException, JSONParseException
	{
		JSONObject filejson = new JSONObject();
		for(Map.Entry<Integer, List<Registry.Entry>> map : reg.reg.entrySet())
		{
			int id = map.getKey();
			if(reg.isConflicting(id))
			{
				JSONArray arr = new JSONArray();
				filejson.put(reg.dataType.toString().toLowerCase() + "s-id:" + id, arr);
				for(Registry.Entry entry : reg.getEntry(id))
				{
					JSONObject json = new JSONObject();
					arr.add(json);
					if(entry.replaced)
					{
						json.put("replaced", true);
					}
					json.put("newId", entry.newId);
					json.put("name", entry.name);
					json.put("class", entry.clazz.getName());
				}
			}
		}
		if(!filejson.isEmpty())
		{
			writer.write(JavaUtil.toPrettyFormat(filejson.toString()));
		}
	}
	
	public static void outputFreeIds()
	{
		try
		{
			mkdirs();
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dirBiomes, "freeids.txt")));
			writeFreeIds(writer, RegistryConfig.biomeLimit, Registries.biomes);
			writer.close();
			
			writer = new BufferedWriter(new FileWriter(new File(dirPotions, "freeids.txt")));
			writeFreeIds(writer, RegistryConfig.potionsLimit, Registries.potions);
			writer.close();
			
			writer = new BufferedWriter(new FileWriter(new File(dirEnchantments, "freeids.txt")));
			writeFreeIds(writer, RegistryConfig.enchantmentsLimit, Registries.enchantments);
			writer.close();
			
			writer = new BufferedWriter(new FileWriter(new File(dirDimensions, "freeids.txt")));
			writeFreeDimIds(writer);
			writer.close();
			
			writer = new BufferedWriter(new FileWriter(new File(dirEntities, "freeids.txt")));
			writeFreeIds(writer, RegistryConfig.entities, Registries.entities);
			writer.close();
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
			if(Registries.dimensions.getEntry(i) == null && Registries.providers.getEntry(i) == null)
				writer.write(i + "\r\n");
		}
	}
	
	private static void grabWatcherNames()
	{
		for(List<Registry.Entry> li : Registries.datawatchers.reg.values())
		{
			for(Registry.Entry entry : li)
			{
				entry.setName(Registries.datawatchers.isVanillaId(entry.newId) ? "vanilla" : "modded");
			}
		}
	}
	
	private static void outputWatcherSuggestions() 
	{
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dirDatawatchers, "suggested.txt")));
			writeSuggested(writer, Registries.datawatchers);
			writer.close();
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
	}
	
	private static void outputWatcherConflicts()
	{
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dirDatawatchers,"conflicts.json")));
			writeConflicts(writer, Registries.datawatchers);
			writer.close();
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
	}
	
	private static void outputWatcherFreeIds()
	{
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dirDatawatchers, "freeids.txt")));
			writeFreeIds(writer, RegistryConfig.dataWatchersLimit, Registries.datawatchers);
			writer.close();
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
	}

}

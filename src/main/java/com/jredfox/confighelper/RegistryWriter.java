package com.jredfox.confighelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.ralleytn.simple.json.JSONArray;
import org.ralleytn.simple.json.JSONObject;
import org.ralleytn.simple.json.JSONParseException;

import com.evilnotch.lib.util.JavaUtil;
import com.google.common.collect.ListMultimap;
import com.jredfox.confighelper.Registry.DataType;

import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityList;
import net.minecraft.potion.Potion;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenMutated;
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
				if(reg.dataType == DataType.BIOME)
				{
					BiomeGenBase biome = BiomeGenBase.biomeList[entry.newId];
					Class c = biome != null ? biome.getBiomeClass() : entry.clazz;//for biomes that are not registered such as jungles but, are in my registry
					entry.modName = Registries.getModName(c);
				}
				else
				{
					entry.setModName();
				}
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
		Map<String,List<Registry.Entry>> entries = new TreeMap();//modname, list of entries
		//sort the entries based on mod
		for(List<Registry.Entry> list : reg.reg.values())
		{
			for(Registry.Entry entry : list)
			{
				String modname = entry.modName;
				List<Registry.Entry> map = entries.get(modname);
				if(map == null)
				{
					map = new ArrayList<Registry.Entry>();
					entries.put(modname, map);
				}
				map.add(entry);
			}
		}
		//sort the list of mod entries by name
		for(List<Registry.Entry> list : entries.values())
		{
			Collections.sort(list, new Comparator()
			{
				@Override
				public int compare(Object arg0, Object arg1) 
				{
					String i1 = ((Registry.Entry)arg0).name;
					String i2 = ((Registry.Entry)arg1).name;
					return i1.compareTo(i2);
				}
			});
		}
		
		for(Map.Entry<String, List<Registry.Entry>> map : entries.entrySet())
		{
			String modName = map.getKey();
			List<Registry.Entry> list = map.getValue();
			for(Registry.Entry e : list)
			{
				if(e.replaced)
					System.out.println("Passable Id Conflict for: " + reg.dataType + ", id:" + e.org + ", " + e.clazz.getName());
				if(!RegistryConfig.showVanillaIds && reg.isVanillaId(e.newId) || e.replaced)
					continue;
				
				writer.write(reg.getNextSuggestedId(e.newId) + " " + reg.getDisplay(e) + "\r\n");
			}
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
	
	public static void writeConflicts(BufferedWriter writer, Registry reg) throws IOException, JSONParseException, IllegalArgumentException, IllegalAccessException
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
					json.put("name", entry.name);
					if(entry.modName != null && !entry.modName.equals("Minecraft"))
						json.put("mod", entry.modName);
					if(entry.replaced)
						json.put("replaced", true);
					if(entry.newId != entry.org)
						json.put("freeId", reg.getNextFreeId(entry.newId));
					json.put("memoryIndex", entry.newId);
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
			if(!reg.containsOrg(i))
				writer.write(i + "\r\n");
		}
	}
	
	private static void writeFreeDimIds(BufferedWriter writer) throws IOException 
	{
		for(int i=RegistryConfig.searchDimLower;i<=RegistryConfig.searchDimUper;i++)
		{
			if(!Registries.dimensions.containsOrg(i) && !Registries.providers.containsOrg(i))
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
				entry.setModName();
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

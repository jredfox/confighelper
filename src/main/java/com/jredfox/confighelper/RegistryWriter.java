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
import com.jredfox.confighelper.Registry.Entry;

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
			RegistryWriter.outputProvider();
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
		Registries.biomes.grabNames();
		Registries.potions.grabNames();
		Registries.enchantments.grabNames();
		Registries.dimensions.grabNames();
		Registries.providers.grabNames();
		Registries.entities.grabNames();
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
			if(!canSuggestList(reg, list))
				continue;
			boolean hasModName = !(reg instanceof RegistryInt);
			if(hasModName)
				writer.write(modName + "\r\n");
			for(Registry.Entry e : list)
			{
				if(!canSuggest(reg, e))
					continue;
				writer.write(reg.getNextSuggestedId(e.newId) + " " + reg.getDisplay(e) + "\r\n");
			}
			if(hasModName)
				writer.write("\r\n");
		}
	}

	public static boolean canSuggestList(Registry reg, List<Registry.Entry> list) 
	{
		for(Registry.Entry e : list)
		{
			if(canSuggest(reg, e))
				return true;
		}
		return false;
	}

	private static boolean canSuggest(Registry reg, Entry e) 
	{
		if(e.replaced)
			return false;
		return RegistryConfig.showVanillaIds || !reg.isVanillaId(e.newId);
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
					boolean nonInt = !(reg instanceof RegistryInt);
					if(nonInt)
					{
						json.put("name", entry.name);
						json.put("mod", entry.modName);
					}
					if(entry.replaced)
						json.put("replaced", true);
					if(entry.newId != entry.org)
						json.put("freeId", reg.getNextFreeId(entry.newId));
					json.put("memoryIndex", entry.newId);
					if(nonInt)
						json.put("class", entry.clazz);
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
	
	/**
	 * outputs DimensionRegistry providers with keepLoaded boolean
	 */
	private static void outputProvider() 
	{
		try 
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dirDimensions, "providers-keeploaded.txt")));
			for(Map.Entry<Integer, Class<? extends WorldProvider>> map : DimensionManager.providers.entrySet())
			{
				int providerId = map.getKey();
				Class c = map.getValue();
				boolean keepLoaded = DimensionManager.spawnSettings.get(providerId);
				if(keepLoaded)
					writer.write(c.getName() + "<" + providerId + ">" + "=" +  keepLoaded + "\r\n");
			}
			writer.close();
		}
		catch (Throwable t) 
		{
			t.printStackTrace();
		}
	}
	
	private static void grabWatcherNames()
	{
		Registries.datawatchers.grabNames();
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

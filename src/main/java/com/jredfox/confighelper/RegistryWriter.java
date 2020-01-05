package com.jredfox.confighelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.ralleytn.simple.json.JSONArray;
import org.ralleytn.simple.json.JSONObject;
import org.ralleytn.simple.json.JSONParseException;

import com.evilnotch.lib.util.JavaUtil;
import com.evilnotch.lib.util.simple.Directory;
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
	
	public static final File root = new Directory("./config/confighelper").create();
	public static final File dirBiomes = new Directory(root, DataType.BIOME.getName()).create();
	public static final File dirPotions = new Directory(root, DataType.POTION.getName()).create();
	public static final File dirEnchantments = new Directory(root, DataType.ENCHANTMENT.getName()).create();
	public static final File dirDimensions = new Directory(root, DataType.DIMENSION.getName()).create();
	public static final File dirEntities = new Directory(root, DataType.ENTITY.getName()).create();
	public static final File dirDatawatchers = new Directory(root, DataType.DATAWATCHER.getName()).create();
	
	public static final String dumps = "dumps";
	public static final File dirDumpBiomes = new Directory(dirBiomes, dumps).create();
	public static final File dirDumpPotions = new Directory(dirPotions, dumps).create();
	public static final File dirDumpEnchantments = new Directory(dirEnchantments, dumps).create();
	public static final File dirDumpDimensions = new Directory(dirDimensions, dumps).create();
	public static final File dirDumpEntities = new Directory(dirEntities, dumps).create();
	public static final File dirDumpDatawatchers = new Directory(dirDatawatchers, dumps).create();
	
	public static final String extension =  ".txt";
	public static final String conflictExtension = ".json";
	public static final String conflicts = "conflicts";
	public static final String suggested = "suggested";
	public static final String freeids = "freeids";
	public static final String dumpIdsOrg = "ids-org";
	public static final String dumpIdsNew = "ids-new";
	
	public static void output() 
	{
		try
		{
			RegistryWriter.grabNames();
			RegistryWriter.outputSuggestions();
			RegistryWriter.outputConflictedIds();
			RegistryWriter.outputFreeIds();
			if(RegistryConfig.dumpIds)
				RegistryWriter.dumpIds();
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
		if(RegistryConfig.dumpIds)
			dumpWatcherIds();
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
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dirBiomes, suggested + extension)));
			writeSuggested(writer, Registries.biomes);
			writer.close();
			writer = new BufferedWriter(new FileWriter(new File(dirPotions, suggested + extension)));
			writeSuggested(writer, Registries.potions);
			writer.close();
			writer = new BufferedWriter(new FileWriter(new File(dirEnchantments, suggested + extension)));
			writeSuggested(writer, Registries.enchantments);
			writer.close();
			writer = new BufferedWriter(new FileWriter(new File(dirDimensions, suggested + extension)));
			writeSuggested(writer, Registries.providers);
			writer.close();
			writer = new BufferedWriter(new FileWriter(new File(dirEntities, suggested + extension)));
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
				writer.write(reg.getNextSuggestedId(e.newId) + " " + reg.getDisplay(e, false) + "\r\n");
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
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dirBiomes, conflicts + conflictExtension)));
			writeConflicts(writer, Registries.biomes);
			writer.close();
		
			writer = new BufferedWriter(new FileWriter(new File(dirDimensions, conflicts + "-providers" + conflictExtension)));
			writeConflicts(writer, Registries.providers);
			writer.close();
		
			writer = new BufferedWriter(new FileWriter(new File(dirDimensions, conflicts + "-dimensions" + conflictExtension)));
			writeConflicts(writer, Registries.dimensions);
			writer.close();
		
			writer = new BufferedWriter(new FileWriter(new File(dirEntities, conflicts + conflictExtension)));
			writeConflicts(writer, Registries.entities);
			writer.close();
		
			writer = new BufferedWriter(new FileWriter(new File(dirPotions, conflicts + conflictExtension)));
			writeConflicts(writer, Registries.potions);
			writer.close();
		
			writer = new BufferedWriter(new FileWriter(new File(dirEnchantments, conflicts + conflictExtension)));
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
				filejson.put(reg.dataType.getName() + "-id:" + id, arr);
				for(Registry.Entry entry : reg.getEntryOrg(id))
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
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dirBiomes, freeids + extension)));
			writeFreeIds(writer, Registries.biomes);
			writer.close();
			
			writer = new BufferedWriter(new FileWriter(new File(dirPotions, freeids + extension)));
			writeFreeIds(writer, Registries.potions);
			writer.close();
			
			writer = new BufferedWriter(new FileWriter(new File(dirEnchantments, freeids + extension)));
			writeFreeIds(writer, Registries.enchantments);
			writer.close();
			
			writer = new BufferedWriter(new FileWriter(new File(dirDimensions, freeids + extension)));
			writeFreeDimIds(writer);
			writer.close();
			
			writer = new BufferedWriter(new FileWriter(new File(dirEntities, freeids + extension)));
			writeFreeIds(writer, Registries.entities);
			writer.close();
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
	}
	
	private static void dumpIds() 
	{
		try
		{	
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dirDumpBiomes, dumpIdsNew + extension)));
			dumpIdsNew(writer, Registries.biomes);
			writer.close();//aaaaaaaaaaaaaaaaaaaaaa
			
			writer = new BufferedWriter(new FileWriter(new File(dirDumpPotions, dumpIdsNew + extension)));
			dumpIdsNew(writer, Registries.potions);
			writer.close();
			
			writer = new BufferedWriter(new FileWriter(new File(dirDumpEnchantments, dumpIdsNew + extension)));
			dumpIdsNew(writer, Registries.enchantments);
			writer.close();
			
			writer = new BufferedWriter(new FileWriter(new File(dirDumpDimensions, dumpIdsNew + extension)));
			dumpIdsNew(writer, Registries.providers);
			writer.close();
			
			writer = new BufferedWriter(new FileWriter(new File(dirDumpEntities, dumpIdsNew + extension)));
			dumpIdsNew(writer, Registries.entities);
			writer.close();
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
	}
	
	private static void dumpIdsNew(BufferedWriter writer, Registry reg) throws IOException
	{
		List<Registry.Entry> entries = reg.getAllEntries();
		Collections.sort(entries, new Comparator<Registry.Entry>()
		{
			@Override
			public int compare(Registry.Entry arg0, Registry.Entry arg1)
			{
				return ((Integer)arg0.newId).compareTo((Integer)arg1.newId);
			}
		});
		for(Registry.Entry entry : entries)
			writer.write("" + entry.newId + " " + reg.getDisplay(entry, true) + "\r\n");
	}

	private static void writeFreeIds(BufferedWriter writer, Registry reg) throws IOException
	{
		Set<Integer> usedIds = reg.getOrgIds();
		writeFreeIds(writer, reg.limitLower, reg.limit, usedIds);
	}
	
	public static void writeFreeDimIds(BufferedWriter writer) throws IOException
	{
		Set<Integer> joinedIds = new TreeSet();
		joinedIds.addAll(Registries.dimensions.getOrgIds());
		joinedIds.addAll(Registries.providers.getOrgIds());
		writeFreeIds(writer, Registries.dimensions.limitLower, Registries.dimensions.limit, joinedIds);
	}
	
	private static void writeFreeIds(BufferedWriter writer, int minLimit, int limit, Set<Integer> usedIds) throws IOException
	{
		Iterator<Integer> it = usedIds.iterator();
		int minId = minLimit;
		int maxId = minLimit;
		while(it.hasNext())
		{
			int usedId = it.next();
			maxId = usedId - 1;
			if(maxId >= minId)
				writer.write("id(" + getIdChunk(minId, maxId) + ")\r\n");
			minId = usedId + 1;//reset min id for the next use
		}
		if(minId <= limit)
		{
			maxId = limit;
			writer.write("id:(" + getIdChunk(minId, maxId) + ") ------> last\r\n");
		}
	}
	
	private static String getIdChunk(int minId, int maxId) 
	{
		if(minId == maxId)
			return "" + minId;
		return "" + minId + " - " + maxId;
	}

	private static void grabWatcherNames()
	{
		Registries.datawatchers.grabNames();
	}
	
	private static void outputWatcherSuggestions() 
	{
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dirDatawatchers, suggested + extension)));
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
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dirDatawatchers, conflicts + conflictExtension)));
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
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dirDatawatchers, freeids + extension)));
			writeFreeIds(writer, Registries.datawatchers);
			writer.close();
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
	}
	
	private static void dumpWatcherIds()
	{
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dirDumpDatawatchers, dumpIdsNew + extension)));
			dumpIdsNew(writer, Registries.datawatchers);
			writer.close();
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
	}

}

package jml.confighelper.reg;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import jml.confighelper.RegistryConfig;
import jml.confighelper.reg.Registry.Entry;
import jml.evilnotch.lib.JavaUtil;
import jml.evilnotch.lib.json.JSONArray;
import jml.evilnotch.lib.json.JSONObject;
import jml.evilnotch.lib.simple.Directory;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.world.biome.BiomeGenMutated;

public class RegistryWriter {
	
	public static final File root = new Directory(Launch.minecraftHome, "config/confighelper").create();
	public static final String dumps = "dumps";
	public static final String extension =  ".txt";
	public static final String conflictExtension = ".json";
	public static final String conflicts = "conflicts";
	public static final String suggested = "suggested";
	public static final String freeids = "freeids";
	public static final String dumpIdsNew = "ids-new";
	
	public Registry reg;
	public Directory dir;
	public Directory dirDump;
	
	public RegistryWriter(Registry reg)
	{
		this.reg = reg;
		this.dir = new Directory(root, this.reg.dataType.getName()).create();
		this.dirDump = new Directory(this.dir, dumps);
		if(RegistryConfig.dumpIds)
		{
			this.dirDump.create();
		}
		else
		{
			JavaUtil.deleteDir(this.dirDump);
		}
	}
	
	public void write()
	{
		try
		{
			this.reg.grabNames();
			this.writeConflicts();
			this.writeFreeIds();
			this.writeSuggestions();
			if(RegistryConfig.dumpIds)
				this.writeIds();
			this.reg.resetInfoIds();
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
	}

	private void writeConflicts() throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(this.dir, conflicts + conflictExtension)));
		JSONObject filejson = new JSONObject();
		for(Integer id : this.reg.getOrgIds())
		{
			if(reg.isConflicting(id))
			{
				JSONArray arr = new JSONArray();
				filejson.put(reg.dataType.getName() + "-id:" + id, arr);
				for(Registry.Entry entry : this.reg.getEntryOrg(id))
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
					if(!nonInt)
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
		writer.close();
	}
	
	private void writeFreeIds() throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(this.dir, freeids + extension)));
		Set<IdChunk> chunks = IdChunk.configureAround(this.reg.limitLower, this.reg.limit, this.reg.getOrgIds());
		for(IdChunk c : chunks)
		{
			writer.write(c.toString() + "\r\n");
		}
		writer.close();
	}

	private void writeSuggestions() throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(this.dir, suggested + extension)));
		Map<String,List<Registry.Entry>> entries = new TreeMap();//modname, list of entries
		//sort the entries based on mod
		for(List<Registry.Entry> list : this.reg.reg.values())
		{
			for(Registry.Entry entry : list)
			{
				if(!canSuggest(reg, entry))
					continue;
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
			boolean hasModName = !(reg instanceof RegistryInt);
			if(hasModName)
				writer.write(modName + "\r\n");
			if(RegistryConfig.suggestIdChunks)
			{
				Set<Integer> ids = new TreeSet();
				for(Registry.Entry e : list)
					ids.add(reg.getNextSuggestedId(e.newId));//add all possible suggested ids in order
				Set<IdChunk> chunks = IdChunk.configureRanges(ids);//format them into id chunks
				for(IdChunk c : chunks)
					writer.write(c.toString() + "\r\n");
			}
			else
			{
				for(Registry.Entry e : list)
				{
					int suggestion = reg.getNextSuggestedId(e.newId);
					writer.write(suggestion + " " + reg.getDisplay(e, false) + "\r\n");
				}
			}
			if(hasModName)
				writer.write("\r\n");
		}
		writer.close();
	}
	
	public boolean canSuggest(Registry reg, Entry e) 
	{
		if(e.replaced || e.obj instanceof BiomeGenMutated)
			return false;
		return !reg.isVanillaId(e.newId);
	}
	
	private void writeIds() throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(this.dirDump, dumpIdsNew + extension)));
		List<Registry.Entry> entries = this.reg.getEntriesSortable();
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
		writer.close();
	}

}

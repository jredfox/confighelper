package com.jredfox.crashwconflicts;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jredfox.util.IdChunk;
import com.jredfox.util.RegTypes;
import com.jredfox.util.RegUtils;

import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

public class AutoConfig {
	
	public Map<String, DataTypeEntry> dataTypes = new HashMap();
	public List<Config> cfgs = new ArrayList();
	public List<File> blacklisted = new ArrayList();
	
	public AutoConfig()
	{
		
	}
	
	public AutoConfig load()
	{
		clearOld();
		Configuration cfg = new Configuration(new File(CrashWConflicts.cwcMain, "autoConfig.cfg"));
		cfg.load();
		//parse the data types
		String[] defaultData = new String[]
		{
			"itemId:" + RegUtils.getMin(RegTypes.ITEMS) + ":" + RegUtils.getMax(RegTypes.ITEMS),
			"blockId:" + RegUtils.getMin(RegTypes.BLOCKS) + ":" + RegUtils.getMax(RegTypes.BLOCKS),
			"biomeId:" + RegUtils.getMin(RegTypes.BIOMES) + ":" + RegUtils.getMax(RegTypes.BIOMES),
			"enchantmentId:" + RegUtils.getMin(RegTypes.ENCHANTMENTS) + ":" + RegUtils.getMax(RegTypes.ENCHANTMENTS),
			"potionId:" + RegUtils.getMin(RegTypes.POTIONS) + ":" + RegUtils.getMax(RegTypes.POTIONS),
			"entityId:" + RegUtils.getMin(RegTypes.ENTITIES) + ":" + RegUtils.getMax(RegTypes.ENTITIES),
			"dimensionId:" + RegUtils.getMin(RegTypes.DIMENSIONS) + ":" + RegUtils.getMax(RegTypes.DIMENSIONS),
			"providerId:" + RegUtils.getMin(RegTypes.PROVIDERS) + ":" + RegUtils.getMax(RegTypes.PROVIDERS),
		};
		String[] dataTypes = cfg.get("autoconfig", "dataTypes", defaultData).getStringList();
		for(String line : dataTypes)
		{
			String[] vals = line.split(":");
			String type = vals[0].toLowerCase().trim();
			this.dataTypes.put(type, new DataTypeEntry(type, new IdChunk(Integer.parseInt(vals[1].trim()), Integer.parseInt(vals[2].trim()))));
		}
		
		//add the blacklisted files for auto config not to touch
		for(String s : cfg.get("autoconfig", "blacklisted", new String[]{"forge.cfg", "forgeChunkLoading.cfg"}).getStringList())
		{
			this.blacklisted.add(this.getFile(s));
		}
		
		//parse the auto config data entries
		String[] arr = cfg.get("autoconfig", "entries", new String[]{}, "the format is \"file.cfg;category:dataType\" To add more categories just append the \";category:dataType\"").getStringList();
		for(String s : arr)
		{
			s = s.substring(1, s.length() - 1).trim();
			String[] vals = s.split(";");
			File file = this.getFile(vals[0]);
			Cat[] cats = new Cat[vals.length - 1];
			for(int i=1;i<vals.length;i++)
			{
				String[] catArr = vals[i].split(":");
				String cat = catArr[0];
				String dataType = catArr[1].trim();
				Cat catObj = new Cat(cat, dataType);
				cats[i - 1] = catObj;
			}
			this.cfgs.add(new Config(file, cats));
		}
		cfg.save();
		return this;
	}
	
	public File getFile(String vals0) 
	{
		String fstr = vals0.trim();
		if(fstr.startsWith("root/"))
			fstr = new File(fstr.substring("root/".length())).getAbsolutePath();//correct the root directory instead of using root/config for realtive paths
		File file = new File(fstr);
		if(!file.isAbsolute())
			file = new File("config", fstr).getAbsoluteFile();
		return file;
	}

	public void clearOld()
	{
		dataTypes.clear();
		cfgs.clear();
	}

	/**
	 * config your modpack automatically after adding a list of configs and config sections
	 */
	public void run() 
	{
		for(Config cfgObj : this.cfgs)
		{
			Set<File> files = RegUtils.getDirFiles(cfgObj.cfgFile.getAbsoluteFile(), ".cfg");
			boolean isDir = files.size() > 1;
			for(File f : files)
			{
				if(this.blacklisted.contains(f))
				{
					System.out.println("skipping blacklisted file:" + f);
					continue;
				}
				Configuration cfg = new Configuration(f);
				cfg.load();
				for(Cat cat : cfgObj.cats)
				{
					DataTypeEntry dt = this.dataTypes.get(cat.dataType);
					if(dt == null)
					{
						System.err.println("skipping null dataType:" + cat.dataType);
						continue;
					}
					
					if(cat.cat.equals("*"))
					{
						for(String cn : cfg.getCategoryNames())
						{
							for(Property p : cfg.getCategory(cn).values())
							{
								if(p.isIntValue())
									p.set(dt.index++);
							}
						}
					}
					else if(cfg.hasCategory(cat.cat))
					{
						for(Property p : cfg.getCategory(cat.cat).values())
						{
							if(p.isIntValue())
								p.set(dt.index++);
						}
					}
				}
				cfg.save();
			}
		}
	}

	public class DataTypeEntry
	{
		public String type;
		public IdChunk range;
		public int index;
		
		public DataTypeEntry(String type, IdChunk chunk)
		{
			this.type = type;
			this.range = chunk;
			this.index = chunk.minId;
		}
		
		@Override
		public String toString()
		{
			return this.type + " index:" + this.index + " " + this.range;
		}
	}
	
	public class Config
	{
		public File cfgFile;
		public List<Cat> cats = new ArrayList();
		
		public Config(File f, Cat... o)
		{
			this.cfgFile = f;
			for(Cat c : o)
				cats.add(c);
		}
		
		@Override
		public String toString()
		{
			return this.cfgFile.getPath() + this.cats;
		}
	}
	
	public class Cat
	{
		public String cat;
		public String dataType;
		public Cat(String c, String d)
		{
			this.cat = c;
			this.dataType = d.toLowerCase();
		}
		
		@Override
		public String toString()
		{
			return this.cat + ":" + this.dataType;
		}
	}

}

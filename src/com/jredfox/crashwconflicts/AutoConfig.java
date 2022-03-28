package com.jredfox.crashwconflicts;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jredfox.crashwconflicts.cfg.ConfigVarBlock;
import com.jredfox.crashwconflicts.reg.Registry;
import com.jredfox.util.IdChunk;
import com.jredfox.util.RegTypes;
import com.jredfox.util.RegUtils;

import cpw.mods.fml.common.FMLLog;
import net.minecraft.crash.CrashReport;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

public class AutoConfig {
	
	public Map<String, DataTypeEntry> dataTypes = new HashMap();
	public List<Config> cfgs = new ArrayList();
	public List<File> blfs = new ArrayList();
	public List<File> blDirs = new ArrayList();
	public Set<String> done = new HashSet();
	public boolean useMaxIds;
	public String[] exts;
	
	public AutoConfig()
	{
		
	}
	
	public AutoConfig load()
	{
		this.clearOld();
		Configuration cfg = new Configuration(new File(CrashWConflicts.cwcMain, "autoConfig.cfg"));
		cfg.load();
		this.useMaxIds = cfg.get("autoconfig", "useMaxIdAlgorithm", false).getBoolean(false);
		this.exts = cfg.get("autoconfig", "extensions", "cfg,txt").getString().split(",");
		Set<String> defaultData = new LinkedHashSet();
		for(RegTypes t : RegTypes.values())
			defaultData.add(t.name().toLowerCase() + "Id:" + RegUtils.getMin(t) + ":" + RegUtils.getMax(t));
		String[] dataTypes = cfg.get("autoconfig", "dataTypes", RegUtils.toArray(defaultData, String.class)).getStringList();
		for(String line : dataTypes)
		{
			String[] vals = line.split(":");
			String type = vals[0].toLowerCase().trim();
			this.dataTypes.put(type, new DataTypeEntry(type, new IdChunk(Integer.parseInt(vals[1].trim()), Integer.parseInt(vals[2].trim()))));
		}
		
		//add the blacklisted files for auto config not to touch
		for(String s : cfg.get("autoconfig", "blacklisted", new String[]{"forge.cfg", "forgeChunkLoading.cfg"}).getStringList())
		{
			if(s.isEmpty())
				continue;
			File f = this.getFile(s);
			if(f.isDirectory())
				this.blDirs.add(f);
			else
				this.blfs.add(f);
		}
		
		//parse the auto config data entries
		String[] arr = cfg.get("autoconfig", "entries", new String[]{"\"root/config;item:itemId;block:blockId;biome:biomeId;biomes:biomeId\""}, "the format is \"file.cfg;category:dataType\" To add more categories just append the \";category:dataType\"").getStringList();
		for(String s : arr)
		{
			if(s.isEmpty())
				continue;
			s = s.substring(1, s.length() - 1).trim();
			String[] vals = s.split(";");
			File file = this.getFile(vals[0]);
			if(!file.exists())
			{
				System.err.println("config entry is maulformed or contains an invalid file skipping. File:\"" + file.getAbsolutePath() + "\" config entry:" + s);
				continue;
			}
			Cat[] cats = new Cat[vals.length - 1];
			for(int i=1;i<vals.length;i++)
			{
				String[] catArr = vals[i].split(":");
				String cat = catArr[0].trim();
				String dataType = catArr[1].trim();
				Cat catObj = new Cat(cat, dataType);
				cats[i - 1] = catObj;
			}
			this.cfgs.add(new Config(file, cats));
//			for(Cat c : this.cfgs.get(0).cats)
//				System.out.println("\"" + c + "\"");
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
		this.dataTypes.clear();
		this.cfgs.clear();
		this.blfs.clear();
		this.blDirs.clear();
		this.done.clear();
	}

	/**
	 * config your modpack automatically after adding a list of configs and config sections
	 */
	public void run() 
	{
		long ms = System.currentTimeMillis();
		for(Config cfgObj : this.cfgs)
		{
			List<File> files = RegUtils.getDirFiles(cfgObj.cfgFile.getAbsoluteFile(), this.exts);
			boolean isDir = files.size() > 1;
			Map<File, Configuration> cfgmap = new HashMap();
			for(File f : files)
			{
				if(this.isBlackListed(f))
				{
					System.out.println("skipping blf:" + f);
					continue;
				}
				Configuration cfg = this.getConfiguration(f, cfgmap);
				if(cfg == null)
				{
					FMLLog.warning("maulformed config skipping:%s", f);
					continue;
				}
				
				for(Cat cat : cfgObj.cats)
				{
					DataTypeEntry dt = this.dataTypes.get(cat.dataType);
					if(dt == null)
					{
						System.err.println("skipping NULL dataType:" + cat.dataType);
						continue;
					}
					
					if(cat.cat.equals("*"))
					{
						for(String cn : cfg.getCategoryNames())
						{
							if(this.hasDone(f, cn))
							{
								System.out.println("skipping dupe cat:" + cn + " from:*");
								continue;
							}
							for(Property p : cfg.getCategory(cn).values())
							{
								if(p.isIntValue())
									p.set(this.nextId(dt));
							}
						}
					}
					else if(cfg.hasCategory(cat.cat))
					{
						if(hasDone(f, cat.cat))
						{
							System.out.println("skipping dupe cat:" + cat.cat + " from:" + cat.dataType);
							continue;
						}
						for(Property p : cfg.getCategory(cat.cat).values())
						{
							if(p.isIntValue())
								p.set(this.nextId(dt));
						}
					}
				}
				cfg.save();
			}
		}
		System.out.println("AutoConfig completed in:" + (System.currentTimeMillis() - ms) + "ms");
	}

	/**
	 * returns if the file is a blacklisted file or is inside of a blacklisted directory
	 */
	public boolean isBlackListed(File f) 
	{
		return this.blfs.contains(f) || this.isBlacklistedDir(f);
	}

	public boolean isBlacklistedDir(File f) 
	{
		for(File blDir : this.blDirs)
		{
			if(RegUtils.isInsideDir(blDir, f))
				return true;
		}
		return false;
	}

	public Configuration getConfiguration(File f, Map<File, Configuration> cfgmap) 
	{
		if(cfgmap.containsKey(f))
			return cfgmap.get(f);
		
		try
		{
			Configuration cfg = new Configuration(f);
			cfgmap.put(f, cfg);
//			cfg.load(); TODO: 1.6+ check to see if it still loads via constructor
			return cfg;
		}
		catch(Throwable t)
		{
			return null;
		}
	}

	public boolean hasDone(File f, String cn)
	{
		String catUri = f.getPath() + ";" + cn;
		return !this.done.add(catUri);
	}

	public int nextId(DataTypeEntry dt)
	{
		if(!useMaxIds)
		{
			while(this.contains(dt))
				dt.index++;
			this.checkId(dt.index, dt);
			return dt.index++;//return the result and then increment dt.index by one for next use
		}
		else
		{
			while(this.contains(dt))
				dt.index--;
			this.checkId(dt.index, dt);
			return dt.index--;//return the result and then increment dt.index by one for next use
		}
	}

	public boolean contains(DataTypeEntry dt)
	{
		return RegUtils.getVanillaIds(dt.regType).contains(dt.index) || dt.regType == RegTypes.BLOCK ? RegUtils.getVanillaIds(RegTypes.ITEM).contains(dt.index) : false || this.isUnconfigured(dt);//if the type is block make sure that it has a freeid for it's item block
	}

	public boolean isUnconfigured(DataTypeEntry dt)
	{
		return (dt.regType == RegTypes.BLOCK || dt.regType == RegTypes.BLOCK_GEN) ? (Registry.blocks.unconfiguredIds.contains(dt.index) || Registry.items.unconfiguredIds.contains(dt.index)) : dt.regType == RegTypes.ITEM ? Registry.items.unconfiguredIds.contains(dt.index) : false;
	}

	public void checkId(int i, DataTypeEntry dt)
	{
		if(i > dt.range.maxId || i < dt.range.minId)
		{
			if(CrashWConflicts.proxy != null)
				CrashWConflicts.proxy.displayCrash(new CrashReport("id limit exceeded", new RuntimeException("out of free ids for:" + dt.type)));
			else if(!CrashWConflicts.isCrashing)
				throw new RuntimeException("out of free ids for:" + dt.type);
		}
	}

	public class DataTypeEntry
	{
		public String type;
		public IdChunk range;
		public int index;
		public RegTypes regType;//vanilla data types only
		
		public DataTypeEntry(String type, IdChunk chunk)
		{
			this.type = type;
			this.regType = this.getRegType(type);
			this.range = chunk;
			this.index = useMaxIds ? chunk.maxId : chunk.minId;
		}
		
		public RegTypes getRegType(String type)
		{
			try
			{
				return RegTypes.valueOf(type.toUpperCase().substring(0, type.length() - 2));
			}
			catch(Throwable t)
			{
				System.err.println("NULL DataType for:\"" + type.toUpperCase() + "\" Vanilla ids won't work on this DataType!");
				return null;
			}
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

package com.jredfox.crashwconflicts.auto;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.jredfox.crashwconflicts.CrashWConflicts;
import com.jredfox.util.IdChunk;
import com.jredfox.util.RegTypes;
import com.jredfox.util.RegUtils;

import net.minecraftforge.common.Configuration;

public class AutoConfigVals {
	
	public boolean lowercase;
	public boolean maxIds;
	public String[] exts;
	public Set<File> blfs = new HashSet();
	public Set<File> blDirs = new HashSet();
	public Map<String, DataTypeEntry> dataTypes = new HashMap();
	public Set<File> files = new HashSet();
	
	public void load()
	{
		this.clearOld();
		Configuration cfg = new Configuration(new File(CrashWConflicts.cwcMain, "autoConfig.cfg"));
		cfg.load();
		//parse config vals
		this.maxIds = cfg.get("autoconfig", "maxIds", false).getBoolean(false);
		this.lowercase = cfg.get("autoconfig", "lowercaseScripts", true).getBoolean(true);
		this.exts = cfg.get("autoconfig", "extensions", "cfg,txt").getString().split(",");
		
		//parse datatypes
		Set<String> defaultData = new LinkedHashSet();
		for(RegTypes t : RegTypes.values())
			defaultData.add(t.name().toLowerCase() + "Id:" + RegUtils.getMin(t) + ":" + RegUtils.getMax(t));
		String[] dataTypes = cfg.get("autoconfig", "dataTypes", RegUtils.toArray(defaultData, String.class)).getStringList();
		for(String line : dataTypes)
		{
			String[] vals = line.split(":");
			String type = vals[0].toLowerCase().trim();
			this.dataTypes.put(type, new DataTypeEntry(type, new IdChunk(Integer.parseInt(vals[1].trim()), Integer.parseInt(vals[2].trim())), this.maxIds));
		}
		
		//add the blacklisted files for auto config not to touch
		for(String s : cfg.get("autoconfig", "blacklisted", new String[]{"forge.cfg", "forgeChunkLoading.cfg"}).getStringList())
		{
			if(s.isEmpty())
				continue;
			File f = AutoConfig2.getFile(s);
			if(f.isDirectory())
				this.blDirs.add(f);
			else
				this.blfs.add(f);
		}
		this.blDirs.add(CrashWConflicts.cwcMain);//blacklist cwc as it won't return configurable mc datatypes and will result in maulformed configs
		
		//parse the files
		System.out.println("fetching files!");
		String[] fstr = cfg.get("autoconfig", "search", new String[]{"root/config"}).getStringList();
		Set<File> sfiles = new HashSet();
		for(String s : fstr)
		{
			File f = AutoConfig2.getFile(s.trim());
			if(!f.isDirectory())
				sfiles.add(f);
			this.files.addAll(RegUtils.getDirFiles(f, this.exts));
		}
		
		//remove blacklisted files
		Iterator<File> it = this.files.iterator();
		while(it.hasNext())
		{
			File f = it.next();
			if(!sfiles.contains(f) && this.isBlackListed(f))//for specified files in the search it will now ignore the blacklist
				it.remove();
		}
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
	
	public void clearOld()
	{
		this.dataTypes.clear();
		this.blfs.clear();
		this.blDirs.clear();
		this.files.clear();
	}

}

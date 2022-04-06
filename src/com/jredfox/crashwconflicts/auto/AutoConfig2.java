package com.jredfox.crashwconflicts.auto;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cpw.mods.fml.common.FMLLog;
import net.minecraftforge.common.Configuration;

public class AutoConfig2 {
	
	public AutoConfigVals vals = new AutoConfigVals();
	
	public void load()
	{
		this.vals.load();
	}
	
	public void run()
	{
		Map<String, DataTypeEntry> dts = this.vals.dataTypes;
		Set<File> files = this.vals.files;
		for(File f : files)
		{
			Configuration forgeconfig = this.getConfiguration(f);
			if(forgeconfig == null)
			{
				FMLLog.warning("maulformed config skipping:%s", f);
				continue;
			}
			forgeconfig.save();//with the new auto config we can just directly assume we are done with the config and save it directly no need for additonal maps
		}
	}
	
	public static Configuration getConfiguration(File f)
	{
		try{
			Configuration cfg = new Configuration(f);
//			cfg.load(); TODO: 1.6+ check to see if it still loads via constructor
			return cfg;
		}
		catch(Throwable t){
			return null;
		}
	}
	
	public static File getFile(String vals0) 
	{
		String fstr = vals0.trim();
		if(fstr.startsWith("root/"))
			fstr = new File(fstr.substring("root/".length())).getAbsolutePath();//correct the root directory instead of using root/config for realtive paths
		File file = new File(fstr);
		if(!file.isAbsolute())
			file = new File("config", fstr).getAbsoluteFile();
		return file;
	}

}

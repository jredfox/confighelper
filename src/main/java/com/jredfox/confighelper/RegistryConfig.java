package com.jredfox.confighelper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraftforge.common.config.Configuration;

public class RegistryConfig {
	
	//registry config
	public static int biomeLimit = 255;
	public static int potionsLimit = 255;
	public static int enchantmentsLimit = 255;
	public static int searchDimLower = -100;
	public static int searchDimUper = 300;
	public static int entities = 255;
	public static int dataWatchersLimit = 254;
	public static int dimensionLimit = Integer.MAX_VALUE;
	
	public static boolean showVanillaIds;
	public static boolean configMode = true;
	public static String[] passable = new String[0];
	public static Set<Integer> passableIds;
	
	static
	{
		Configuration cfg = new Configuration(new File("./config/confighelper/main.cfg"));
		cfg.load();
		showVanillaIds = cfg.getBoolean("showVanillaIds", "general", showVanillaIds, "disable this to only show modded ids in suggestion files");
		configMode = cfg.getBoolean("configMode", "general", configMode, "disable this when your modpack has been configured properly so it runs faster");
		passable = cfg.getStringList("conflictExceptions", "general", passable, "add a list of classes that it's acceptible to conflict with the registry");
		passableIds = getPassableIds(cfg);
		cfg.addCustomCategoryComment("limit", "changing these will not increase/decrease the limit of the ids. This is just so If a mod does extend the ids you can change them");
		biomeLimit = cfg.get("limit", "biome", biomeLimit).getInt();
		searchDimUper = cfg.get("limit", "searchDimUper", searchDimUper).getInt();
		searchDimLower = cfg.get("limit", "searchDimLower", searchDimLower).getInt();
		potionsLimit = cfg.get("limit", "potions", potionsLimit).getInt();
		enchantmentsLimit = cfg.get("limit", "enchantments", enchantmentsLimit).getInt();
		entities = cfg.get("limit", "entities", entities).getInt();
		dataWatchersLimit = cfg.get("limit", "dataWatchers", dataWatchersLimit).getInt();
		cfg.save();
	}

	private static Set<Integer> getPassableIds(Configuration cfg) 
	{
		String[] list = cfg.getStringList("conflictIdExceptions", "general", new String[0], "Used for dimension/datawatcher ids. Do not input ids here unless you know what you are doing");
		passableIds = new HashSet();
		for(int i=0;i<list.length;i++)
		{
			try
			{
				passableIds.add(Integer.parseInt(list[i]));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return passableIds;
	}

}

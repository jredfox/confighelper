package com.jredfox.confighelper;

import java.io.File;

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
	public static boolean showVanillaIds = true;
	
	static
	{
		Configuration cfg = new Configuration(new File("./config/confighelper/main.cfg"));
		cfg.load();
		showVanillaIds = cfg.getBoolean("showVanillaIds", "general", showVanillaIds, "disable this to only show modded ids in suggestion files");
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

}

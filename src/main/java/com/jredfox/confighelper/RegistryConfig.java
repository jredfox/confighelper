package com.jredfox.confighelper;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class RegistryConfig {
	
	//registry config
	public static int biomeLimit = 256;
	public static int potionsLimit = 256;
	public static int enchantmentsLimit = 32767;
	public static int searchDimLower = -100;
	public static int searchDimUper = 300;
	public static int searchEntities = 256;
	public static int dataWatchersLimit = 31;
	
	//mod config
	public static boolean autoConfig = true;
	
	static
	{
		Configuration cfg = new Configuration(new File("./config/confighelper/main.cfg"));
		cfg.load();
		autoConfig = cfg.get("general", "autoConfig", autoConfig).getBoolean();
		
		cfg.addCustomCategoryComment("limit", "changing these will not increase/decrease the limit of the ids. It's if another mod does increase this for compatability");
		biomeLimit = cfg.get("limit", "biome", biomeLimit).getInt();
		searchDimUper = cfg.get("limit", "searchDimUper", searchDimUper).getInt();
		searchDimLower = cfg.get("limit", "searchDimLower", searchDimLower).getInt();
		potionsLimit = cfg.get("limit", "potions", potionsLimit).getInt();
		enchantmentsLimit = cfg.get("limit", "enchantments", enchantmentsLimit).getInt();
		searchEntities = cfg.get("limit", "searchEntities", searchEntities).getInt();
		dataWatchersLimit = cfg.get("limit", "dataWatchers", dataWatchersLimit).getInt();
		cfg.save();
	}

}

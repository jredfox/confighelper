package com.jredfox.confighelper;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class RegistryConfig {
	
	public static int biomeLimit = 256;
	public static int dimensionLimit = Integer.MAX_VALUE;
	public static int providerLimit = Integer.MAX_VALUE;
	public static int searchDim = 500;
	public static int potionsLimit = 256;
	public static int enchantmentsLimit = 32767;
	public static int entitiesLimit = 256;
	public static int dataWatchersLimit = 31;

	public static boolean autoConfig = true;
	public static void load()
	{
		Configuration cfg = new Configuration(new File("./config/confighelper/main.cfg"));
		cfg.load();
		autoConfig = cfg.get("general", "autoConfig", autoConfig).getBoolean();
		cfg.addCustomCategoryComment("limit", "changing these will not increase/decrease the limit of the ids. It's if another mod does increase this for compatability");
		biomeLimit = cfg.get("limit", "biome", biomeLimit).getInt();
		dimensionLimit = cfg.get("limit", "dimSearch", searchDim).getInt();
		potionsLimit = cfg.get("limit", "potions", potionsLimit).getInt();
		enchantmentsLimit = cfg.get("limit", "enchantments", enchantmentsLimit).getInt();
		entitiesLimit = cfg.get("limit", "entities", entitiesLimit).getInt();
		dataWatchersLimit = cfg.get("limit", "dataWatchers", dataWatchersLimit).getInt();
		cfg.save();
	}

}

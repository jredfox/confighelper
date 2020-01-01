package com.jredfox.confighelper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.evilnotch.lib.util.JavaUtil;

import net.minecraftforge.common.config.Configuration;

public class RegistryConfig {
	
	//registry config
	public static int biomeLimit = 255;
	public static int potionsLimit = 255;
	public static int enchantmentsLimit = 255;
	public static int entities = 255;
	public static int dataWatchersLimit = Integer.MAX_VALUE;
	public static int dimLimitUpper = Integer.MAX_VALUE;
	public static int dimLimitLower = Integer.MIN_VALUE;
	
	//mod config
	public static boolean configMode = true;
	public static boolean dumpIds;
	public static boolean showVanillaIds;
	public static boolean regUnregBiomes = true;
	//passable ids
	public static String[] passable = new String[]
	{
			"chylex.hee.world.biome.BiomeGenHardcoreEnd",
			"chylex.hee.world.WorldProviderHardcoreEnd",
			"chylex.hee.entity.mob.EntityMobEnderman",
			"chylex.hee.entity.block.EntityBlockEnderCrystal"
	};
	public static String[] passableSelf = new String[]
	{
			"net.aetherteam.aether.dungeons.worldgen.DungeonsBiome"
	};
	public static Set<Integer> passableDimIds;
	public static Set<Integer> passableWatcherIds;
	//optimizations
	public static boolean unloadModDimIds = true;
	
	static
	{
		Configuration cfg = new Configuration(new File("./config/confighelper/main.cfg"));
		cfg.load();
		showVanillaIds = cfg.getBoolean("showVanillaIds", "general", showVanillaIds, "disable this to only show modded ids in suggestion files");
		configMode = cfg.getBoolean("configMode", "general", configMode, "disable this when your modpack has been configured properly so it runs faster");
		regUnregBiomes = cfg.getBoolean("regUnregBiomes", "general", regUnregBiomes, "will prevent future biome conflicts if un registerd biomes get registerd later");
		dumpIds = cfg.getBoolean("dumpIds", "general", false, "dump original requested and memory indexed ids");
		
		passable = cfg.getStringList("conflicts", "passable", passable, "passable Classes that are allowed to conflict(replace) a registry object");
		passableSelf = cfg.getStringList("selfConflicts", "passable", passableSelf, "passable Classes that are allowed to conflict with itself");
		passableDimIds = getPassableIds(cfg, "conflictDimIds", "passable Dim ids(Not Provider) that are allowed to conflict. Only use if inputting the provider conflict class wasn't enough");
		passableWatcherIds = getPassableIds(cfg, "conflictWatcherIds", "passable ids that data watchers are allowed to conflict with");
		
		cfg.addCustomCategoryComment("limit", "changing these will not increase/decrease the limit of the ids. This is just so If a mod does extend the ids you can change them");
		biomeLimit = cfg.get("limit", "biome", biomeLimit).getInt();
		potionsLimit = cfg.get("limit", "potions", potionsLimit).getInt();
		enchantmentsLimit = cfg.get("limit", "enchantments", enchantmentsLimit).getInt();
		entities = cfg.get("limit", "entities", entities).getInt();
		
		unloadModDimIds = cfg.getBoolean("unloadModDimIds", "optimization", unloadModDimIds, "enabled: (less laggy)");
		cfg.save();
	}

	private static Set<Integer> getPassableIds(Configuration cfg, String name, String comment) 
	{
		String[] list = cfg.getStringList(name, "passable", new String[0], comment);
		Set<Integer> passable = new HashSet();
		for(int i=0;i<list.length;i++)
		{
			try
			{
				passable.add(Integer.parseInt(list[i]));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return passable;
	}

}

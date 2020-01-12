package com.jredfox.confighelper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.evilnotch.lib.JavaUtil;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.config.Configuration;

public class RegistryConfig {
	
	//mod config
	public static boolean configMode = true;
	public static boolean suggestIdChunks = true;
	public static boolean dumpIds;
	public static boolean regUnregBiomes = true;
	public static boolean autoBiomeMutated = true;
	
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
		Configuration cfg = new Configuration(new File(Launch.minecraftHome, "config/confighelper/main.cfg"));
		cfg.load();
		configMode = cfg.getBoolean("configMode", "general", configMode, "disable this when your modpack has been configured properly so it runs faster");
		regUnregBiomes = cfg.getBoolean("regUnregBiomes", "general", regUnregBiomes, "will prevent future biome conflicts if un registerd biomes get registerd later");
		dumpIds = cfg.getBoolean("dumpIds", "general", dumpIds, "dump original requested and memory indexed ids");
		suggestIdChunks = cfg.getBoolean("suggestIdChunks", "general", suggestIdChunks, "disable this to veiw more details");
		autoBiomeMutated = cfg.getBoolean("autoBiomeMutated", "general", autoBiomeMutated, "if enabled client must have the same mods and loading order");
				
		passable = cfg.getStringList("conflicts", "passable", passable, "passable Classes that are allowed to conflict(replace) a registry object");
		passableSelf = cfg.getStringList("selfConflicts", "passable", passableSelf, "passable Classes that are allowed to conflict with itself");
		passableDimIds = getPassableIds(cfg, "conflictDimIds", "passable Dim ids(Not Provider) that are allowed to conflict. Only use if inputting the provider conflict class wasn't enough");
		passableWatcherIds = getPassableIds(cfg, "conflictWatcherIds", "passable ids that data watchers are allowed to conflict with");
		
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

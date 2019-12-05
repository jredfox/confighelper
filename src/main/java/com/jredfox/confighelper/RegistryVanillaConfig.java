package com.jredfox.confighelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.jredfox.confighelper.Registry.DataType;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityList;
import net.minecraft.potion.Potion;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class RegistryVanillaConfig {
	
	public static Map<Integer,Integer> cfgBiomes = new HashMap<Integer,Integer>();
	public static Map<Integer,Integer> cfgDimensions = new HashMap<Integer,Integer>();
	public static Map<Integer,Integer> cfgPotions = new HashMap<Integer,Integer>();
	public static Map<Integer,Integer> cfgEnchantments = new HashMap<Integer,Integer>();
	public static Map<Integer,Integer> cfgEntities = new HashMap<Integer,Integer>();
	public static Map<Integer,Integer> cfgDataWatcherPlayers = new HashMap<Integer,Integer>();
	/**
	 * when in mdk this may be manually turned into true
	 */
	public static final boolean genVanillacfg = false;
	public static final File vanillacfg = new File("./config/confighelper/vanilla/" + MinecraftForge.MC_VERSION + "/vanilla.cfg");
	
	static
	{
		if(!genVanillacfg)
			loadConfig();
		else
		{
			if(vanillacfg.exists())
				vanillacfg.delete();
			genConfigs();
		}
	}
	
	private static void loadConfig() 
	{
		if(!vanillacfg.exists())
		{
			vanillacfg.getParentFile().mkdirs();
			moveFileFromJar(RegistryVanillaConfig.class, "assets/" + ConfigHelperMod.MODID + "/" + MinecraftForge.MC_VERSION + "/vanilla.cfg", vanillacfg, true);
		}
		Configuration cfg = new Configuration(vanillacfg);
		cfg.load();
		populateIntMap(cfgBiomes, cfg.getCategory("biomes"));
		populateDim(cfg.getCategory("dimensions"));
		populateIntMap(cfgPotions, cfg.getCategory("potions"));
		populateIntMap(cfgEnchantments, cfg.getCategory("enchantments"));
		populateIntMap(cfgEntities, cfg.getCategory("entities"));
		cfg.save();
	}
	
	@SuppressWarnings("rawtypes")
	public static void moveFileFromJar(Class clazz, String input, File output, boolean replace) {
		if(output.exists() && !replace)
			return;
		try {
			InputStream inputstream =  clazz.getClassLoader().getResourceAsStream(input);
			FileOutputStream outputstream = new FileOutputStream(output);
			output.createNewFile();
			IOUtils.copy(inputstream,outputstream);
			inputstream.close();
			outputstream.close();
		} catch (Exception io) {io.printStackTrace();}
	}
	
	private static void populateDim(ConfigCategory cat) 
	{
		for(Map.Entry<String, Property> map : cat.entrySet())
		{
			String str = map.getKey();
			String strId = str.substring(0, str.indexOf(' '));
			cfgDimensions.put(Integer.parseInt(strId), (Integer)map.getValue().getInt());
		}
	}
	
	private static void populateIntMap(Map<Integer, Integer> cfg, ConfigCategory cat) 
	{
		for(Map.Entry<String, Property> map : cat.entrySet())
		{
			String str = map.getKey();
			String strId = str.substring(str.indexOf("id:") + "id:".length(), str.length());
			cfg.put(Integer.parseInt(strId), (Integer)map.getValue().getInt());
		}
	}

	/**
	 * use in mdk to generate vanilla configs
	 */
	public static void genConfigs()
	{
		Configuration cfg = new Configuration(vanillacfg);
		cfg.load();
		int index = 0;
		for(BiomeGenBase b : BiomeGenBase.biomeList)
		{
			if(b != null && Registry.isVanillaObj(b))
			{
				cfg.get("biomes", b.biomeName + " id:" + b.biomeID, index++).getInt();
			}
		}
		index = 0;
		for(Potion p : Potion.potionTypes)
		{
			if(p != null && Registry.isVanillaObj(p))
			{
				cfg.get("potions", p.getName() + " id:" + p.id, index++).getInt();
			}
		}
		index = 0;
		for(Enchantment e : Enchantment.enchantmentsList)
		{
			if(e != null && Registry.isVanillaObj(e))
			{
				cfg.get("enchantments", e.getName() + " id:" + e.effectId, index++).getInt();
			}
		}
		index = 0;
		for(Map.Entry<Integer, Class> map : EntityList.IDtoClassMapping.entrySet())
		{
			if(Registry.isVanillaObj(map.getValue()))
			{
				int id = map.getKey();
				String name = EntityList.getStringFromID(id);
				cfg.get("entities", name + " id:" + id, index++).getInt();
			}
		}
		
		for(Integer dim : DimensionManager.providers.keySet())
		{
			Class c = DimensionManager.providers.get(dim);
			if(Registry.isVanillaObj(c))
			{
				cfg.get("dimensions", dim + " " + c.getName(), dim);
			}
		}
		cfg.save();
	}

	/**
	 * get the configured vanilla id from the original
	 */
	public static int getId(DataType dataType, int org) 
	{
		if(genVanillacfg)
			return org;
		if(dataType == DataType.BIOME)
			return getBiomeId(org);
		else if(dataType == DataType.DIMENSION || dataType == DataType.PROVIDER)
			return getDimensionId(org);
		else if(dataType == DataType.POTION)
			return getPotionId(org);
		else if(dataType == DataType.ENCHANTMENT)
			return getEnchantmentId(org);
		else if(dataType == DataType.ENTITY)
			return getEntityId(org);
		else if(dataType == DataType.DATAWATCHERPLAYER)
			return getDataWatcherId(org);
		return -1;
	}

	private static int getDataWatcherId(int org) {
		return cfgDataWatcherPlayers.get(org);
	}

	private static int getEntityId(int org) {
		return cfgEntities.get(org);
	}

	private static int getEnchantmentId(int org) {
		return cfgEnchantments.get(org);
	}

	private static int getPotionId(int org) {
		return cfgPotions.get(org);
	}

	private static int getBiomeId(int org) {
		return cfgBiomes.get(org);
	}
	
	private static int getDimensionId(int org) {
		return cfgDimensions.get(org);
	}

}

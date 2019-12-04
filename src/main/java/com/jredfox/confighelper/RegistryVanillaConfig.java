package com.jredfox.confighelper;

import java.io.File;
import java.util.Map;

import com.jredfox.confighelper.Registry.DataType;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityList;
import net.minecraft.potion.Potion;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.config.Configuration;

public class RegistryVanillaConfig {
	
	static
	{
		
	}
	
	/**
	 * use in mdk to generate vanilla configs
	 */
	public static void genConfigs()
	{
		Configuration cfg = new Configuration(new File("./config/confighelper/vanilla/vanilla.cfg"));
		cfg.load();
		int index = 0;
		for(BiomeGenBase b : BiomeGenBase.biomeList)
		{
			if(b != null && Registry.getClass(b).getName().startsWith("net.minecraft."))
			{
				cfg.get("biomes", b.biomeName + " id:" + b.biomeID, index++).getInt();
			}
		}
		index = 0;
		for(Potion p : Potion.potionTypes)
		{
			if(p != null && Registry.getClass(p).getName().startsWith("net.minecraft."))
			{
				cfg.get("potions", p.getName() + " id:" + p.id, index++).getInt();
			}
		}
		index = 0;
		for(Enchantment e : Enchantment.enchantmentsList)
		{
			if(e != null && Registry.getClass(e).getName().startsWith("net.minecraft."))
			{
				cfg.get("enchantments", e.getName() + " id:" + e.effectId, index++).getInt();
			}
		}
		index = 0;
		for(Map.Entry<Integer, Class> map : EntityList.IDtoClassMapping.entrySet())
		{
			if(map.getValue().getName().startsWith("net.minecraft."))
			{
				int id = map.getKey();
				String name = EntityList.getStringFromID(id);
				cfg.get("entities", name + " id:" + id, index++).getInt();
			}
		}
		
		for(Integer dim : DimensionManager.providers.keySet())
		{
			String c = DimensionManager.providers.get(dim).getName();
			if(c.startsWith("net.minecraft."))
			{
				cfg.get("dimensions", dim + " " + c, dim);
			}
		}
		cfg.save();
	}

	/**
	 * get the configured vanilla id from the original
	 */
	public static int getId(DataType dataType, int org) 
	{
		if(true)
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
		// TODO Auto-generated method stub
		return 0;
	}

	private static int getEntityId(int org) {
		// TODO Auto-generated method stub
		return 0;
	}

	private static int getEnchantmentId(int org) {
		// TODO Auto-generated method stub
		return 0;
	}

	private static int getPotionId(int org) {
		// TODO Auto-generated method stub
		return 0;
	}

	private static int getBiomeId(int org) 
	{
		// TODO Auto-generated method stub
		return 0;
	}
	
	private static int getDimensionId(int org) {
		// TODO Auto-generated method stub
		return 0;
	}

}

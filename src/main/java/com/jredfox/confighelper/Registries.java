package com.jredfox.confighelper;

import com.evilnotch.lib.util.JavaUtil;
import com.google.common.collect.ListMultimap;
import com.jredfox.confighelper.Registry.DataType;
import com.jredfox.confighelper.proxy.ClientProxy;
import com.jredfox.confighelper.proxy.ServerProxy;

import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.potion.Potion;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.biome.BiomeGenBase;

/**
 * The central Registry system for all data types to be used upon
 * TileEntities, Blocks and Items are already automated so no need for a registry here
 * @author jredfox
 */
public class Registries {
	
	public static boolean hasConflicts;
	public static boolean startup = true;
	
	public static Registry biomes = new Registry(DataType.BIOME);
	public static Registry enchantments = new Registry(DataType.ENCHANTMENT);
	public static Registry potions = new Registry(DataType.POTION);
	public static Registry dimensions = new RegistryDim();
	public static Registry providers = new RegistryProvider();
	public static Registry entities = new Registry(DataType.ENTITY);
	/**
	 * the last EntityPlayer data watcher object list. Warning will be null on startup
	 */
	public static Registry datawatchers;
	
	public static int registerBiome(BiomeGenBase biome, int id)
	{
		return register(biome, id, biomes);
	}
	
	public static int registerProvider(Class providerObj, int providerId)
	{
		return register(providerObj, providerId, providers);
	}
	
	public static int registerDimension(int dimId)
	{
		return register(Integer.class, dimId, dimensions);
	}
	
	public static int registerPotion(Potion p, int id)
	{
		return register(p, id, potions);
	}
	
	public static int registerEnchantment(Enchantment ench, int id)
	{
		return register(ench, id, enchantments);
	}
	
	public static int registerEntity(Class entity, int id) 
	{
		return register(entity, id, entities);
	}
	
	public static int registerDataWatcher(Class entityClass, int id, Registry reg)
	{
		return register(entityClass, id, reg);
	}
	
	public static int register(Object obj, int id, Registry reg)
	{
		return reg.reg(obj, id);
	}
	
	public static int registerBlock(Object obj, int id)
	{
		throw new RuntimeException("Blocks are already Automated Ids");
	}
	
	public static int registerItem(Object obj, int id)
	{
		throw new RuntimeException("Items are already Automated Ids");
	}
	
	public static int registerTileEntity(Object obj, int id)
	{
		throw new RuntimeException("Tile Entitities are already Automated Ids");
	}
	
	public static void output()
	{
		RegistryWriter.output();
	}
	
	public static void outputWatcher()
	{
		RegistryWriter.outputWatcher();
	}
	
	public static void makeCrashReport(String cat, String msg) 
	{
		boolean isClient = false;
		try
		{
			Class c = DedicatedServer.class;
		}
		catch(Throwable t)
		{
			isClient = true;
		}
		if(isClient)
		{
			ClientProxy.makeCrashReport(cat, msg);
		}
		else
		{
			ServerProxy.makeCrashReport(cat, msg);
		}
	}
	
	public static LoadController loadController;
	public static ListMultimap<String, ModContainer> packageOwners;
	
	public static String getModName(Class clazz)
	{		
		if(clazz.getName().startsWith("net.minecraft."))
			return "Minecraft";
		if(packageOwners == null)
		{
			try 
			{
				loadController = (LoadController) ReflectionHelper.findField(Loader.class, "modController").get(Loader.instance());
				packageOwners = (ListMultimap<String, ModContainer>) ReflectionHelper.findField(LoadController.class, "packageOwners").get(loadController);
			} 
			catch (Throwable t)
			{
				t.printStackTrace();
			}
			if(packageOwners == null)
				return "packageOwners-" + null;
		}
		String name = clazz.getName();
		String pakage = name.substring(0,JavaUtil.findLastChar(name, '.'));
		if(packageOwners.containsKey(pakage))
		{
			ModContainer mod = packageOwners.get(pakage).get(0);
			if(mod.getModId().equals("examplemod"))
			{
				for(ModContainer c : packageOwners.get(pakage))
				{
					if(!c.getModId().equals("examplemod") && !c.getModId().equals("minecraft"))
					{
						return c.getName();
					}
				}
			}
			return "" + mod.getName();
		}
		return "" + null;
	}

}

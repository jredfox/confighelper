package com.jredfox.confighelper.reg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.evilnotch.lib.JavaUtil;
import com.google.common.collect.ListMultimap;
import com.jredfox.confighelper.RegistryConfig;
import com.jredfox.confighelper.datawatcher.WatcherDataType;
import com.jredfox.confighelper.proxy.ClientProxy;
import com.jredfox.confighelper.proxy.ServerProxy;
import com.jredfox.confighelper.reg.Registry.DataType;

import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.Potion;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.DimensionManager;

/**
 * The central Registry system for all data types to be used upon
 * TileEntities, Blocks and Items are already automated so no need for a registry here
 * @author jredfox
 */
public class Registries {
	
	public static boolean loading = true;
	public static Registry biomes = new Registry(DataType.BIOME);
	public static Registry potions = new Registry(DataType.POTION);
	public static Registry enchantments = new Registry(DataType.ENCHANTMENT);
	public static Registry dimensions = new RegistryDim();
	public static Registry providers = new RegistryProvider();
	public static Registry entities = new Registry(DataType.ENTITY);
	/**
	 * the last EntityPlayer data watcher object list. Warning will be null on startup
	 */
	public static Registry datawatchers;
	
	public static int registerBiome(BiomeGenBase biome, int id, boolean reg)
	{
		if(reg || !reg && RegistryConfig.regUnregBiomes)
		{
			return register(biome, id, biomes);
		}
		System.out.println("Returning Original Biome Id as it's flagged to not be registered:" + id);
		return id;
	}
	
	public static int registerPotion(Potion p, int id)
	{
		return register(p, id, potions);
	}
	
	public static int registerEnchantment(Enchantment ench, int id)
	{
		return register(ench, id, enchantments);
	}
	
	public static int registerProvider(Class<? extends WorldProvider> providerObj, int providerId)
	{
		return register(providerObj, providerId, providers);
	}
	
	public static int registerDimension(int providerId, int dimId)
	{
		return register(new EntryDimension(providerId, dimId), dimId, dimensions);
	}
	
	public static int registerEntity(Class<? extends Entity> entity, String entityName, int id) 
	{
		return register(new EntryEntity(entity, entityName), id, entities);
	}
	
	public static int registerDataWatcher(Entity entity, int id, Registry reg)
	{
		if(!(entity instanceof EntityPlayer))
		{
			return id;
		}
		datawatchers = reg;
		return register(entity, id, reg);
	}
	
	public static int register(Object obj, int id, Registry reg)
	{
		return reg.reg(obj, id);
	}
	
	public static int registerBlock(Block obj, int id)
	{
		throw new RuntimeException("Blocks are already Automated Ids");
	}
	
	public static int registerItem(Item obj, int id)
	{
		throw new RuntimeException("Items are already Automated Ids");
	}
	
	public static int registerTileEntity(TileEntity obj, int id)
	{
		throw new RuntimeException("Tile Entitities are already Automated Ids");
	}
	
	public static void unregisterProvider(int id) 
	{
		unregister(id, providers);
	}
	
	public static void unregisterDimension(int id) 
	{
		unregister(id, dimensions);
	}
	
	public static void unregister(int id, Registry reg)
	{
		reg.unreg(id);
	}
	
	public static boolean hasConflicts() 
	{
		return biomes.hasConflicts || 
				potions.hasConflicts || 
				enchantments.hasConflicts || 
				dimensions.hasConflicts || 
				providers.hasConflicts ||
				entities.hasConflicts ||
				hasWatcherConflicts();
	}

	public static boolean hasWatcherConflicts() 
	{
		return datawatchers != null ? datawatchers.hasConflicts : false;
	}
	
	public static void strictRegs()
	{
		biomes.strict = true;
		potions.strict = true;
		enchantments.strict = true;
		dimensions.strict = true;
		providers.strict = true;
		entities.strict = true;
		strictWatcher();
	}
	
	public static void strictWatcher() 
	{
		if(datawatchers != null)
			datawatchers.strict = true;
	}
	
	public static String getConflictTypes() 
	{
		StringBuilder b = new StringBuilder();
		b.append('[');
		if(Registries.biomes.hasConflicts)
			b.append(Registries.biomes.dataType + ", ");
		if(Registries.potions.hasConflicts)
			b.append(Registries.potions.dataType + ", ");
		if(Registries.enchantments.hasConflicts)
			b.append(Registries.enchantments.dataType + ", ");
		if(Registries.dimensions.hasConflicts)
			b.append(Registries.dimensions.dataType + ", ");
		if(Registries.providers.hasConflicts)
			b.append(Registries.providers.dataType + ", ");
		if(Registries.entities.hasConflicts)
			b.append(Registries.entities.dataType + ", ");
		if(Registries.datawatchers != null && Registries.entities.hasConflicts)
			b.append(Registries.datawatchers.dataType + ", ");
		String c = b.toString();
		return c.substring(0, c.length()-2) + "]";
	}
	
	public static void potionSecurity() 
	{
		Potion[] potions = Potion.potionTypes;
		int limit =  Byte.MAX_VALUE + 1;
		//if it's greater then a signed byte and not greater then an unsigned byte patch potions
		if(potions.length > limit && potions.length < 256 + 1)
		{
			System.out.println("Fixing Potion ids. A Dumb Mod has extended potions to unsiged byte it can be only a signed byte 0-127!");
			for(int i=128; i < potions.length; i++)
			{
				Potion p = potions[i];
				if(p != null)
				{
					Registries.potions.checkId(p, i);
				}
			}
			Potion[] fixed = new Potion[limit];
			for(int i=0; i < fixed.length; i++)
			{
				fixed[i] = potions[i];
			}
			Potion.potionTypes = fixed;
		}
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
	
	/**
	 * get the crash report category based on loading or in game
	 */
	public static String getCat()
	{
		return Registries.loading ? "Loading" : "In Game";
	}
	
	public static LoadController loadController;
	public static ListMultimap<String, ModContainer> packageOwners;
	/**
	 * get a modname from a class object is pretty expensive use with caution
	 */
	public static String getModName(String clazz)
	{		
		if(clazz.startsWith("net.minecraft."))
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
		String pakage = clazz.substring(0, JavaUtil.findLastChar(clazz, '.'));
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
	
	public static int nextDim = Integer.MAX_VALUE;
	public static int nextDimFrozen = nextDim;
	
	public static boolean keepDimLoaded(int id, boolean keepLoaded) 
	{
		return RegistryConfig.unloadModDimIds && !Registries.providers.isVanillaId(id) ? false : keepLoaded;
	}
	
	/**
	 * DimensionManager providers and dimIds are unlinked if a conflict occurs 
	 * the best way to link them is to guess
	 */
	public static int guessProviderId(int providerId) 
	{
		List<Registry.Entry> list = Registries.providers.getEntryOrg(providerId);
		return list.get(list.size() - 1).newId;
	}
	
	/**
	 * DimensionManager providers and dimIds are unlinked if a conflict occurs 
	 * the best way to link them is to guess
	 */
	public static int guessDimOrgId(int providerId) 
	{
		for(List<Registry.Entry> li : dimensions.reg.values())
		{
			for(Registry.Entry e : li)
			{
				EntryDimension dim = (EntryDimension) e.obj;
				if(providerId == dim.providerNewId)
					return e.org;
			}
		}
		System.out.println("GUESS DIM ID FROM PROVIDER FAILED! returning default:\t" + providerId);
		return providerId;
	}

	public static Registry createWatcherReg(Entity e) 
	{
		return e instanceof EntityPlayer ? new RegistryDatawatcher() : null;
	}
	
	public static Map<Integer, WatcherDataType> reg = new HashMap(0);
	public static void registerWatcherDataType(WatcherDataType type)
	{
		if(reg.containsKey(type.dataType))
			throw new IllegalArgumentException("DataWatcher DataType id conflict!" + type.dataType);
		else if(type.dataType < 0 || type.dataType > 254)
			throw new IllegalArgumentException("DataWatcher DataType cannot be above" + 254 + " or less then 0");
		reg.put(type.dataType, type);
		DataWatcher.dataTypes.put(type.clazz, type.dataType);
	}
	
	public static void writeWatcher(PacketBuffer buf, int dataType, Object object) 
	{
		reg.get(dataType).write(buf, object);
	}

	public static Object readWatcher(PacketBuffer buf, int dataType) 
	{
		return reg.get(dataType).read(buf);
	}

}

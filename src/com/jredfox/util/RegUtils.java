package com.jredfox.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.jredfox.crashwconflicts.CrashWConflicts;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.DimensionManager;

/**
 * all version specific minecraft code needs to be updated her before porting to x version of the game with the exception of free ids and item shift +-256
 * @author jredfox
 */
public class RegUtils {
	
	public static boolean isClassExtending(Class<? extends Object> base, Class<? extends Object> toCompare) 
	{
		return base.isAssignableFrom(toCompare);
	}
	
	public static <T> void registerOrgId(RegTypes type, int id) 
	{
		if(CrashWConflicts.isCrashing)
			throw new RuntimeException("stop registering while crashing:" + type + " id:" + id);
		getOrgIds(type).add(id);
	}
	
	public static int getMax(RegTypes type)
	{
		switch(type)
		{
			case ITEMS:
				return Item.itemsList.length - 1;
			case BLOCKS:
				return Block.blocksList.length - 1;
			case BLOCK_GEN:
				return 255;
			case BIOMES:
				return BiomeGenBase.biomeList.length - 1;
			case ENCHANTMENTS:
				return Enchantment.enchantmentsList.length - 1;
			case POTIONS:
				return Potion.potionTypes.length - 1;
			case ENTITIES:
				return CrashWConflicts.entId;
			case PROVIDERS:
				return Integer.MAX_VALUE;
			case DIMENSIONS:
				return Integer.MAX_VALUE;
		}
		return -1;
	}

	public static int getMin(RegTypes type) 
	{
		switch(type)
		{
			case ITEMS:
				return Block.blocksList.length - 256;//avoid ItemBlocks as free ids it's -1 for length then +1 to start the index so no need to say -1 +1
			case BLOCKS:
				return 256;
			case BLOCK_GEN:
				return 0;
			case PROVIDERS:
				return Integer.MIN_VALUE;
			case DIMENSIONS:
				return Integer.MIN_VALUE;
			default:
				return 0;
		}
	}

	public static Object[] getArr(Collection<Integer> col, int size) 
	{
		Integer[] arr = new Integer[size];
		for(Integer i : col)
			if(i < arr.length)
				arr[i] = (Integer)i;//take the index of the id and turn it into a non null object aka itself as a non free id
		return arr;
	}
	
	public static BufferedWriter getWriter(File f) throws FileNotFoundException
	{
		return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8));
	}

	public static Set<Integer> getOrgIds(RegTypes type) 
	{
		switch(type)
		{
			case ITEMS:
				return CrashWConflicts.itemsOrg;
			case BLOCKS:
				return CrashWConflicts.blocksOrg;
			case BLOCK_GEN:
				return CrashWConflicts.blocksOrg;
			case BIOMES:
				return CrashWConflicts.biomesOrg;
			case ENCHANTMENTS:
				return CrashWConflicts.enchantmentsOrg;
			case POTIONS:
				return CrashWConflicts.potionsOrg;
			case ENTITIES:
				return CrashWConflicts.entitiesOrg;
			case PROVIDERS:
				return CrashWConflicts.providersOrg;
			case DIMENSIONS:
				return CrashWConflicts.dimensionsOrg;
		}
		return null;
	}

	/**
	 * ensures vanilla data types are all loaded
	 */
	public static void init()
	{
		Object o = Item.itemsList;
		o = Block.blocksList;
		o = BiomeGenBase.biomeList;
		o = Enchantment.enchantmentsList;
		o = Potion.potionTypes;
		o = EntityList.IDtoClassMapping;
		o = DimensionManager.getDimensions();//does providers and dimensions init
	}

	public static <T> List<T> asArr(int[] arr) 
	{
		List li = new ArrayList(arr.length);
		for(int i : arr)
			li.add(i);
		return li;
	}

	public static void getDirFiles(File dir, Set<File> files, String ext, boolean blackList) 
	{
	    for (File file : dir.listFiles()) 
	    {
	    	boolean isType = blackList ? (!file.getName().endsWith(ext)) : (file.getName().endsWith(ext) || ext.equals("*") );
	        if (file.isFile() && isType)
	        {
	            files.add(file);
	        }
	        else if (file.isDirectory()) 
	        {
	        	getDirFiles(file, files, ext, blackList);
	        }
	    }
	}
	
	public static void getDirFiles(File dir, Set<File> files) 
	{
		getDirFiles(dir, files, "*", false);
	}
	
	public static Set<File> getDirFiles(File dir, String ext)
	{
		Set<File> files = new HashSet();
		if(!dir.isDirectory())
		{
			files.add(dir);
			return files;
		}
		getDirFiles(dir, files, ext, false);
		return files;
	}

}

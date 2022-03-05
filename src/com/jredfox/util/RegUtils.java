package com.jredfox.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
	
	/**
	 * generated from 
	 * for(RegTypes t : RegTypes.values())
	 * 		System.out.println(t + "" + RegUtils.getOrgIds(t));
	 */
	public static final List<Integer> id_items = RegUtils.asArr(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 157, 158, 2256, 2257, 2258, 2259, 2260, 2261, 2262, 2263, 2264, 2265, 2266, 2267, 256, 257, 258, 259, 260, 261, 262, 263, 264, 265, 266, 267, 268, 269, 270, 271, 272, 273, 274, 275, 276, 277, 278, 279, 280, 281, 282, 283, 284, 285, 286, 287, 288, 289, 290, 291, 292, 293, 294, 295, 296, 297, 298, 299, 300, 301, 302, 303, 304, 305, 306, 307, 308, 309, 310, 311, 312, 313, 314, 315, 316, 317, 318, 319, 320, 321, 322, 323, 324, 325, 326, 327, 328, 329, 330, 331, 332, 333, 334, 335, 336, 337, 338, 339, 340, 341, 342, 343, 344, 345, 346, 347, 348, 349, 350, 351, 352, 353, 354, 355, 356, 357, 358, 359, 360, 361, 362, 363, 364, 365, 366, 367, 368, 369, 370, 371, 372, 373, 374, 375, 376, 377, 378, 379, 380, 381, 382, 383, 384, 385, 386, 387, 388, 389, 390, 391, 392, 393, 394, 395, 396, 397, 398, 399, 400, 401, 402, 403, 404, 405, 406, 407, 408});
	public static final List<Integer> id_blocks = RegUtils.asArr(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 157, 158});
	public static final List<Integer> id_biomes = RegUtils.asArr(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22});
	public static final List<Integer> id_enchantments = RegUtils.asArr(new int[]{0, 32, 1, 33, 2, 34, 3, 35, 4, 5, 6, 7, 16, 48, 17, 49, 18, 50, 19, 51, 20, 21});
	public static final List<Integer> id_potions = RegUtils.asArr(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
	public static final List<Integer> id_entities = RegUtils.asArr(new int[]{1, 2, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 200, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 120});
	public static final List<Integer> id_dimensions = RegUtils.asArr(new int[]{0, -1, 1});
	public static final List<Integer> id_empty = new ArrayList(0);
	public static final int ITEM_SHIFT = 256;
	
//	public static <T> T[] getArr(RegTypes type)
//	{
//		switch(type)
//		{
//			case ITEM:
//				return (T[]) Item.itemsList;
//			case BLOCK:
//				return (T[]) Block.blocksList;
//			case BLOCK_GEN:
//				return (T[]) Block.blocksList;
//			case BIOME:
//				return (T[]) BiomeGenBase.biomeList;
//			case ENCHANTMENT:
//				return (T[]) Enchantment.enchantmentsList;
//			case POTION:
//				return (T[]) Potion.potionTypes;
//			case ENTITY:
//				return (T[]) toArray(EntityList.IDtoClassMapping.keySet(), Integer.class);
//			case DIMENSION:
//				return (T[]) DimensionManager.getStaticDimensionIDs();
//			case PROVIDER:
//				return (T[]) DimensionManager.getStaticProviderIDs();//TODO: make this cached for AutoConfig
//		}
//		return null;
//	}
	
	public static List<Integer> getVanillaIds(RegTypes type)
	{
		switch(type)
		{
			case ITEM:
				return id_items;
			case BLOCK:
				return id_blocks;
			case BLOCK_GEN:
				return id_blocks;
			case BIOME:
				return id_biomes;
			case ENCHANTMENT:
				return id_enchantments;
			case POTION:
				return id_potions;
			case ENTITY:
				return id_entities;
			case DIMENSION:
				return id_dimensions;
			case PROVIDER:
				return id_dimensions;
		}
		return id_empty;
	}
	
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
	
	public static int getMin(RegTypes type)
	{
		return getMin(type, false);
	}
	
	public static int getMax(RegTypes type)
	{
		return getMax(type, false);
	}
	
	public static int getMin(RegTypes type, boolean unshifted) 
	{
		switch(type)
		{
			case ITEM:
				return unshifted ? Block.blocksList.length : Block.blocksList.length - ITEM_SHIFT;//avoid ItemBlocks as free ids it's -1 for length then +1 to start the index so no need to say -1 +1
			case BLOCK:
				return 256;
			case PROVIDER:
				return Integer.MIN_VALUE;
			case DIMENSION:
				return Integer.MIN_VALUE;
			default:
				return 0;
		}
	}
	
	public static int getMax(RegTypes type, boolean unshifted)
	{
		switch(type)
		{
			case ITEM:
				return unshifted ? Item.itemsList.length - 1 : Item.itemsList.length - (ITEM_SHIFT + 1);
			case BLOCK:
				return Block.blocksList.length - 1;
			case BLOCK_GEN:
				return 255;
			case BIOME:
				return BiomeGenBase.biomeList.length - 1;
			case ENCHANTMENT:
				return Enchantment.enchantmentsList.length - 1;
			case POTION:
				return Potion.potionTypes.length - 1;
			case ENTITY:
				return CrashWConflicts.entId;
			case PROVIDER:
				return Integer.MAX_VALUE;
			case DIMENSION:
				return Integer.MAX_VALUE;
		}
		return Integer.MIN_VALUE;
	}
	
	public static IdChunk unshiftIdChunk(RegTypes type, IdChunk chunk)
	{
		return type == RegTypes.ITEM ? new IdChunk(chunk.minId - ITEM_SHIFT, chunk.maxId - ITEM_SHIFT) : chunk;
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
			case ITEM:
				return CrashWConflicts.itemsOrg;
			case BLOCK:
				return CrashWConflicts.blocksOrg;
			case BLOCK_GEN:
				return CrashWConflicts.blocksOrg;
			case BIOME:
				return CrashWConflicts.biomesOrg;
			case ENCHANTMENT:
				return CrashWConflicts.enchantmentsOrg;
			case POTION:
				return CrashWConflicts.potionsOrg;
			case ENTITY:
				return CrashWConflicts.entitiesOrg;
			case PROVIDER:
				return CrashWConflicts.providersOrg;
			case DIMENSION:
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

	public static <T> T[] toArray(Collection<T> col, Class<T> clazz)
	{
	    T[] li = (T[]) Array.newInstance(clazz, col.size());
	    int index = 0;
	    for(T obj : col)
	    {
	        li[index++] = obj;
	    }
	    return li;
	}

	/**
	 * returns if the specified file is inside the base dir
	 */
	public static boolean isInsideDir(File baseDir, File file)
	{
		return file.getAbsolutePath().startsWith(baseDir.getAbsolutePath());
	}

	public static RegTypes getRegType(Map<Integer, String> conflicts)
	{
		if(conflicts == CrashWConflicts.biomes)
			return RegTypes.BIOME;
		else if(conflicts == CrashWConflicts.blocks)
			return RegTypes.BLOCK;
		else if(conflicts == CrashWConflicts.dimensions)
			return RegTypes.DIMENSION;
		else if(conflicts == CrashWConflicts.enchantments)
			return RegTypes.ENCHANTMENT;
		else if(conflicts == CrashWConflicts.entities)
			return RegTypes.ENTITY;
		else if(conflicts == CrashWConflicts.items)
			return RegTypes.ITEM;
		else if(conflicts == CrashWConflicts.potions)
			return RegTypes.POTION;
		else if(conflicts == CrashWConflicts.providers)
			return RegTypes.PROVIDER;
		return null;
	}

}

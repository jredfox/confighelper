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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.jredfox.crashwconflicts.CrashWConflicts;
import com.jredfox.crashwconflicts.reg.Registry;
import com.jredfox.crashwconflicts.reg.Registry.RegEntry;

import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.EntityRegistry.EntityRegistration;
import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.util.StatCollector;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.DimensionManager;

/**
 * all version specific minecraft code needs to be updated her before porting to x version of the game with the exception of free ids and item shift +-256
 * @author jredfox
 */
public class RegUtils {
	
	public static final String MC_VERSION = "1.5.2";//the mc version which RegUtils currently supports
	public static final List<Integer> id_items = RegUtils.asArr(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 157, 158, 2256, 2257, 2258, 2259, 2260, 2261, 2262, 2263, 2264, 2265, 2266, 2267, 256, 257, 258, 259, 260, 261, 262, 263, 264, 265, 266, 267, 268, 269, 270, 271, 272, 273, 274, 275, 276, 277, 278, 279, 280, 281, 282, 283, 284, 285, 286, 287, 288, 289, 290, 291, 292, 293, 294, 295, 296, 297, 298, 299, 300, 301, 302, 303, 304, 305, 306, 307, 308, 309, 310, 311, 312, 313, 314, 315, 316, 317, 318, 319, 320, 321, 322, 323, 324, 325, 326, 327, 328, 329, 330, 331, 332, 333, 334, 335, 336, 337, 338, 339, 340, 341, 342, 343, 344, 345, 346, 347, 348, 349, 350, 351, 352, 353, 354, 355, 356, 357, 358, 359, 360, 361, 362, 363, 364, 365, 366, 367, 368, 369, 370, 371, 372, 373, 374, 375, 376, 377, 378, 379, 380, 381, 382, 383, 384, 385, 386, 387, 388, 389, 390, 391, 392, 393, 394, 395, 396, 397, 398, 399, 400, 401, 402, 403, 404, 405, 406, 407, 408});
	public static final List<Integer> id_blocks = RegUtils.asArr(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 157, 158});
	public static final List<Integer> id_biomes = RegUtils.asArr(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22});
	public static final List<Integer> id_enchantments = RegUtils.asArr(new int[]{0, 32, 1, 33, 2, 34, 3, 35, 4, 5, 6, 7, 16, 48, 17, 49, 18, 50, 19, 51, 20, 21});
	public static final List<Integer> id_potions = RegUtils.asArr(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
	public static final List<Integer> id_entities = RegUtils.asArr(new int[]{1, 2, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 200, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 120});
	public static final List<Integer> id_dimensions = RegUtils.asArr(new int[]{0, -1, 1});
	public static final List<Integer> id_empty = new ArrayList(0);
	public static final int ITEM_SHIFT = 256;
	
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
	
	public static String getName(int id, RegTypes type, Object o) throws Exception
	{
		if(o instanceof Item)
			return ((Item)o).getStatName();
		else if(o instanceof Block)
			return ((Block)o).getLocalizedName();
		else if(o instanceof BiomeGenBase)
			return ((BiomeGenBase)o).biomeName;
		else if(o instanceof Enchantment)
			return trans(((Enchantment)o).getName());
		else if(o instanceof Potion)
			return trans(((Potion)o).getName());
		else if(type == RegTypes.PROVIDER)
		{
			Class<? extends WorldProvider> c = (Class<? extends WorldProvider>) o;
			WorldProvider provider = c.newInstance();
			provider.setDimension(id);
			return provider.getDimensionName();
		}
		else if(type == RegTypes.DIMENSION)
			return RegUtils.id_dimensions.contains(id) ? "vanilla" : "modded";
		else if(type == RegTypes.ENTITY)
			return getTransEntity((Class<? extends Entity>)o);
		
		return "";
	}
	
	public static String trans(String unlocal) 
	{
		String str = StatCollector.translateToLocal(unlocal);
		return str.isEmpty() ? unlocal : str;
	}

	/**
	 * method adapted from mo spawn eggs mod which is also written by me
	 */
	public static String getTransEntity(Class<? extends Entity> o)
	{
		String entityId = (String) EntityList.classToStringMapping.get(o);
        String s1 = StatCollector.translateToLocal("entity." + entityId + ".name");
        if(s1.startsWith("entity."))
        {
        	EntityRegistration er = EntityRegistry.instance().entityClassRegistrations.get(EntityList.stringToClassMapping.get(entityId));
        	if(er != null)
        	{
        		s1 = StatCollector.translateToLocal("entity." + er.getEntityName() + ".name");
        		if(s1.startsWith("entity."))
        			return er.getEntityName();//returns spawn + " " + realEntityId without the modid.entityId as the translated name when regular translation fails
        	}
        }
        if(s1.startsWith("entity."))
        	s1 = entityId;
        return s1;
	}

	public static boolean isClassExtending(Class<? extends Object> base, Class<? extends Object> toCompare) 
	{
		return base.isAssignableFrom(toCompare);
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
		return -1;
	}
	
	public static IdChunk unshiftIdChunk(RegTypes type, IdChunk chunk)
	{
		return type == RegTypes.ITEM ? new IdChunk(chunk.minId - ITEM_SHIFT, chunk.maxId - ITEM_SHIFT) : chunk;
	}
	
	public static int unshiftId(RegTypes type, int id)
	{
		return type == RegTypes.ITEM ? id - ITEM_SHIFT : id;
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
	
	public static Set<Integer> getFreeIds(RegTypes type)
	{
		Set<Integer> ids = getOrgIds(type);
		if(type == RegTypes.BLOCK)
		{
			ids = copyShallow(ids);
			ids.addAll(getVanillaIds(RegTypes.ITEM));
		}
		return ids;
	}

	/**
	 * returns a shallow copy of a list the objects inside the list are not copied
	 */
	public static <T> Set<T> copyShallow(Set<T> set)
	{
		Set<T> copySet = new HashSet(set.size());
		copySet.addAll(set);
		return copySet;
	}

	public static Set<Integer> getOrgIds(RegTypes type) 
	{
		switch(type)
		{
			case ITEM:
				return Registry.items.orgIds;
			case BLOCK:
				return Registry.blocks.orgIds;
			case BLOCK_GEN:
				return Registry.blocks.orgIds;
			case BIOME:
				return Registry.biomes.orgIds;
			case ENCHANTMENT:
				return Registry.enchantments.orgIds;
			case POTION:
				return Registry.potions.orgIds;
			case ENTITY:
				return Registry.entities.orgIds;
			case PROVIDER:
				return Registry.providers.orgIds;
			case DIMENSION:
				return Registry.dimensions.orgIds;
		}
		return null;
	}

	/**
	 * ensures vanilla data types are all loaded except datawatchers as I don't support those yet
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

	public static List<File> getDirFiles(File dir)
	{
		return getDirFiles(dir, "*");
	}
	
	/**
	 * get a list of files from a file or directory
	 */
	public static List<File> getDirFiles(File dir, String... exts)
	{
		return getDirFiles(dir, exts, false);
	}
	
	/**
	 * get a list of files from a file or directory. has blacklist extension support
	 */
	public static List<File> getDirFiles(File dir, String[] exts, boolean blacklist) 
	{
		if(!dir.exists())
			return Collections.emptyList();
		if(!dir.isDirectory())
		{
			List<File> li = new ArrayList(1);
			String ext = getExtension(dir);
			boolean isType = blacklist ? !isExtEqual(ext, exts) : isExtEqual(ext, exts);
			if(isType)
				li.add(dir);
			return li;
		}
		List<File> list = new ArrayList(dir.listFiles().length);
		getDirFiles(list, dir, exts, blacklist);
		return list;
	}
	
	public static boolean isExtEqual(String orgExt, String... exts)
	{
		orgExt = orgExt.toLowerCase();
		for(String ext : exts)
		{
			if(ext.equals("*") || orgExt.isEmpty() && ext.equals("noextension") || orgExt.equals(ext))
				return true;
		}
		return false;
	}
	
	protected static void getDirFiles(List<File> files, File dir, String[] exts, boolean blacklist) 
	{
	    for (File file : dir.listFiles()) 
	    {
	    	String extension = getExtension(file);
	    	boolean isType = blacklist ? !isExtEqual(extension, exts) : isExtEqual(extension, exts);
	        if (file.isFile() && isType)
	        {
	            files.add(file);
	        }
	        else if (file.isDirectory()) 
	        {
	        	getDirFiles(files, file, exts, blacklist);
	        }
	    }
	}
	
	public static String getExtensionFull(File file) 
	{
		String ext = getExtension(file);
		return ext.isEmpty() ? "" : "." + ext;
	}

	/**
	 * get a file extension. Note directories do not have file extensions
	 */
	public static String getExtension(File file) 
	{
		String name = file.getName();
		int index = name.lastIndexOf('.');
		return index != -1 && !file.isDirectory() ? name.substring(index + 1) : "";
	}
	
	/**
	 * no directory support use at your own risk
	 */
	public static String getExtension(String name) 
	{
		int index = name.lastIndexOf('.');
		return index != -1 ? name.substring(index + 1) : "";
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
	 * get the object class if it's not already a class
	 */
	public static <T> Class<?> getOClass(T obj)
	{
		return obj instanceof Class ? (Class<?>)obj : obj.getClass();
	}

	/**
	 * returns if the specified file is inside the base dir
	 */
	public static boolean isInsideDir(File baseDir, File file)
	{
		return file.getAbsolutePath().startsWith(baseDir.getAbsolutePath());
	}

	public static <T> T getFirst(Collection<T> col) 
	{
		Iterator<T> it = col.iterator();
		return it.next();
	}

}

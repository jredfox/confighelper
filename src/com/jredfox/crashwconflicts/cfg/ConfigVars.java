package com.jredfox.crashwconflicts.cfg;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

public class ConfigVars {
	
    public static int ITEM_SHIFT = 256;
    public static int ITEM_MIN = Block.blocksList.length;
    public static int ITEM_MAX = Item.itemsList.length - 1;
    public static int item_index = Item.itemsList.length - 1;
    public static boolean[] mark_blocks = new boolean[Block.blocksList.length];
    public static boolean[] mark_items = new boolean[Item.itemsList.length];

}

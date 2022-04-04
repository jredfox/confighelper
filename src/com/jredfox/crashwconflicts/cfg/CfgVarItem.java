package com.jredfox.crashwconflicts.cfg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

public class CfgVarItem {

	public static int ITEM_SHIFT = 256;
	public static int ITEM_MIN = Block.blocksList.length;
	public static int ITEM_MAX = Item.itemsList.length - 1;
	public static int item_index = Item.itemsList.length - 1;
    public static CfgMarker markItems = new CfgMarker();



}

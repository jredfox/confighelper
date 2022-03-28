package com.jredfox.crashwconflicts.cfg;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

public class ConfigVarBlock {
	
    public static boolean[] mark_blocks = new boolean[Block.blocksList.length];
    
    public static void markBlock(int i)
    {
    	mark_blocks[i] = true;
    }
    
}

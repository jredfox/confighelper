package com.jredfox.confighelper;

import java.io.File;

import com.jredfox.confighelper.Registry.DataType;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = ConfigHelperMod.MODID, version = ConfigHelperMod.VERSION, name = ConfigHelperMod.NAME)
public class ConfigHelperMod
{
    public static final String MODID = "confighelper";
    public static final String VERSION = "1.0";
    public static final String NAME = "Config Helper";
    
    @EventHandler
    public void preinit(FMLPreInitializationEvent event)
    {
    	MinecraftForge.EVENT_BUS.register(new DataWatcherEvent());
    }
    
    /**
     * once the game has completely initialized output suggested ids for all mods
     */
    @EventHandler
    public void loadComplete(FMLLoadCompleteEvent event)
    {	
    	RegistryTracker.startup = false;
		RegistryTracker.output();
		if(RegistryTracker.hasConflicts)
		{
			CrashReport crashreport = CrashReport.makeCrashReport(new RuntimeException("Id Conflicts have been detected! Reconfigure your modpack"), "Load Complete");
			crashreport.makeCategory("Load Complete");
			Minecraft.getMinecraft().displayCrashReport(Minecraft.getMinecraft().addGraphicsAndWorldToCrashReport(crashreport));
		}
    }
}

package com.jredfox.confighelper;

import org.apache.logging.log4j.core.appender.SyslogAppender;

import com.evilnotch.lib.util.JavaUtil;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityList;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = ConfigHelperMod.MODID, version = ConfigHelperMod.VERSION, name = ConfigHelperMod.NAME)
public class ConfigHelperMod
{
    public static final String MODID = "confighelper";
    public static final String VERSION = "beta-1.0";
    public static final String NAME = "Config Helper";
    
    @EventHandler
    public void preinit(FMLPreInitializationEvent event)
    {
    	MinecraftForge.EVENT_BUS.register(new DatawatcherEvent());
    	Registries.registerBiome(BiomeGenBase.biomeList[161], 161, true);//fix vanilla
    	Enchantment e = Enchantment.aquaAffinity;//force Load vanilla Enchantment
    }
    
    /**
     * once the game has completely initialized output suggested ids for all mods
     */
    @EventHandler
    public void loadComplete(FMLLoadCompleteEvent event)
    {	
    	Registries.startup = false;
		
		if(Registries.hasConflicts)
		{
			Registries.output();
			Registries.makeCrashReport("Load Complete", "Id Conflicts have been detected! Reconfigure your modpack");
		}
		else if(RegistryConfig.configMode)
		{
			long time = System.currentTimeMillis();
			Registries.output();
			JavaUtil.printTime(time, "Done Outputing files: ");
		}
    }
}

package com.jredfox.confighelper;

import com.evilnotch.lib.util.JavaUtil;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = ModReference.MODID, version = ModReference.VERSION, name = ModReference.NAME)
public class ConfigHelperMod 
{
	
    @EventHandler
    public void preinit(FMLPreInitializationEvent event)
    {	
		MinecraftForge.EVENT_BUS.register(new DatawatcherEvent());
    	Registries.registerBiome(BiomeGenBase.getBiomeGenArray()[161], 161, true);//fix vanilla
    	Enchantment e = Enchantment.aquaAffinity;//force Load vanilla Enchantment
    	DimensionManager.init();
    	new DataWatcher(new EntityCreeper(null));
    }
    
    /**
     * once the game has completely initialized output suggested ids for all mods
     */
    @EventHandler
    public void loadComplete(FMLLoadCompleteEvent event)
    {	 		
    	PatchedClassLoader.checkClassLoader(this.getClass().getClassLoader());
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

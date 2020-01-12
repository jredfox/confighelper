package com.jredfox.confighelper;

import java.io.File;

import com.evilnotch.lib.JavaUtil;
import com.evilnotch.lib.simple.DummyMap;
import com.evilnotch.lib.simple.SimpleConfig;
import com.evilnotch.mod.PatchedClassLoader;
import com.jredfox.confighelper.datawatcher.WatcherDataType;
import com.jredfox.confighelper.event.WatcherEvent;
import com.jredfox.confighelper.reg.Registries;
import com.jredfox.confighelper.reg.Registry;
import com.jredfox.confighelper.reg.RegistryWriter;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = ModReference.MODID, version = ModReference.VERSION, name = ModReference.NAME)
public class ConfigHelperMod 
{
	
    @EventHandler
    public void preinit(FMLPreInitializationEvent event)
    {	
		Registries.initCentralReg();
		MinecraftForge.EVENT_BUS.register(new WatcherEvent());
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
    	Registries.freeze();
		Registries.biomes.securityCheck();
		Registries.potions.securityCheck();
		Registries.enchantments.securityCheck();
    	Registries.nextDimFrozen = Registries.nextDim;
    	Registries.loading = false;
    	Registries.strictRegs();
		if(Registries.hasConflicts())
		{
			Registries.write();
			Registries.makeCrashReport("Load Complete", "Id Conflicts have been detected! Reconfigure your modpack:" + Registries.getConflictTypes());
		}
		else if(RegistryConfig.configMode)
		{
			long time = System.currentTimeMillis();
			Registries.write();
			JavaUtil.printTime(time, "Done Outputing files: ");
		}
    }
}

package com.jredfox.confighelper;

import com.evilnotch.lib.util.JavaUtil;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.potion.Potion;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = ModReference.MODID, version = ModReference.VERSION, name = ModReference.NAME)
public class ConfigHelperMod 
{
	
    @EventHandler
    public void preinit(FMLPreInitializationEvent event)
    {	
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
    	Registries.nextDimFrozen = Registries.nextDim;
    	Registries.loading = false;
    	Registries.strictRegs();
		if(Registries.hasConflicts())
		{
			Registries.output();
			Registries.makeCrashReport("Load Complete", "Id Conflicts have been detected! Reconfigure your modpack:" + getConflicts());
		}
		else if(RegistryConfig.configMode)
		{
			long time = System.currentTimeMillis();
			Registries.output();
			JavaUtil.printTime(time, "Done Outputing files: ");
		}
    }

	private String getConflicts() 
	{
		StringBuilder b = new StringBuilder();
		b.append('[');
		if(Registries.biomes.hasConflicts)
			b.append(Registries.biomes.dataType + ", ");
		if(Registries.potions.hasConflicts)
			b.append(Registries.potions.dataType + ", ");
		if(Registries.enchantments.hasConflicts)
			b.append(Registries.enchantments.dataType + ", ");
		if(Registries.dimensions.hasConflicts)
			b.append(Registries.dimensions.dataType + ", ");
		if(Registries.providers.hasConflicts)
			b.append(Registries.providers.dataType + ", ");
		if(Registries.entities.hasConflicts)
			b.append(Registries.entities.dataType + ", ");
		if(Registries.datawatchers != null && Registries.entities.hasConflicts)
			b.append(Registries.datawatchers.dataType + ", ");
		String c = b.toString();
		return c.substring(0, c.length()-2) + "]";
	}
}

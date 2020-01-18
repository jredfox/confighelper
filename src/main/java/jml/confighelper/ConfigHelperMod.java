package jml.confighelper;

import java.lang.annotation.Annotation;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import jml.confighelper.event.WatcherEvent;
import jml.confighelper.reg.Registries;
import jml.confighelper.reg.Registry;
import jml.confighelper.reg.Registry.DataType;
import jml.evilnotch.lib.JavaUtil;
import jml.evilnotch.lib.Validate;
import jml.evilnotch.lib.asm.PatchedClassLoader;
import jml.evilnotch.lib.reflect.ReflectionHandler;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.potion.Potion;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;

@Mod(modid = ModReference.MODID, version = ModReference.VERSION, name = ModReference.NAME, dependencies = "required-after:evilnotchlib")
public class ConfigHelperMod 
{
	
    @EventHandler
    public void preinit(FMLPreInitializationEvent event)
    {	
		Registries.preinit();
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
    	Registries.loadComplete();
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

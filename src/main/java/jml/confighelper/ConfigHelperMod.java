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
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;

@Mod(modid = ModReference.MODID, version = ModReference.VERSION, name = ModReference.NAME)
public class ConfigHelperMod 
{
	
    @EventHandler
    public void preinit(FMLPreInitializationEvent event)
    {	
    	testReflect();
		Registries.preinit();
		MinecraftForge.EVENT_BUS.register(new WatcherEvent());
    	Registries.registerBiome(BiomeGenBase.getBiomeGenArray()[161], 161, true);//fix vanilla
    	Enchantment e = Enchantment.aquaAffinity;//force Load vanilla Enchantment
    	DimensionManager.init();
    	new DataWatcher(new EntityCreeper(null));
    }
    
    private static void testReflect()
    {
    	Class c = ReflectionHandler.getClass("jml.confighelper.ConfigHelperMod");
    	Class c2 = ReflectionHandler.getClass("jml.evilnotch.lib.reflect.MCPSidedString", true, ReflectionHandler.getClassLoader(ConfigHelperMod.class));
    	Annotation an = ReflectionHandler.getClassAnnotation(ConfigHelperMod.class, Mod.class);
    	boolean containsIntf = ReflectionHandler.containsInterface(EntityCreeper.class, IMob.class);
    	Enum e = ReflectionHandler.getEnum(Registry.DataType.class, "BIOME");
    	boolean inft = ReflectionHandler.containsInterface(IMob.class, IAnimals.class);
    	boolean containsEnum = ReflectionHandler.containsEnum(Registry.DataType.class, "BIOME");
    	Validate.nonNull(c);
    	Validate.nonNull(c2);
    	Validate.nonNull(an);
    	Validate.isTrue(containsIntf);
    	Validate.nonNull(e);
    	Validate.isTrue(e == Registry.DataType.BIOME);
    	Validate.isTrue(inft);
    	Validate.isTrue(containsEnum);
    	
    	//add enum test
    	ToolMaterial type = (ToolMaterial) ReflectionHandler.addEnum(ToolMaterial.class, "CONFIGHELPERTEST", 0, 0, 0f, 0f, 0);
    	Validate.nonNull(type);
    	Validate.isTrue(ReflectionHandler.containsEnum(ToolMaterial.class, "CONFIGHELPERTEST"));
	}

	/**
     * once the game has completely initialized output suggested ids for all mods
     */
    @EventHandler
    public void loadComplete(FMLLoadCompleteEvent event)
    {
    	PatchedClassLoader.checkClassLoader(this.getClass().getClassLoader());
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

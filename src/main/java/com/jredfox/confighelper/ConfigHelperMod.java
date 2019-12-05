package com.jredfox.confighelper;

import java.io.File;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.potion.Potion;

@Mod(modid = ConfigHelperMod.MODID, version = ConfigHelperMod.VERSION, name = ConfigHelperMod.NAME)
public class ConfigHelperMod
{
    public static final String MODID = "confighelper";
    public static final String VERSION = "1.0";
    public static final String NAME = "Config Helper";
    
    static
    {
    	//extend potion ids to byte and save any currently stored potions in here
    	if(Potion.potionTypes.length < 256)
    	{
    		System.out.println("Extending Potion limit to byte[0-255]");
    		Potion[] potions = new Potion[256];
    		for(int i=0;i<Potion.potionTypes.length;i++)
    		{
    			potions[i] = Potion.potionTypes[i];
    		}
    		Potion.potionTypes = potions;
    	}
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
			crashreport.makeCategory("LoadComplete");
			Minecraft.getMinecraft().displayCrashReport(Minecraft.getMinecraft().addGraphicsAndWorldToCrashReport(crashreport));
		}
    }
}

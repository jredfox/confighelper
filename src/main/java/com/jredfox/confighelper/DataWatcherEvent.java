package com.jredfox.confighelper;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;

public class DataWatcherEvent {
	
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void checkReg(EntityConstructing event)
	{
		if(event.entity instanceof EntityPlayer)
		{
			if(RegistryTracker.hasConflicts)
			{
				RegistryTracker.output();
				CrashReport crashreport = CrashReport.makeCrashReport(new RuntimeException("DataWatcher Id Conflicts have been detected! Reconfigure your modpack"), "Load Complete");
				crashreport.makeCategory("Load Complete");
				Minecraft.getMinecraft().displayCrashReport(Minecraft.getMinecraft().addGraphicsAndWorldToCrashReport(crashreport));
			}
			else if(!event.entity.worldObj.isRemote && RegistryConfig.autoConfig)
			{
				CrashReport crashreport = CrashReport.makeCrashReport(new RuntimeException("Ids cannot be automatic for in game purposes! Disable autoConfig after configuring your modpack"), "Load Complete");
				crashreport.makeCategory("Load Complete");
				Minecraft.getMinecraft().displayCrashReport(Minecraft.getMinecraft().addGraphicsAndWorldToCrashReport(crashreport));
			}
		}
	}
}

package com.jredfox.confighelper;

import com.evilnotch.lib.util.JavaUtil;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;

public class DatawatcherEvent {
	
	private static boolean outputted;
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void checkReg(EntityConstructing event)
	{
		if(event.entity instanceof EntityPlayer)
		{
			Registries.datawatchers.strict = true;//make sure it crashes after entity.init if a new data watcher id gets registered on the fly
			if(Registries.hasConflicts)
			{
				Registries.outputWatcher();
				Registries.makeCrashReport("Load Complete", "DataWatcher Id Conflicts have been detected! Reconfigure your modpack");
			}
			else if(RegistryConfig.configMode && !outputted)
			{
				outputted = true;
				long time = System.currentTimeMillis();
				Registries.outputWatcher();
				JavaUtil.printTime(time, "Done Outputing datawatchers: ");
			}
		}
	}
}

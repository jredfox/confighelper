package jml.confighelper.event;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import jml.confighelper.RegistryConfig;
import jml.confighelper.reg.Registries;
import jml.evilnotch.lib.JavaUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;

public class WatcherEvent {
	
	private static boolean outputted;
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void checkReg(EntityConstructing event)
	{
		if(event.entity instanceof EntityPlayer)
		{
			Registries.strictWatcher();//make sure it crashes after entity.init if a new data watcher id gets registered on the fly
			if(Registries.hasWatcherConflicts())
			{
				Registries.writeWatcher();
				Registries.makeCrashReport("Constructing DataWatcher", "DataWatcher Id Conflicts have been detected! Reconfigure your modpack");
			}
			else if(RegistryConfig.configMode && !outputted)
			{
				outputted = true;
				long time = System.currentTimeMillis();
				Registries.writeWatcher();
				JavaUtil.printTime(time, "Done Outputing datawatchers: ");
			}
		}
	}
}

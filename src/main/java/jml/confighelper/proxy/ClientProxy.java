package jml.confighelper.proxy;

import jml.confighelper.reg.Registries;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;

public class ClientProxy {

	public static void makeCrashReport(String cat, String msg) 
	{
		CrashReport crashreport = CrashReport.makeCrashReport(new RuntimeException(msg), cat);
		crashreport.makeCategory(cat);
		Minecraft.getMinecraft().theWorld = null;
		Minecraft.getMinecraft().displayCrashReport(Minecraft.getMinecraft().addGraphicsAndWorldToCrashReport(crashreport));
	}

}

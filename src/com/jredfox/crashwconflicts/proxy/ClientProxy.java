package com.jredfox.crashwconflicts.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;

public class ClientProxy extends Proxy{
	
	@Override
	public void displayCrash(CrashReport c)
	{
		Minecraft.getMinecraft().displayCrashReport(c);
	}

}

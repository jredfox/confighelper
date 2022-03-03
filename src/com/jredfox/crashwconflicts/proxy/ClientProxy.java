package com.jredfox.crashwconflicts.proxy;

import com.jredfox.crashwconflicts.CrashWConflicts;

import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;

public class ClientProxy extends Proxy{
	
	@Override
	public void displayCrash(CrashReport c)
	{
		if(CrashWConflicts.isCrashing)
			return;
		Minecraft.getMinecraft().displayCrashReport(c);
		CrashWConflicts.isCrashing = true;
		throw new ReportedException(c);
	}

}

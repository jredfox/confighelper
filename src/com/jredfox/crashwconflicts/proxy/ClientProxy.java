package com.jredfox.crashwconflicts.proxy;

import java.awt.Color;
import java.awt.Graphics;

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
		CrashWConflicts.isCrashing = true;
		Minecraft.getMinecraft().displayCrashReport(c);
		throw new ReportedException(c);
	}

}

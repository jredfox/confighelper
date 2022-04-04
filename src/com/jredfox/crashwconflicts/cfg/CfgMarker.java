package com.jredfox.crashwconflicts.cfg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

public class CfgMarker {

	public Map<Integer, Set<String>> marks = new HashMap();

	public void mark(int id, String modid)
	{
		this.marks.put(id, markId(id, modid));
	}

	protected Set<String> markId(int id, String modid) 
	{
		Set<String> ml = this.marks.get(id);
		if (ml == null)
		{
			ml = new HashSet();
			this.marks.put(id, ml);
		}
		ml.add(modid);
		return ml;
	}

	public boolean isMarked(int id)
	{
		return this.marks.containsKey(id);
	}

	public boolean isUnconfigured(int id)
	{
		return this.marks.containsKey(id) ? !this.marks.get(id).contains(getModId()) : true;
	}

	public static String getModId()
	{
		ModContainer mc = Loader.instance().activeModContainer();
		return mc != null ? mc.getModId() : Loader.instance().getMinecraftModContainer().getModId();
	}

}

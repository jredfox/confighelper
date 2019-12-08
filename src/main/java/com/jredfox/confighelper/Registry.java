package com.jredfox.confighelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityList;
import net.minecraft.potion.Potion;
import net.minecraft.world.biome.BiomeGenBase;

public class Registry {
	
	public Map<Integer,List<Registry.Entry>> reg = new LinkedHashMap<Integer,List<Registry.Entry>>();
	
	/**
	 * an automated integer
	 */
	public int id;
	public DataType dataType;
	/**
	 * turn this on to auto crash at the first sign of conflict
	 */
	public boolean strict;
	
	public Registry(DataType dataType)
	{
		this.dataType = dataType;
	}
	
	public int reg(Object obj, int id)
	{
		List<Entry> list = this.getEntry(id);
		if(list == null)
		{
			list = new ArrayList<Entry>();
			this.reg.put(id, list);
		}
		Entry entry = new Entry(getClass(obj), id);
		list.add(entry);
		if(list.size() > 1)
		{
			entry.newId = this.getFreeId(id);//if it's a duplicate id transform it into a newId
			RegistryTracker.hasConflicts = true;
			if(this.canCrash())
			{
				RegistryTracker.output();
				String inGame = !RegistryTracker.startup ? "In Game" : "Loading";
				CrashReport crashreport = CrashReport.makeCrashReport(new RuntimeException(this.dataType + " Id conflict during " +  inGame + " id:" + id + "=" + list.toString()), inGame);
				crashreport.makeCategory(inGame);
		        Minecraft.getMinecraft().displayCrashReport(Minecraft.getMinecraft().addGraphicsAndWorldToCrashReport(crashreport));
			}
		}
		return entry.newId;
	}
	
	/**
	 * returns whether or not it should crash on the first sign of conflict
	 */
	public boolean canCrash()
	{
		return !RegistryTracker.startup || this.strict;
	}

	/**
	 * grabs the next automatic id that works around vanilla ids usually
	 * warning this will automatically set your id counter and return the next free id
	 */
	protected int getFreeId(int org) 
	{
		if(this.dataType == DataType.ENTITY)
		{
			for(int i=this.id;i<EntityList.IDtoClassMapping.size();i++)
			{
				if(EntityList.getClassFromID(this.id) == null)
					return this.id;
				this.id++;
			}
		}
		Object[] arr = this.getStaticArr();
		for(int i=this.id;i<arr.length;i++)
		{
			if(arr[i] == null)
			{
				this.id = i;
				return this.id;
			}
		}
		return -1;
	}
	
	protected Object[] getStaticArr() 
	{
		if(this.dataType == DataType.BIOME)
			return BiomeGenBase.biomeList;
		if(this.dataType == DataType.POTION)
			return Potion.potionTypes;
		if(this.dataType == DataType.ENCHANTMENT)
			return Enchantment.enchantmentsList;
		return null;
	}
	
	/**
	 * not used in the root class of Registry
	 */
	public boolean containsId(int org)
	{
		if(this.dataType == DataType.ENTITY)
			return EntityList.IDtoClassMapping.containsKey(org);
		return this.getStaticArr()[org] != null;
	}
	
	public static Class getClass(Object entry)
	{
		if(entry instanceof Class)
			return (Class) entry;
		return entry.getClass();
	}
	
	public boolean isConflicting(int org)
	{
		return this.getEntry(org).size() > 1;
	}
	
	public List<Entry> getEntry(int org)
	{
		return this.reg.get(org);
	}
    
    public static enum DataType{
    	BIOME(),
    	ENCHANTMENT(),
    	POTION(),
    	DIMENSION(),
    	PROVIDER(),
    	ENTITY(),
    	DATAWATCHER(),
    	ITEM(),
    	BLOCK();
    }
    
    public static class Entry
    {
    	public int org;
    	public int newId;
    	public Class clazz;
    	public String name;
    	
    	public Entry(Class c, int org)
    	{
    		this.clazz = c;
    		this.org = org;
    		this.newId = org;
    	}
    	
    	public void setName(String str)
    	{
    			this.name = str;
    	}
    	
    	@Override
    	public String toString()
    	{
    		return "(name:" + this.name + ",newId:" + this.newId + ",class:" + this.clazz.getName() + ")";
    	}
    	
    	public String getDisplay()
    	{
    		return "(name:" + this.name + ", " + this.clazz.getName() + ")";
    	}
    	
    	@Override
    	public boolean equals(Object obj)
    	{
    		if(!(obj instanceof Entry))
    			return false;
    		return this.newId == ((Entry)obj).newId;
    	}
    	
    	@Override
    	public int hashCode()
    	{
    		return ((Integer)this.newId).hashCode();
    	}
    }
    
    @Override
    public String toString()
    {
    	return this.reg.toString();
    }
    
    public String getDisplay(int id)
    {
    	StringBuilder builder = new StringBuilder();
    	builder.append('[');
    	List<Entry> list = this.reg.get(id);
    	for(Entry e : list)
    	{
    		builder.append(e.getDisplay() + ", ");
    	}
    	builder.append(']');
    	return builder.toString();
    }

}

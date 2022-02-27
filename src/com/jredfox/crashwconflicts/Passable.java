package com.jredfox.crashwconflicts;

public class Passable {

	public int id;
	public Class<?> clazz;
	public String modid;
	
	public Passable(int id, Class<?> c, String mId)
	{
		this.id = id;
		this.clazz = c;
		this.modid = mId;
	}
	
	@Override
	public int hashCode()
	{
		return this.id;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(!(obj instanceof Passable))
			return false;
		Passable p = (Passable)obj;
		return this.id == p.id && this.clazz.equals(p.clazz) && this.modid.equals(p.modid);
	}
	
	@Override
	public String toString()
	{
		return this.id + ":" + this.clazz + ":" + this.modid;
	}
}

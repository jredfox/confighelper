package com.jredfox.crashwconflicts.auto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Script {
	
//	public Map<String, Variable> vars = new HashMap();
//	public List<Opperand> ops = new ArrayList();
	
	public Script()
	{
		
	}
	
	public Script(String str)
	{
		this.compile(str);
	}
	
	public void compile(String str)
	{
		
	}
	
	/**
	 * used to determine if the given string matches the script logic
	 */
	public boolean matches(String str)
	{
		return false;//TODO:
	}
	
}

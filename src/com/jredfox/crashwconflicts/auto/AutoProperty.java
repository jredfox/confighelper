package com.jredfox.crashwconflicts.auto;

import com.jredfox.crashwconflicts.AutoConfig;
import com.jredfox.util.RegTypes;

public class AutoProperty {
	
	public Script sf;
	public Script sc;
	public Script sp;
	public String dataType;
	public RegTypes regType;
	public int range;
	public int shift;
	
	public AutoProperty(String file, String cat, String property, String datatype, int range, int shift)
	{
		this(new Script(file), new Script(cat), new Script(property), datatype, range, shift);
	}
	
	public AutoProperty(Script file, Script cat, Script property, String dataType, int rangeCount, int shift)
	{
		assert rangeCount > 0 : "idCount must be > 0!";
		this.sf = file;
		this.sc = cat;
		this.sp = property;
		this.dataType = dataType;
		this.regType = AutoConfig.getRegType(dataType);
		this.range = rangeCount;
		this.shift = shift;
	}

}

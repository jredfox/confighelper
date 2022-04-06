package com.jredfox.crashwconflicts.auto;

import com.jredfox.crashwconflicts.AutoConfig;
import com.jredfox.util.IdChunk;
import com.jredfox.util.RegTypes;

public class DataTypeEntry
{
	public String type;
	public IdChunk range;
	public int index;
	public RegTypes regType;//vanilla data types only
	
	public DataTypeEntry(String type, IdChunk chunk, boolean max)
	{
		this.type = type;
		this.regType = AutoConfig.getRegType(type);
		this.range = chunk;
		this.index = max ? chunk.maxId : chunk.minId;
	}

	@Override
	public String toString()
	{
		return this.type + " index:" + this.index + " " + this.range;
	}
}

package com.jredfox.confighelper.reg;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * example of how the custom registry with a custom data type is suppose to work
 */
public abstract class RegistryCustom extends Registry{
	
	public RegistryCustom(int limit)
	{
		this(limit, new HashSet());
	}

	public RegistryCustom(int limit, Set<Integer> vanillaIds) 
	{
		super(DataType.CUSTOM);
		this.limit = limit;
		this.vanillaIds = vanillaIds;
	}
	
	@Override
	protected abstract String grabName(Registry.Entry entry);

}

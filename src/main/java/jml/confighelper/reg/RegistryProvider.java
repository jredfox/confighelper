package jml.confighelper.reg;

import java.util.HashSet;
import java.util.Set;

import jml.confighelper.RegistryConfig;
import jml.confighelper.reg.Registry.DataType;
import net.minecraftforge.common.DimensionManager;

public class RegistryProvider extends Registry{
	
	public RegistryProvider() 
	{
		super(DataType.PROVIDER);
	}

}

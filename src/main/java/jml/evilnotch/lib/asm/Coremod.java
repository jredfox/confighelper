package jml.evilnotch.lib.asm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

public interface Coremod extends IFMLLoadingPlugin{
	
	/**
	 * minimum sorting index for your mod to work or use IFMLLoadingPlugin.DependsOn("")
	 */
	public static final int sortingIndexMin = 1002;
	
	public void registerTransformers();

	@Override
	public default String[] getASMTransformerClass() {return null;}

	@Override
	public default String getModContainerClass() {return null;}

	@Override
	public default String getSetupClass() {return null;}

	@Override
	public default void injectData(Map<String, Object> data) {}

	@Override
	public default String getAccessTransformerClass() {return null;}
	
}

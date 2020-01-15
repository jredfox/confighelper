package jml.evilnotch.lib.asm;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;

import net.minecraft.util.ResourceLocation;

public interface ITransformer {
	
	/**
	 * the transformer name essentially but, in resource location
	 */
	public ResourceLocation id();
	/**
	 * do not return a new list every time used a cached one
	 * @return a list of classes in which your transformer will transform
	 */
	public List<String> getClasses();
	/**
	 * the transformer for your asm
	 */
	public void transform(String name, ClassNode node);
	
	/**
	 * return true if your transformer needs to run every class
	 */
	public default boolean isDynamic()
	{
		return false;
	}
	
	public default boolean canTransform(String name)
	{
		return this.isDynamic() || this.getClasses().contains(name);
	}

}
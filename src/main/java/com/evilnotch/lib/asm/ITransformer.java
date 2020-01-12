package com.evilnotch.lib.asm;

import java.util.List;

import org.objectweb.asm.tree.ClassNode;

import net.minecraft.util.ResourceLocation;

public interface ITransformer {
	
	/**
	 * the transformer name essentially but, in resource location
	 */
	public ResourceLocation id();
	/**
	 * return the list of classes you want to transform here
	 */
	public List<String> getClasses();
	public void transform(String actualName, ClassNode node);

}

package com.evilnotch.lib.asm;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.tree.ClassNode;

import net.minecraft.launchwrapper.Launch;

public class TransformsReg {
	
	public static List<ITransformer> transformers = new ArrayList(2);
	
	public static void registerTransformer(String transformerClass)
	{
		try
		{
			ITransformer transformer = (ITransformer) Launch.classLoader.loadClass(transformerClass).newInstance();
			transformers.add(transformer);
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
	}
	
	public static boolean canTransform(String name)
	{
		for(ITransformer t : transformers)
			if(t.isDynamic() || t.getClasses().contains(name))
				return true;
		return false;
	}
	
	public static void setClassNode(ClassNode newNode)
	{
		Transformer.node = newNode;
	}

}

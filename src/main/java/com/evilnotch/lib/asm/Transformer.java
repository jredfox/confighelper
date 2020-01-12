package com.evilnotch.lib.asm;

import org.objectweb.asm.tree.ClassNode;

import net.minecraft.launchwrapper.IClassTransformer;

public class Transformer implements IClassTransformer{

	public static ClassNode node = null;
	@Override
	public byte[] transform(String name, String actualName, byte[] bytes) 
	{
		if(TransformsReg.transformers.isEmpty())
		System.out.println("loading:" + actualName + ", " + TransformsReg.transformers.size());
		if(!TransformsReg.canTransform(actualName))
			return bytes;
		try
		{
			node = ASMHelper.getClassNode(bytes);
			for(ITransformer transformer : TransformsReg.transformers)
				transformer.transform(actualName, node);
			return ASMHelper.getClassWriter(node).toByteArray();
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
		return bytes;
	}

}

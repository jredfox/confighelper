package com.evilnotch.lib.asm;

import org.objectweb.asm.tree.ClassNode;

import net.minecraft.launchwrapper.IClassTransformer;

public class Transformer implements IClassTransformer{

	public static ClassNode node = null;
	@Override
	public byte[] transform(String oldName, String name, byte[] bytes) 
	{
		if(!TransformsReg.canTransform(name))
			return bytes;
		try
		{
			node = ASMHelper.getClassNode(bytes);
			for(ITransformer transformer : TransformsReg.transformers)
				transformer.transform(name, node);
			byte[] custom = ASMHelper.getClassWriter(node).toByteArray();
			ASMHelper.dumpFile(name, custom);
			return custom;
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
		return bytes;
	}

}

package jml.evilnotch.lib.asm;

import org.objectweb.asm.tree.ClassNode;

import net.minecraft.launchwrapper.IClassTransformer;

public class ASMLoader implements IClassTransformer{

	public static ClassNode node = null;
	@Override
	public byte[] transform(String oldName, String name, byte[] bytes) 
	{
		if(!TransformsReg.canTransform(name))
			return bytes;
		ITransformer last = null;
		try
		{
			node = ASMHelper.getClassNode(bytes);
			for(ITransformer transformer : TransformsReg.transformers)
			{
				last = transformer;
				transformer.transform(name, node);
			}
			byte[] custom = ASMHelper.getClassWriter(node).toByteArray();
			ASMHelper.dumpFile(name, custom);
			return custom;
		}
		catch(Throwable t)
		{
			System.out.print("Blamed Transformer:\t" + last.id() + "\nLoaded Transformers:" + TransformsReg.printIds() + "\n");
			t.printStackTrace();
		}
		return bytes;
	}

}

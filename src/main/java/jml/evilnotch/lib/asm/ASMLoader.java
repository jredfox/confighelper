package jml.evilnotch.lib.asm;

import org.objectweb.asm.tree.ClassNode;

import net.minecraft.launchwrapper.IClassTransformer;

public class ASMLoader implements IClassTransformer{

	@Override
	public byte[] transform(String oldName, String name, byte[] bytes) 
	{
		if(bytes == null)
			return bytes;//do not parse null classes
		
		ClassNode node = null;
		ITransformer last = null;
		try
		{
			for(ITransformer transformer : TransformsReg.transformers)
			{
				if(!transformer.canTransform(name))
					continue;
				if(node == null)
					node = ASMHelper.getClassNode(bytes);
				last = transformer;
				transformer.transform(name, node);
			}
			if(last == null)
				return bytes;
			
			byte[] custom = ASMHelper.getClassWriter(node).toByteArray();
			if(ObfHelper.isDeob)
				ASMHelper.dumpFile(name, custom);
			return custom;
		}
		catch(Throwable t)
		{
			t.printStackTrace();
			System.out.print("Blamed Transformer:\t" + (last != null ? last.id() : null) + "\nLoaded Transformers:" + TransformsReg.printIds() + "\n");
		}
		return bytes;
	}

}

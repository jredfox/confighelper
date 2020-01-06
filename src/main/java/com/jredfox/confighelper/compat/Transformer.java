package com.jredfox.confighelper.compat;

import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;

import com.evilnotch.lib.asm.ASMHelper;
import com.jredfox.confighelper.reg.RegistryIds;

import net.minecraft.launchwrapper.IClassTransformer;

public class Transformer implements IClassTransformer{
	
	public static List<String> clazzes = RegistryIds.asList(new String[]{"com.shinoow.abyssalcraft.AbyssalCraft"});

	@Override
	public byte[] transform(String name, String actualName, byte[] bytes) 
	{
		try
		{
			if(clazzes.contains(actualName))
			{
				ClassNode classNode = ASMHelper.getClassNode(bytes);
				patchAbyssalcraft(classNode);
				return ASMHelper.getClassWriter(classNode).toByteArray();
			}
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
		return bytes;
	}
	
	private static void patchAbyssalcraft(ClassNode classNode) 
	{
		MethodNode method = ASMHelper.getMethodNode(classNode, "checkBiomeIds", "(Z)V");
		LineNumberNode line = ASMHelper.getFirstInstruction(method);
		method.instructions.clear();
		LabelNode label = new LabelNode();
		method.instructions.add(label);
		method.instructions.add(new LineNumberNode(line.line, label));
		method.instructions.add(new InsnNode(Opcodes.RETURN));
	}
	

}

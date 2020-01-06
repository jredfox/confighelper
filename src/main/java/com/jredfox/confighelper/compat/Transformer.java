package com.jredfox.confighelper.compat;

import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import com.evilnotch.lib.asm.ASMHelper;
import com.jredfox.confighelper.reg.RegistryIds;

import net.minecraft.launchwrapper.IClassTransformer;

public class Transformer implements IClassTransformer{
	
	public static List<String> clazzes = RegistryIds.asList(
	new String[]
	{
		"com.shinoow.abyssalcraft.AbyssalCraft",
		"erebus.ModBiomes"
	});

	@Override
	public byte[] transform(String name, String actualName, byte[] bytes) 
	{
		try
		{
			int index = clazzes.indexOf(actualName);
			if(index != -1)
			{
				System.out.println("config helper compat patching:" + actualName);
				ClassNode classNode = ASMHelper.getClassNode(bytes);
				switch(index)
				{
					case 0:
						patchAbyssalcraft(classNode);
					break;
					
					case 1:
						patchErebus(classNode);
					break;
				}
				byte[] custom = ASMHelper.getClassWriter(classNode).toByteArray();
				ASMHelper.dumpFile(actualName, custom);
				return custom;
			}
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
		return bytes;
	}
	
	private void patchErebus(ClassNode classNode) 
	{
		MethodNode init = ASMHelper.getMethodNode(classNode, "init", "()V");
		ASMHelper.clearNextThrowable(init, "java/lang/IllegalArgumentException");
		ASMHelper.clearNextThrowable(init, "java/lang/IllegalArgumentException");
		
		MethodNode post = ASMHelper.getMethodNode(classNode, "postInit", "()V");
		ASMHelper.clearVoidMethod(post);
	}

	private static void patchAbyssalcraft(ClassNode classNode) 
	{
		MethodNode method = ASMHelper.getMethodNode(classNode, "checkBiomeIds", "(Z)V");
		ASMHelper.clearVoidMethod(method);
	}

}

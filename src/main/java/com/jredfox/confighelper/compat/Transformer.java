package com.jredfox.confighelper.compat;

import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import com.evilnotch.lib.asm.ASMHelper;
import com.evilnotch.lib.asm.ITransformer;
import com.jredfox.confighelper.reg.RegistryIds;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.util.ResourceLocation;

public class Transformer implements ITransformer{
	
	public static List<String> clazzes = RegistryIds.asList(
	new String[]
	{
		"com.shinoow.abyssalcraft.AbyssalCraft",
		"erebus.ModBiomes"
	});
	
	@Override
	public ResourceLocation id() 
	{
		return new ResourceLocation("confighelper:compat");
	}

	@Override
	public List<String> getClasses() 
	{
		return clazzes;
	}

	@Override
	public void transform(String name, ClassNode node) 
	{
		int index = clazzes.indexOf(name);
		if(index != -1)
		{
			System.out.println("config helper compat patching:" + node.name);
			switch(index)
			{
				case 0:
					patchAbyssalcraft(node);
				break;
				
				case 1:
					patchErebus(node);
				break;
			}
		}
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

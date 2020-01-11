package com.jredfox.confighelper.asm;

import org.apache.logging.log4j.core.appender.SyslogAppender;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.evilnotch.lib.asm.ASMHelper;
import com.evilnotch.lib.reflect.MCPSidedString;

public class DataWatcherPatcher {
	
	public static void patchConstructor(ClassNode classNode)
	{	
		MethodNode construct = ASMHelper.getConstructionNode(classNode, "(Lnet/minecraft/entity/Entity;)V");
		InsnList list0 = new InsnList();
		list0.add(new VarInsnNode(Opcodes.ALOAD, 0));
		list0.add(new VarInsnNode(Opcodes.ALOAD, 1));
		list0.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/jredfox/confighelper/reg/Registries", "createWatcherReg", "(Lnet/minecraft/entity/Entity;)Lcom/jredfox/confighelper/reg/Registry;", false));
		list0.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/entity/DataWatcher", "reg", "Lcom/jredfox/confighelper/reg/Registry;"));
		construct.instructions.insert(ASMHelper.getLastPutField(construct), list0);
	}

	public static void patchAddObject(ClassNode classNode) 
	{
		MethodNode addObject = ASMHelper.getMethodNode(classNode, new MCPSidedString("addObject", "func_75682_a").toString(), "(ILjava/lang/Object;)V");
		
		//delete line Integer integer = (Integer) dataTypes.get(obj.getClass());
		AbstractInsnNode start = ASMHelper.getFieldInsnNode(addObject, new FieldInsnNode(Opcodes.GETSTATIC, "net/minecraft/entity/DataWatcher", new MCPSidedString("dataTypes", "field_75697_a").toString(), "Ljava/util/HashMap;"));
		if(start != null)
		{
			AbstractInsnNode end = ASMHelper.nextInsn(start, Opcodes.ASTORE);
			ASMHelper.removeInsn(addObject, start, end);
		}
		else
			System.out.println("confighelper asm DataWatcher#addObject cannot delete line: \"Integer integer = (Integer) dataTypes.get(obj.getClass());\"");
		
		//inject line: Integer integer = Registries.getWatcherTypeId(obj.getClass());
		LineNumberNode line = ASMHelper.getFirstInstruction(addObject);
		InsnList list0 = new InsnList();
		list0.add(new VarInsnNode(Opcodes.ALOAD, 2));
		list0.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false));
		list0.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/jredfox/confighelper/reg/Registries", "getWatcherTypeId", "(Ljava/lang/Class;)Ljava/lang/Integer;", false));
		list0.add(new VarInsnNode(Opcodes.ASTORE, 3));
		addObject.instructions.insert(line, list0);
		
		InsnList list = new InsnList();
		//inject line: id = Registries.registerDataWatcher(this.field_151511_a, id, this.reg);
		list.add(new VarInsnNode(Opcodes.ALOAD, 0));
		list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/entity/DataWatcher", new MCPSidedString("field_151511_a", "field_151511_a").toString(), "Lnet/minecraft/entity/Entity;"));
		list.add(new VarInsnNode(Opcodes.ILOAD, 1));
		list.add(new VarInsnNode(Opcodes.ALOAD, 0));
		list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/entity/DataWatcher", "reg", "Lcom/jredfox/confighelper/reg/Registry;"));
		list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/jredfox/confighelper/reg/Registries", "registerDataWatcher", "(Lnet/minecraft/entity/Entity;ILcom/jredfox/confighelper/reg/Registry;)I", false));
		list.add(new VarInsnNode(Opcodes.ISTORE, 1));
		addObject.instructions.insert(line, list);
		
		//disable throwable if the id > 31
		JumpInsnNode todisable = null;
		IntInsnNode push = null;
		for(AbstractInsnNode ab : addObject.instructions.toArray())
		{
			if(ab.getOpcode() == Opcodes.BIPUSH)
			{
				push = (IntInsnNode)ab;
				todisable = ASMHelper.nextJumpInsnNode(push);
				break;
			}
		}
		InsnList append = new InsnList();
		append.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/evilnotch/lib/JavaUtil", "returnFalse", "()Z", false));
		append.add(new JumpInsnNode(Opcodes.IFEQ, todisable.label));
		addObject.instructions.insertBefore(push.getPrevious(), append);
	}
	
	public static void patchWriteList(ClassNode classNode)
	{
		MethodNode write1 = ASMHelper.getMethodNode(classNode, new MCPSidedString("func_151509_a", "func_151509_a").toString(), "(Lnet/minecraft/network/PacketBuffer;)V");
		DataWatcherPatcher.patch127(write1);
		MethodNode write2 = ASMHelper.getMethodNode(classNode, new MCPSidedString("writeWatchedListToPacketBuffer", "func_151507_a").toString(), "(Ljava/util/List;Lnet/minecraft/network/PacketBuffer;)V");
		DataWatcherPatcher.patch127(write2);
	}
	
	/**
	 * patch the writing of the packet to end in 255 instead of 127
	 */
	public static void patch127(MethodNode node) 
	{
		AbstractInsnNode[] abs = node.instructions.toArray();
		for(int i = abs.length-1; i >= 0; i--)
		{
			AbstractInsnNode ab = abs[i];
			if(Opcodes.BIPUSH == ab.getOpcode())
			{
				IntInsnNode k = (IntInsnNode)ab;
				if(k.operand == 127)
				{
					k.setOpcode(Opcodes.SIPUSH);
					k.operand = 255;
				}
				break;
			}
		}
	}

}

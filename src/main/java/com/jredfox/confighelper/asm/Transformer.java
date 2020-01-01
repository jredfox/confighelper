package com.jredfox.confighelper.asm;

import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.evilnotch.lib.asm.ASMHelper;
import com.evilnotch.lib.asm.ObfHelper;
import com.jredfox.confighelper.ConfigHelperMod;
import com.jredfox.confighelper.PatchedClassLoader;
import com.jredfox.confighelper.ModReference;
import com.jredfox.confighelper.RegistryIds;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;

public class Transformer implements IClassTransformer{
	
	static
	{
		PatchedClassLoader.stopMemoryOverflow(Launch.classLoader);
	}

	
	public static final List<String> clazzes = RegistryIds.asList(new String[]
	{
		"net.minecraft.world.biome.BiomeGenBase",
		"net.minecraft.potion.Potion",
		"net.minecraft.enchantment.Enchantment",
		"net.minecraftforge.common.DimensionManager",
		"net.minecraft.entity.EntityList",
		"net.minecraft.entity.DataWatcher"
	});
	
	@Override
	public byte[] transform(String oldName, String actualName, byte[] bytes)
	{
		try
		{
		if(clazzes.contains(actualName))
		{
			ClassNode classNode = ASMHelper.getClassNode(bytes);
			int index = clazzes.indexOf(actualName);
			switch(index)
			{
				case 0:
					patchBiome(classNode);
				break;
				
				case 1:
					patchPotion(classNode);
				break;
				
				case 2:
					patchEnchantment(classNode);
				break;
				
				case 3:
					patchForgeDimensions(classNode);
				break;
				
				case 4:
					patchEntityList(classNode);
				break;
				
				case 5:
					patchDatawatcher(classNode);
				break;
			}
			byte[] custom = ASMHelper.getClassWriter(classNode).toByteArray();
			if(index == 5)
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

	private void patchDatawatcher(ClassNode classNode) 
	{
		ASMHelper.addFeild(classNode, "reg", "Lcom/jredfox/confighelper/Registry;");
		MethodNode construct = ASMHelper.getConstructionNode(classNode, "(Lnet/minecraft/entity/Entity;)V");
		InsnList list0 = new InsnList();
		list0.add(new VarInsnNode(Opcodes.ALOAD, 0));
		list0.add(new VarInsnNode(Opcodes.ALOAD, 1));
		list0.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/jredfox/confighelper/Registries", "createWatcherReg", "(Lnet/minecraft/entity/Entity;)Lcom/jredfox/confighelper/Registry;", false));
		list0.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/entity/DataWatcher", "reg", "Lcom/jredfox/confighelper/Registry;"));
		construct.instructions.insert(ASMHelper.getLastPutField(construct), list0);
		
		MethodNode addObject = ASMHelper.getMethodNode(classNode, "addObject", "(ILjava/lang/Object;)V");
		InsnList list = new InsnList();
		//inject line: id = Registries.registerDataWatcher(this.field_151511_a, id, this.reg);
		list.add(new VarInsnNode(Opcodes.ALOAD, 0));
		list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/entity/DataWatcher", "field_151511_a", "Lnet/minecraft/entity/Entity;"));
		list.add(new VarInsnNode(Opcodes.ILOAD, 1));
		list.add(new VarInsnNode(Opcodes.ALOAD, 0));
		list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/entity/DataWatcher", "reg", "Lcom/jredfox/confighelper/Registry;"));
		list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/jredfox/confighelper/Registries", "registerDataWatcher", "(Lnet/minecraft/entity/Entity;ILcom/jredfox/confighelper/Registry;)I", false));
		list.add(new VarInsnNode(Opcodes.ISTORE, 1));
		addObject.instructions.insert(ASMHelper.getFirstInstruction(addObject), list);
		
		//disable throwable if the id > 31
		JumpInsnNode todisable = null;
		IntInsnNode push = null;
		for(AbstractInsnNode ab : addObject.instructions.toArray())
		{
			if(ab.getOpcode() == Opcodes.BIPUSH)
			{
				push = (IntInsnNode)ab;
				todisable = ASMHelper.getJumpInsnNode(push);
				break;
			}
		}
		InsnList append = new InsnList();
		append.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/evilnotch/lib/util/JavaUtil", "returnFalse", "()Z", false));
		append.add(new JumpInsnNode(Opcodes.IFEQ, todisable.label));
		addObject.instructions.insertBefore(push.getPrevious(), append);
		
		
		String input = ASMHelper.getInputStream(ModReference.MODID, "DataWatcher"); //"assets/confighelper/asm/" + (ObfHelper.isObf ? "srg/" : "deob/") + "DataWatcher";
		//127 > 255
		ASMHelper.replaceMethod(classNode, input, "func_151509_a", "(Lnet/minecraft/network/PacketBuffer;)V");
		ASMHelper.replaceMethod(classNode, input, "writeWatchedListToPacketBuffer", "(Ljava/util/List;Lnet/minecraft/network/PacketBuffer;)V");
		//method edits
		ASMHelper.replaceMethod(classNode, input, "writeWatchableObjectToPacketBuffer", "(Lnet/minecraft/network/PacketBuffer;Lnet/minecraft/entity/DataWatcher$WatchableObject;)V");
		ASMHelper.replaceMethod(classNode, input, "readWatchedListFromPacketBuffer", "(Lnet/minecraft/network/PacketBuffer;)Ljava/util/List;");
	}

	/**
	 * injects line id = Registries.registerBiome(this, id, boolean register);
	 */
	private static void patchBiome(ClassNode classNode) 
	{
		MethodNode node = ASMHelper.getConstructionNode(classNode, "(IZ)V");
		InsnList list = new InsnList();
		list.add(new VarInsnNode(Opcodes.ALOAD, 0));
		list.add(new VarInsnNode(Opcodes.ILOAD, 1));
		list.add(new VarInsnNode(Opcodes.ILOAD, 2));
		list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/jredfox/confighelper/Registries", "registerBiome", "(Lnet/minecraft/world/biome/BiomeGenBase;IZ)I", false));
		list.add(new VarInsnNode(Opcodes.ISTORE, 1));
		FieldInsnNode field = new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/world/biome/BiomeGenBase", "topBlock", "Lnet/minecraft/block/Block;");
		node.instructions.insert(ASMHelper.getFirstInstruction(node, Opcodes.INVOKESPECIAL), list);
	}
	
	/**
	 * inject line Registries.registerPotion(this, id)
	 */
	private static void patchPotion(ClassNode classNode) 
	{
		MethodNode constructor = ASMHelper.getConstructionNode(classNode, "(IZI)V");
		InsnList list = new InsnList();
		list.add(new VarInsnNode(Opcodes.ALOAD, 0));
		list.add(new VarInsnNode(Opcodes.ILOAD, 1));
		list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/jredfox/confighelper/Registries", "registerPotion", "(Lnet/minecraft/potion/Potion;I)I", false));
		list.add(new VarInsnNode(Opcodes.ISTORE, 1));
		constructor.instructions.insert(ASMHelper.getFirstInstruction(constructor, Opcodes.INVOKESPECIAL), list);
	}
	
	/**
	 * inject line: Registries.registerEnchantment(this, id)
	 */
	private void patchEnchantment(ClassNode classNode) 
	{
		MethodNode node = ASMHelper.getConstructionNode(classNode, "(IILnet/minecraft/enchantment/EnumEnchantmentType;)V");
		InsnList list = new InsnList();
		list.add(new VarInsnNode(Opcodes.ALOAD, 0));
		list.add(new VarInsnNode(Opcodes.ILOAD, 1));
		list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/jredfox/confighelper/Registries", "registerEnchantment", "(Lnet/minecraft/enchantment/Enchantment;I)I", false));
		list.add(new VarInsnNode(Opcodes.ISTORE, 1));
		node.instructions.insert(ASMHelper.getFirstInstruction(node, Opcodes.INVOKESPECIAL), list);
	}
	
	private static void patchForgeDimensions(ClassNode classNode) 
	{
		//inject provider lines
		MethodNode provider = ASMHelper.getMethodNode(classNode, "registerProviderType", "(ILjava/lang/Class;Z)Z");
		//id = Registries.registerProvider(provider.class, id);
		InsnList list = new InsnList();
		list.add(new VarInsnNode(Opcodes.ALOAD, 1));
		list.add(new VarInsnNode(Opcodes.ILOAD, 0));
		list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/jredfox/confighelper/Registries", "registerProvider", "(Ljava/lang/Class;I)I", false));
		list.add(new VarInsnNode(Opcodes.ISTORE, 0));
		//keepLoaded = Registries.keepDimLoaded(id, keepLoaded);
		list.add(new VarInsnNode(Opcodes.ILOAD, 0));
		list.add(new VarInsnNode(Opcodes.ILOAD, 2));
		list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/jredfox/confighelper/Registries", "keepDimLoaded", "(IZ)Z", false));
		list.add(new VarInsnNode(Opcodes.ISTORE, 2));
		provider.instructions.insert(ASMHelper.getFirstInstruction(provider), list);
		
		//inject dimension lines
		MethodNode dimensions = ASMHelper.getMethodNode(classNode, "registerDimension", "(II)V");
		InsnList list2 = new InsnList();
		//id = Registries.registerDimension(id);
		list2.add(new VarInsnNode(Opcodes.ILOAD, 0));
		list2.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/jredfox/confighelper/Registries", "registerDimension", "(I)I", false));
		list2.add(new VarInsnNode(Opcodes.ISTORE, 0));
		//providerId = Registries.guessProviderId(providerId);
		list2.add(new VarInsnNode(Opcodes.ILOAD, 1));
		list2.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/jredfox/confighelper/Registries", "guessProviderId", "(I)I", false));
		list2.add(new VarInsnNode(Opcodes.ISTORE, 1));
		dimensions.instructions.insert(ASMHelper.getFirstInstruction(dimensions), list2);
		
		//replace DimensionManager nextId methods
		String input = "assets/confighelper/asm/deob/DimensionManager";
		ASMHelper.replaceMethod(classNode, input, "getNextFreeDimId", "()I");
		ASMHelper.replaceMethod(classNode, input, "saveDimensionDataMap", "()Lnet/minecraft/nbt/NBTTagCompound;");
		ASMHelper.replaceMethod(classNode, input, "loadDimensionDataMap", "(Lnet/minecraft/nbt/NBTTagCompound;)V");
	}
	
	private void patchEntityList(ClassNode classNode) 
	{
		//inject line: Registries.registerEntity(EntityClass.class, name, id)
		MethodNode node = ASMHelper.getMethodNode(classNode, "addMapping", "(Ljava/lang/Class;Ljava/lang/String;I)V");
		InsnList list = new InsnList();
		list.add(new VarInsnNode(Opcodes.ALOAD, 0));
		list.add(new VarInsnNode(Opcodes.ALOAD, 1));
		list.add(new VarInsnNode(Opcodes.ILOAD, 2));
		list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/jredfox/confighelper/Registries", "registerEntity", "(Ljava/lang/Class;Ljava/lang/String;I)I", false));
		list.add(new VarInsnNode(Opcodes.ISTORE, 2));
		node.instructions.insert(ASMHelper.getFirstInstruction(node), list);
		
		//inject line: id = (Integer) classToIDMapping.get(id);
		MethodNode egg = ASMHelper.getMethodNode(classNode, "addMapping", "(Ljava/lang/Class;Ljava/lang/String;III)V");
		InsnList list2 = new InsnList();
		list2.add(new FieldInsnNode(Opcodes.GETSTATIC, "net/minecraft/entity/EntityList", "classToIDMapping", "Ljava/util/Map;"));
		list2.add(new VarInsnNode(Opcodes.ALOAD, 0));
		list2.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", true));
		list2.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Integer"));
		list2.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false));
		list2.add(new VarInsnNode(Opcodes.ISTORE, 2));
		MethodInsnNode check = new MethodInsnNode(Opcodes.INVOKESTATIC, "net/minecraft/entity/EntityList", "addMapping", "(Ljava/lang/Class;Ljava/lang/String;I)V", false);
		egg.instructions.insert(ASMHelper.getFirstMethodInsn(egg, check), list2);
	}

}

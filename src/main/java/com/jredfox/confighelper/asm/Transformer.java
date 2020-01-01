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
import com.evilnotch.lib.reflect.MCPSidedString;
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
		FieldInsnNode field = new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/world/biome/BiomeGenBase", new MCPSidedString("topBlock", "field_76752_A").toString(), "Lnet/minecraft/block/Block;");
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
		String input = ASMHelper.getInputStream(ModReference.MODID, "DimensionManager");
		ASMHelper.replaceMethod(classNode, input, "getNextFreeDimId", "()I");
		ASMHelper.replaceMethod(classNode, input, "saveDimensionDataMap", "()Lnet/minecraft/nbt/NBTTagCompound;");
		ASMHelper.replaceMethod(classNode, input, "loadDimensionDataMap", "(Lnet/minecraft/nbt/NBTTagCompound;)V");
	}
	
	private void patchEntityList(ClassNode classNode) 
	{
		//inject line: Registries.registerEntity(EntityClass.class, name, id)
		MethodNode node = ASMHelper.getMethodNode(classNode, new MCPSidedString("addMapping", "func_75618_a").toString(), "(Ljava/lang/Class;Ljava/lang/String;I)V");
		InsnList list = new InsnList();
		list.add(new VarInsnNode(Opcodes.ALOAD, 0));
		list.add(new VarInsnNode(Opcodes.ALOAD, 1));
		list.add(new VarInsnNode(Opcodes.ILOAD, 2));
		list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/jredfox/confighelper/Registries", "registerEntity", "(Ljava/lang/Class;Ljava/lang/String;I)I", false));
		list.add(new VarInsnNode(Opcodes.ISTORE, 2));
		node.instructions.insert(ASMHelper.getFirstInstruction(node), list);
		
		//inject line: id = (Integer) classToIDMapping.get(id);
		MethodNode egg = ASMHelper.getMethodNode(classNode, new MCPSidedString("addMapping", "func_75614_a").toString(), "(Ljava/lang/Class;Ljava/lang/String;III)V");
		InsnList list2 = new InsnList();
		list2.add(new FieldInsnNode(Opcodes.GETSTATIC, "net/minecraft/entity/EntityList", new MCPSidedString("classToIDMapping", "field_75624_e").toString(), "Ljava/util/Map;"));
		list2.add(new VarInsnNode(Opcodes.ALOAD, 0));
		list2.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", true));
		list2.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Integer"));
		list2.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false));
		list2.add(new VarInsnNode(Opcodes.ISTORE, 2));
		MethodInsnNode check = new MethodInsnNode(Opcodes.INVOKESTATIC, "net/minecraft/entity/EntityList", new MCPSidedString("addMapping", "func_75618_a").toString(), "(Ljava/lang/Class;Ljava/lang/String;I)V", false);
		egg.instructions.insert(ASMHelper.getFirstMethodInsn(egg, check), list2);
	}
	
	private void patchDatawatcher(ClassNode classNode) 
	{
		ASMHelper.addFeild(classNode, "reg", "Lcom/jredfox/confighelper/Registry;");
		DataWatcherPatcher.patchConstructor(classNode);
		DataWatcherPatcher.patchAddObject(classNode);
		DataWatcherPatcher.patchWriteList(classNode);
		String input = ASMHelper.getInputStream(ModReference.MODID, "DataWatcher"); //"assets/confighelper/asm/" + (ObfHelper.isObf ? "srg/" : "deob/") + "DataWatcher";
		ASMHelper.replaceMethod(classNode, input, new MCPSidedString("writeWatchableObjectToPacketBuffer", "func_151510_a").toString(), "(Lnet/minecraft/network/PacketBuffer;Lnet/minecraft/entity/DataWatcher$WatchableObject;)V");
		ASMHelper.replaceMethod(classNode, input, new MCPSidedString("readWatchedListFromPacketBuffer", "func_151508_b").toString(), "(Lnet/minecraft/network/PacketBuffer;)Ljava/util/List;");
	}

}

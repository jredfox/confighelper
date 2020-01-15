package jml.confighelper.asm;

import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import jml.confighelper.ConfigHelperMod;
import jml.confighelper.ModReference;
import jml.confighelper.RegistryConfig;
import jml.confighelper.reg.RegistryIds;
import jml.evilnotch.lib.asm.ASMHelper;
import jml.evilnotch.lib.asm.ITransformer;
import jml.evilnotch.lib.asm.ObfHelper;
import jml.evilnotch.lib.asm.PatchedClassLoader;
import jml.evilnotch.lib.reflect.MCPSidedString;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.ResourceLocation;

public class RegTransformer implements ITransformer{
	
	public static final List<String> clazzes = RegistryIds.asList(new String[]
	{
		"net.minecraft.world.biome.BiomeGenBase",
		"net.minecraft.potion.Potion",
		"net.minecraft.enchantment.Enchantment",
		"net.minecraftforge.common.DimensionManager",
		"net.minecraft.entity.EntityList",
		"net.minecraft.entity.DataWatcher",
		"cpw.mods.fml.common.registry.EntityRegistry"
	});
	
	@Override
	public ResourceLocation id()
	{
		return new ResourceLocation("confighelper:registry");
	}

	@Override
	public List<String> getClasses() 
	{
		return clazzes;
	}
	
	@Override
	public void transform(String name, ClassNode classNode)
	{
		int index = clazzes.indexOf(name);
		if(index != -1)
		{
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
					patchForgeDims(classNode);
				break;
				
				case 4:
					patchEntityList(classNode);
				break;
				
				case 5:
					patchDatawatcher(classNode);
				break;
				
				case 6:
					patchForgeEntityReg(classNode);
				break;
			}
		}
	}

	private static void patchForgeEntityReg(ClassNode classNode) 
	{
		String input = ASMHelper.getInputStream(ModReference.MODID, "EntityRegistry");
		ASMHelper.replaceMethod(classNode, input, "<init>", "()V");
		ASMHelper.replaceMethod(classNode, input, "validateAndClaimId", "(I)I");
		ASMHelper.replaceMethod(classNode, input, "findGlobalUniqueEntityId", "()I");
		ASMHelper.replaceMethod(classNode, input, "registerGlobalEntityID", "(Ljava/lang/Class;Ljava/lang/String;III)V");
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
		list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "jml/confighelper/reg/Registries", "registerBiome", "(Lnet/minecraft/world/biome/BiomeGenBase;IZ)I", false));
		list.add(new VarInsnNode(Opcodes.ISTORE, 1));
		FieldInsnNode field = new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/world/biome/BiomeGenBase", new MCPSidedString("topBlock", "field_76752_A").toString(), "Lnet/minecraft/block/Block;");
		node.instructions.insert(ASMHelper.getFirstInstruction(node, Opcodes.INVOKESPECIAL), list);
		
		MethodNode clinit = ASMHelper.getClassInitNode(classNode);
		InsnList list2 = new InsnList();
		list2.add(new InsnNode(Opcodes.ICONST_1));
		list2.add(new FieldInsnNode(Opcodes.PUTSTATIC, "jml/confighelper/reg/Registries", "initBiomes", "Z"));
		clinit.instructions.insertBefore(ASMHelper.getLastInstruction(clinit, Opcodes.RETURN), list2);
	}
	
	/**
	 * inject line Registries.registerPotion(this, id)
	 */
	private static void patchPotion(ClassNode classNode) 
	{	
		//inject line: Registries.registerPotion(this, id);
		MethodNode constructor = ASMHelper.getConstructionNode(classNode, "(IZI)V");
		InsnList list = new InsnList();
		list.add(new VarInsnNode(Opcodes.ALOAD, 0));
		list.add(new VarInsnNode(Opcodes.ILOAD, 1));
		list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "jml/confighelper/reg/Registries", "registerPotion", "(Lnet/minecraft/potion/Potion;I)I", false));
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
		list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "jml/confighelper/reg/Registries", "registerEnchantment", "(Lnet/minecraft/enchantment/Enchantment;I)I", false));
		list.add(new VarInsnNode(Opcodes.ISTORE, 1));
		node.instructions.insert(ASMHelper.getFirstInstruction(node, Opcodes.INVOKESPECIAL), list);
	}
	
	private static void patchForgeDims(ClassNode classNode) 
	{
		//inject provider lines
		MethodNode provider = ASMHelper.getMethodNode(classNode, "registerProviderType", "(ILjava/lang/Class;Z)Z");
		//id = Registries.registerProvider(provider.class, id);
		InsnList list = new InsnList();
		list.add(new VarInsnNode(Opcodes.ALOAD, 1));
		list.add(new VarInsnNode(Opcodes.ILOAD, 0));
		list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "jml/confighelper/reg/Registries", "registerProvider", "(Ljava/lang/Class;I)I", false));
		list.add(new VarInsnNode(Opcodes.ISTORE, 0));
		//keepLoaded = Registries.keepDimLoaded(id, keepLoaded);
		list.add(new VarInsnNode(Opcodes.ILOAD, 0));
		list.add(new VarInsnNode(Opcodes.ILOAD, 2));
		list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "jml/confighelper/reg/Registries", "keepDimLoaded", "(IZ)Z", false));
		list.add(new VarInsnNode(Opcodes.ISTORE, 2));
		provider.instructions.insert(ASMHelper.getFirstInstruction(provider), list);
		
		//inject dimension lines
		MethodNode dimensions = ASMHelper.getMethodNode(classNode, "registerDimension", "(II)V");
		InsnList list2 = new InsnList();
		//providerId = Registries.guessProviderId(providerId);
		list2.add(new VarInsnNode(Opcodes.ILOAD, 1));
		list2.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "jml/confighelper/reg/Registries", "guessProviderId", "(I)I", false));
		list2.add(new VarInsnNode(Opcodes.ISTORE, 1));
		//Regitries.register(providerId, dimid);
		list2.add(new VarInsnNode(Opcodes.ILOAD, 1));
		list2.add(new VarInsnNode(Opcodes.ILOAD, 0));
		list2.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "jml/confighelper/reg/Registries", "registerDimension", "(II)I", false));
		list2.add(new VarInsnNode(Opcodes.ISTORE, 0));
		dimensions.instructions.insert(ASMHelper.getFirstInstruction(dimensions), list2);
		
		//inject line: Registries.unregisterProvider(id);
		MethodNode unregProvider = ASMHelper.getMethodNode(classNode, "unregisterProviderType", "(I)[I");
		InsnList list3 = new InsnList();
		list3.add(new VarInsnNode(Opcodes.ILOAD, 0));
		list3.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "jml/confighelper/reg/Registries", "unregisterProvider", "(I)V", false));
		unregProvider.instructions.insert(ASMHelper.getFirstInstruction(unregProvider), list3);
		
		//inject line Registries.unregisterDimension(id);
		MethodNode unregDim = ASMHelper.getMethodNode(classNode, "unregisterDimension", "(I)V");
		InsnList list4 = new InsnList();
		list4.add(new VarInsnNode(Opcodes.ILOAD, 0));
		list4.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "jml/confighelper/reg/Registries", "unregisterDimension", "(I)V", false));
		unregDim.instructions.insert(ASMHelper.getFirstInstruction(unregDim), list4);
	}
	
	private void patchEntityList(ClassNode classNode) 
	{
		//inject line: Registries.registerEntity(EntityClass.class, name, id)
		MethodNode node = ASMHelper.getMethodNode(classNode, new MCPSidedString("addMapping", "func_75618_a").toString(), "(Ljava/lang/Class;Ljava/lang/String;I)V");
		InsnList list = new InsnList();
		list.add(new VarInsnNode(Opcodes.ALOAD, 0));
		list.add(new VarInsnNode(Opcodes.ALOAD, 1));
		list.add(new VarInsnNode(Opcodes.ILOAD, 2));
		list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "jml/confighelper/reg/Registries", "registerEntity", "(Ljava/lang/Class;Ljava/lang/String;I)I", false));
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
		egg.instructions.insert(ASMHelper.findMethodInsn(egg, check), list2);
	}
	
	private void patchDatawatcher(ClassNode classNode) 
	{
		//add field
		ASMHelper.addFeild(classNode, "reg", "Ljml/confighelper/reg/Registry;");
		//patch constructor
		MethodNode construct = ASMHelper.getConstructionNode(classNode, "(Lnet/minecraft/entity/Entity;)V");
		InsnList list0 = new InsnList();
		list0.add(new VarInsnNode(Opcodes.ALOAD, 0));
		list0.add(new VarInsnNode(Opcodes.ALOAD, 1));
		list0.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "jml/confighelper/reg/Registries", "createWatcherReg", "(Lnet/minecraft/entity/Entity;)Ljml/confighelper/reg/Registry;", false));
		list0.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/entity/DataWatcher", "reg", "Ljml/confighelper/reg/Registry;"));
		construct.instructions.insert(ASMHelper.getLastPutField(construct), list0);
		
		MethodNode addObject = ASMHelper.getMethodNode(classNode, new MCPSidedString("addObject", "func_75682_a").toString(), "(ILjava/lang/Object;)V");
		//inject line: id = Registries.registerDataWatcher(this.field_151511_a, id, this.reg);
		InsnList list2 = new InsnList();
		list2.add(new VarInsnNode(Opcodes.ALOAD, 0));
		list2.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/entity/DataWatcher", new MCPSidedString("field_151511_a", "field_151511_a").toString(), "Lnet/minecraft/entity/Entity;"));
		list2.add(new VarInsnNode(Opcodes.ILOAD, 1));
		list2.add(new VarInsnNode(Opcodes.ALOAD, 0));
		list2.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/entity/DataWatcher", "reg", "Ljml/confighelper/reg/Registry;"));
		list2.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "jml/confighelper/reg/Registries", "registerDataWatcher", "(Lnet/minecraft/entity/Entity;ILjml/confighelper/reg/Registry;)I", false));
		list2.add(new VarInsnNode(Opcodes.ISTORE, 1));
		addObject.instructions.insert(ASMHelper.getFirstInstruction(addObject), list2);
	}

}
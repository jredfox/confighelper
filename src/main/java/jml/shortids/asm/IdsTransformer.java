package jml.shortids.asm;

import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import jml.confighelper.ModReference;
import jml.confighelper.reg.RegistryIds;
import jml.evilnotch.lib.asm.ASMHelper;
import jml.evilnotch.lib.asm.ITransformer;
import jml.evilnotch.lib.reflect.MCPSidedString;
import net.minecraft.util.ResourceLocation;

public class IdsTransformer implements ITransformer{
	
	public static final List<String> clazzes = RegistryIds.asList(new String[]
	{
		"net.minecraft.world.biome.BiomeGenBase",
		"net.minecraft.potion.Potion",
		"net.minecraft.enchantment.Enchantment",
		"net.minecraftforge.common.DimensionManager",
		"net.minecraft.entity.EntityList",
		"net.minecraft.entity.DataWatcher",
		"net.minecraft.client.network.NetHandlerPlayClient"
	});

	@Override
	public ResourceLocation id() 
	{
		return new ResourceLocation("shortids:transformer");
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
			switch(index)
			{
				case 0:
					patchBiome(node);
				break;
				
				case 1:
					patchPotion(node);
				break;
				
				case 2:
					patchEnchantment(node);
				break;
				
				case 4:
					patchEntityList(node);
				break;
				
				case 5:
					patchDatawatcher(node);
				break;
				
				case 6:
					patchNetHandlerPlayClient(node);
				break;
			}
		}
	}

	private void patchEntityList(ClassNode node) {
		// TODO Auto-generated method stub
		
	}

	private void patchEnchantment(ClassNode node) 
	{
		//change enchantment limit from 256 > short max value
		MethodNode clinit = ASMHelper.getClassInitNode(node);
		AbstractInsnNode spot = ASMHelper.getFirstFieldInsn(clinit, new FieldInsnNode(Opcodes.PUTSTATIC, "net/minecraft/enchantment/Enchantment", "enchantmentsList", "[Lnet/minecraft/enchantment/Enchantment;")).getPrevious().getPrevious();
		clinit.instructions.insert(spot, ASMHelper.getPush(RegistryIds.limitEnchantments + 1));
		clinit.instructions.remove(spot);
	}

	private void patchBiome(ClassNode node) 
	{
		
	}

	private void patchPotion(ClassNode node) 
	{
		//extend potion id limit to signed byte(0-127) or short
		MethodNode clinit = ASMHelper.getClassInitNode(node);
		AbstractInsnNode spot = ASMHelper.getFirstFieldInsn(clinit, new FieldInsnNode(Opcodes.PUTSTATIC, "net/minecraft/potion/Potion", new MCPSidedString("potionTypes", "field_76425_a").toString(), "[Lnet/minecraft/potion/Potion;") ).getPrevious().getPrevious();
		clinit.instructions.insert(spot, ASMHelper.getPush(RegistryIds.limitPotions + 1));
		clinit.instructions.remove(spot);
	}
	
	private void patchDatawatcher(ClassNode node) 
	{
		MethodNode write1 = ASMHelper.getMethodNode(node, new MCPSidedString("func_151509_a", "func_151509_a").toString(), "(Lnet/minecraft/network/PacketBuffer;)V");
		patch127(write1);
		MethodNode write2 = ASMHelper.getMethodNode(node, new MCPSidedString("writeWatchedListToPacketBuffer", "func_151507_a").toString(), "(Ljava/util/List;Lnet/minecraft/network/PacketBuffer;)V");
		patch127(write2);
		
		MethodNode addObject = ASMHelper.getMethodNode(node, new MCPSidedString("addObject", "func_75682_a").toString(), "(ILjava/lang/Object;)V");
		
		//delete line Integer integer = (Integer) dataTypes.get(obj.getClass());
		AbstractInsnNode start = ASMHelper.getFirstFieldInsn(addObject, new FieldInsnNode(Opcodes.GETSTATIC, "net/minecraft/entity/DataWatcher", new MCPSidedString("dataTypes", "field_75697_a").toString(), "Ljava/util/HashMap;"));
		AbstractInsnNode end = ASMHelper.nextInsn(start, Opcodes.ASTORE);
		ASMHelper.removeInsn(addObject, start, end);
		
		//inject line: Integer integer = Registries.getWatcherTypeId(obj.getClass());
		InsnList list = new InsnList();
		list.add(new VarInsnNode(Opcodes.ALOAD, 2));
		list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false));
		list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "jml/confighelper/reg/Registries", "getWatcherTypeId", "(Ljava/lang/Class;)Ljava/lang/Integer;", false));
		list.add(new VarInsnNode(Opcodes.ASTORE, 3));
		addObject.instructions.insert(ASMHelper.getFirstInstruction(addObject), list);
		
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
		append.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "jml/evilnotch/lib/asm/ASMHelper", "isFalse", "()Z", false));
		append.add(new JumpInsnNode(Opcodes.IFEQ, todisable.label));
		addObject.instructions.insertBefore(push.getPrevious(), append);
		
		String input = ASMHelper.getInputStream(ModReference.MODID, "DataWatcher"); //"assets/confighelper/asm/" + (ObfHelper.isObf ? "srg/" : "deob/") + "DataWatcher";
		ASMHelper.replaceMethod(node, input, new MCPSidedString("writeWatchableObjectToPacketBuffer", "func_151510_a").toString(), "(Lnet/minecraft/network/PacketBuffer;Lnet/minecraft/entity/DataWatcher$WatchableObject;)V");
		ASMHelper.replaceMethod(node, input, new MCPSidedString("readWatchedListFromPacketBuffer", "func_151508_b").toString(), "(Lnet/minecraft/network/PacketBuffer;)Ljava/util/List;");
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
	
	/**
	 * make it binarary compatible with the id extension change
	 */
	private void patchNetHandlerPlayClient(ClassNode node) 
	{ 
		MethodNode method = ASMHelper.getMethodNode(node, new MCPSidedString("handleEntityEffect", "func_147260_a").toString(), "(Lnet/minecraft/network/play/server/S1DPacketEntityEffect;)V");
		MethodInsnNode m1 = (MethodInsnNode) ASMHelper.getFirstMethodInsn(method, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/network/play/server/S1DPacketEntityEffect", "func_149427_e", "()B", false));
		MethodInsnNode m2 = ASMHelper.nextMethodInsnNode(m1, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/network/play/server/S1DPacketEntityEffect", "func_149425_g", "()S", false));
		MethodInsnNode m3 = ASMHelper.nextMethodInsnNode(m2, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/network/play/server/S1DPacketEntityEffect", "func_149428_f", "()B", false));
		m1.desc = "()I";
		m2.desc = "()I";
		m3.desc = "()I";
	}

}

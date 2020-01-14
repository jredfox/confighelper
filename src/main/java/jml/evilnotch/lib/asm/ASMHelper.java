package jml.evilnotch.lib.asm;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import jml.evilnotch.lib.Validate;
import jml.evilnotch.lib.reflect.MCPSidedString;
import jml.evilnotch.lib.reflect.ReflectionHandler;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;

public class ASMHelper 
{	
	public static Map<String, ClassNode> classNodes = new HashMap();//input to classNode
	
	/**
	 * srg support doesn't patch local vars nor instructions
	 */
	public static MethodNode replaceMethod(ClassNode node, String input, String name, String desc)
	{
		try
		{
			MethodNode origin = getMethodNode(node, name, desc);
			MethodNode toReplace = getMethodNode(input, name, desc);
			origin.instructions = toReplace.instructions;
			origin.localVariables = toReplace.localVariables;
			origin.annotationDefault = toReplace.annotationDefault;
			origin.tryCatchBlocks = toReplace.tryCatchBlocks;
			origin.visibleAnnotations = toReplace.visibleAnnotations;
			origin.visibleLocalVariableAnnotations = toReplace.visibleLocalVariableAnnotations;
			origin.visibleTypeAnnotations = toReplace.visibleTypeAnnotations;
			return origin;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public static ClassNode getClassNodeCached(String input) throws IOException 
	{
		if(classNodes.containsKey(input))
		{
			return classNodes.get(input);
		}
		ClassNode node = getClassNode(input);
		classNodes.put(input, node);
		return node;
	}
	
	/**
	 * after editing one class call this for cleanup
	 */
	public static void clear() 
	{
		classNodes.clear();
	}
	
	public static ClassNode getClassNode(String input) throws IOException 
	{
		InputStream stream = ASMHelper.class.getClassLoader().getResourceAsStream(input);
		return getClassNode(stream);
	}
	
	/**
	 * doesn't get cached for further uses
	 */
	public static ClassNode getClassNode(InputStream stream) throws IOException 
	{
		byte[] newbyte = IOUtils.toByteArray(stream);
		return getClassNode(newbyte);
	}
	
	/**
	 * if you already have the bytes and you don't need the class reader
	 */
	public static ClassNode getClassNode(byte[] newbyte)
	{
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(newbyte);
		classReader.accept(classNode, 0);
		return classNode;
	}
	
	/**
	 * if you use the ITransformer library call this method to replace a full class from another
	 */
	public static void replaceClassNode(ClassNode org, String input) throws IOException
	{
		replaceClassNode(org, getClassNode(input));
	}
	
	/**
	 * if you use the ITransformer library call this method to replace a full class from another
	 */
	public static void replaceClassNode(ClassNode org, ClassNode node) throws IOException
	{
		 org.access = node.access;
		 org.attrs = node.attrs;
		 org.fields = node.fields;
		 org.innerClasses = node.innerClasses;
		 org.interfaces = node.interfaces;
		 org.invisibleAnnotations = node.invisibleAnnotations;
		 org.invisibleTypeAnnotations = node.invisibleTypeAnnotations;
		 org.methods = node.methods;
		 org.name = node.name;
		 org.outerClass = node.outerClass;
		 org.outerMethod = node.outerMethod;
		 org.outerMethodDesc = node.outerMethodDesc;
		 org.signature = node.signature;
		 org.sourceDebug = node.sourceDebug;
		 org.sourceFile = node.sourceFile;
		 org.superName = node.superName;
		 org.version = node.version;
		 org.visibleAnnotations = node.visibleAnnotations;
		 org.visibleTypeAnnotations = node.visibleTypeAnnotations;
	}
	
	public static MCWriter getClassWriter(ClassNode classNode) 
	{
		MCWriter writer = new MCWriter();
		classNode.accept(writer);
		return writer;
	}
	
	/**
	 * get a method node from a possble cached classnode
	 */
	public static MethodNode getMethodNode(String input, String name, String desc) throws IOException 
	{
		return getMethodNode(getClassNodeCached(input), name, desc);
	}
	
	public static MethodNode getMethodNode(ClassNode node, String name, String desc) 
	{
		for (MethodNode method : node.methods)
		{
			if (method.name.equals(name) && method.desc.equals(desc))
			{
				return method;
			}
		}
		return null;
	}
	
	public static FieldNode getFieldNode(ClassNode node, String name)
	{
		for(FieldNode f : node.fields)
			if(f.name.equals(name))
				return f;
		return null;
	}
	
	public static void patchMethod(MethodNode node, String className, String oldClassName)
	{
		patchMethod(node, className, oldClassName, false);
	}
	
	/**
	 * patch a method you can call this directly after replacing it
	 */
	public static void patchMethod(MethodNode node, String className, String oldClassName, boolean patchStatic)
	{
		patchInstructions(node, className, oldClassName, patchStatic);
		patchLocals(node, className);
	}
	
	/**
	 * patch all references on the local variable table instanceof of this to a new class
	 */
	public static void patchLocals(MethodNode method, String name)
	{
		for(LocalVariableNode lvn : method.localVariables)
		{
			if(lvn.name.equals("this"))
			{
				lvn.desc = "L" + name.replace('.', '/') + ";";
				break;
			}
		}
	}
	
	/**
	 * patch previous object owner instructions to new owner with filtering out static fields/method calls
	 */
	public static void patchInstructions(MethodNode mn, String class_name, String class_old,boolean patchStatic) 
	{
		String className = class_name.replace('.', '/');
		String oldClassName = class_old.replace('.', '/');
		
		for(AbstractInsnNode ain : mn.instructions.toArray())
		{
			if(ain instanceof MethodInsnNode)
			{
				MethodInsnNode min = (MethodInsnNode)ain;
				if(min.owner.equals(oldClassName) && (!isStaticMethod(min) || patchStatic) )
				{
					min.owner = className;
				}
			}
			else if(ain instanceof FieldInsnNode)
			{
				FieldInsnNode fin = (FieldInsnNode)ain;
				if(fin.owner.equals(oldClassName) && (!isStaticFeild(fin) || patchStatic) )
				{
					fin.owner = className;
				}
			}
		}
	}
	
	/**
	 * add an interface to a class
	 */
	public static void addInterface(ClassNode node,String theInterface)
	{
		node.interfaces.add(theInterface);
	}
	
	/**
	 * add a object field to the class
	 */
	public static void addFeild(ClassNode node, String name, String desc)
	{
		addFeild(node, name, desc, null);
	}
	
	/**
	 * add a object field to the class with optional signature. The paramDesc is a descriptor of the types of a class HashMap<key,value>
	 */
	public static void addFeild(ClassNode node, String feildName, String desc, String sig)
	{
		FieldNode field = new FieldNode(Opcodes.ACC_PUBLIC, feildName, desc, sig, null);
		node.fields.add(field);
	}
	
	/**
	 * don't add the method if it's already has it
	 */
	public static void addIfMethod(ClassNode classNode, String input, String name, String desc) throws IOException
	{
		MethodNode method = getMethodNode(input, name, desc);
		Validate.nonNull(method);
		if(!containsMethod(classNode, name, desc))
			classNode.methods.add(method);
	}

	/**
	 * add a method no obfuscated checks you have to do that yourself if you got a deob compiled class
	 * no checks for patching the local variables nor the instructions
	 */
	public static MethodNode addMethod(ClassNode node, String input, String name, String desc) throws IOException 
	{
		MethodNode method = getMethodNode(input, name, desc);
		Validate.nonNull(method);
		node.methods.add(method);
		return method;
	}
	
	/**
	 * search from the class node if it contains the method
	 * @return
	 */
	public static boolean containsMethod(ClassNode classNode, String name, String desc) 
	{
		for(MethodNode node : classNode.methods)
			if(node.name.equals(name) && node.desc.equals(desc))
				return true;
		return false;
	}
	
	/**
	 * remove a method don't remove ones that are going to get executed unless you immediately add the same method and descriptor back
	 * @throws IOException 
	 */
	public static void removeMethod(ClassNode node, String name, String desc) throws IOException
	{
		MethodNode method = getMethodNode(node, name, desc);
		if(method != null)
			node.methods.remove(method);
	}
	
	public static void removeField(ClassNode node, String name) 
	{
		FieldNode f = getFieldNode(node, name);
		if(f != null)
			node.fields.remove(f);
	}
	
	/**
	 * find the first instruction to inject
	 */
	public static AbstractInsnNode getFirstInstruction(MethodNode method,int opcode) 
	{
		for(AbstractInsnNode node : method.instructions.toArray())
		{
			if(node.getOpcode() == opcode)
			{
				return node;
			}
		}
		return null;
	}
	
	/**
	 * getting the first instanceof of this will usually tell you where the initial injection point should be after
	 */
	public static LineNumberNode getFirstInstruction(MethodNode method) 
	{
		for(AbstractInsnNode obj : method.instructions.toArray())
			if(obj instanceof LineNumberNode)
				return (LineNumberNode) obj;
		return null;
	}
	
	public static AbstractInsnNode getLastInstruction(MethodNode method)
	{
		AbstractInsnNode[] arr = method.instructions.toArray();
		for(int i=arr.length;i>=0;i--)
		{
			AbstractInsnNode ab = arr[i];
			if(!isReturnOpcode(ab.getOpcode()))
			{
				return ab;
			}
		}
		return null;
	}
	
	/**
	 * get a constructor since they are MethodNodes
	 */
	public static MethodNode getConstructionNode(ClassNode classNode, String desc) 
	{
		return getMethodNode(classNode, "<init>", desc);
	}
	
	public static MethodNode getClassInitNode(ClassNode classNode) 
	{
		return getMethodNode(classNode, "<clinit>", "()V");
	}
	
	/**
	 * helpful for finding injection point to the end of constructors
	 */
	public static AbstractInsnNode getLastPutField(MethodNode mn) 
	{
		return getLastInstruction(mn, Opcodes.PUTFIELD);
	}
	
	/**
	 * optimized way of getting a last instruction
	 */
	public static AbstractInsnNode getLastInstruction(MethodNode method, int opCode) 
	{
		AbstractInsnNode[] arr = method.instructions.toArray();
		for(int i=arr.length-1;i>=0;i--)
		{
			AbstractInsnNode node = arr[i];
			if(node.getOpcode() == opCode)
				return node;
		}
		return null;
	}
	
	/**
	 * add a brand new method node into the classNode
	 */
	public static void addMethodNodeIf(ClassNode classNode, int opcode, String name, String desc) 
	{
		if(containsMethod(classNode, name, desc))
		{
			System.out.println("returing class has method already!" + name + "," + desc);
			return;
		}
		MethodNode node = new MethodNode(opcode, name, desc, null, null);
		classNode.methods.add(node);
	}
	
	/**
	 * get a local variable index by it's owner name
	 */
	public static int getLocalVarIndexFromOwner(MethodNode method, String owner, String name) 
	{
		for(LocalVariableNode node : method.localVariables)
		{
			if(node.desc.equals(owner) && node.name.equals(name))
				return node.index;
		}
		return -1;
	}
	
	public static String toString(FieldNode node) 
	{
		return node.name + " desc:" + node.desc + " signature:" + node.signature + " access:" + node.access;
 	}
	
	public static String toString(MethodNode node) 
	{
		return node.name + " desc:" + node.desc + " signature:" + node.signature + " access:" + node.access;
 	}

	public static MethodInsnNode getLastMethodInsn(MethodNode node, MethodInsnNode compare) 
	{
		AbstractInsnNode[] list = node.instructions.toArray();
		for(int i = list.length-1; i >=0 ; i--)
		{
			AbstractInsnNode ab = list[i];
			if(equals(compare, ab) )
			{
				return (MethodInsnNode)ab;
			}
		}
		return null;
	}
	
	public static FieldInsnNode getLastFieldInsn(MethodNode node, FieldInsnNode compare) 
	{
		AbstractInsnNode[] list = node.instructions.toArray();
		for(int i = list.length-1; i >=0 ; i--)
		{
			AbstractInsnNode ab = list[i];
			if(equals(compare, ab) )
			{
				return (FieldInsnNode)ab;
			}
		}
		return null;
	}
	
	public static boolean equals(MethodInsnNode obj1, Object o2)
	{
		if(!(o2 instanceof MethodInsnNode))
			return false;
		MethodInsnNode obj2 = (MethodInsnNode)o2;
		return obj1.getOpcode() == obj2.getOpcode() && obj1.name.equals(obj2.name) && obj1.desc.equals(obj2.desc) && obj1.owner.equals(obj2.owner) && obj1.itf == obj2.itf;
	}
	
	public static boolean equals(FieldInsnNode obj1, Object o2)
	{
		if(!(o2 instanceof FieldInsnNode))
			return false;
		FieldInsnNode obj2 = (FieldInsnNode)o2;
		return obj1.getOpcode() == obj2.getOpcode() && obj1.name.equals(obj2.name) && obj1.desc.equals(obj2.desc) && obj1.owner.equals(obj2.owner);
	}
	
	public static boolean equals(FieldNode obj1, FieldNode obj2)
	{
		return obj1.name.equals(obj2.name) && obj1.desc.equals(obj2.desc);
	}
	
	public static boolean equals(MethodNode obj1, MethodNode obj2)
	{
		return obj1.name.equals(obj2.name) && obj1.desc.equals(obj2.desc);
	}
	
	/**
	 * dumps a file from memory
	 */
	public static void dumpFile(String name, ClassWriter classWriter) throws IOException 
	{
		dumpFile(name, classWriter.toByteArray());
	}
	
	/**
	 * dumps a file from memory
	 */
	public static void dumpFile(String name, byte[] bytes) throws IOException 
	{
    	name = name.replace('.', '/');
    	File f = new File(Launch.minecraftHome + "/asm/dumps/" + name + ".class");
    	f.getParentFile().mkdirs();
    	FileUtils.writeByteArrayToFile(f, bytes);
	}

	public static String getMethodDescriptor(Class clazz, String name, Class... params)
	{
		Method m = ReflectionHandler.getMethod(clazz, name, params);
		if(m == null)
			return null;
	    String s = "(";
	    for(final Class c : m.getParameterTypes())
	    {
	        s += getTypeForClass(c);
	    }
	    s+=')';
	    return s + getTypeForClass(m.getReturnType());
	}
	
	public static String getTypeForClass(final Class c)
	{
	    if(c.isPrimitive())
	    {
	        if(c==byte.class)
	            return "B";
	        if(c==char.class)
	            return "C";
	        if(c==double.class)
	            return "D";
	        if(c==float.class)
	            return "F";
	        if(c==int.class)
	            return "I";
	        if(c==long.class)
	            return "J";
	        if(c==short.class)
	            return "S";
	        if(c==boolean.class)
	            return "Z";
	        if(c==void.class)
	            return "V";
	        throw new RuntimeException("Unrecognized primitive " + c);
	    }
	    if(c.isArray()) 
	    {
	    	return c.getName().replace('.', '/');
	    }
	    return ('L' + c.getName() + ';').replace('.', '/');
	}
	
	public static AbstractInsnNode findFieldInsn(MethodNode node, FieldInsnNode field) 
	{
		for(AbstractInsnNode ab : node.instructions.toArray())
		{
			if(ASMHelper.equals(field, ab))
			{
				return ab;
			}
		}
		return null;
	}
	
	public static AbstractInsnNode findMethodInsn(MethodNode node, MethodInsnNode field) 
	{
		for(AbstractInsnNode ab : node.instructions.toArray())
		{
			if(ASMHelper.equals(field, ab))
			{
				return ab;
			}
		}
		return null;
	}
	
	/**
	 * get the standard recommended input stream for asm
	 */
	public static String getInputStream(String modid, String clazzName) 
	{
		return "assets/" + modid + "/asm/" + (ObfHelper.isObf ? "srg/" : "deob/") + clazzName;
	}
	
	/**
	 * get a classes simple name without loading it
	 */
	public static String getSimpleName(String clazz)
	{
		String[] args = clazz.split("\\.");
		return args[args.length-1];
	}

	public static JumpInsnNode nextJumpInsnNode(AbstractInsnNode starting) 
	{
		AbstractInsnNode k = starting;
		while(k != null)
		{
			k = k.getNext();
			if(k instanceof JumpInsnNode)
				return (JumpInsnNode) k;
		}
		return null;
	}
	
	public static FieldInsnNode nextFieldInsnNode(AbstractInsnNode starting) 
	{
		AbstractInsnNode k = starting;
		while(k != null)
		{
			k = k.getNext();
			if(k instanceof FieldInsnNode)
				return (FieldInsnNode) k;
		}
		return null;
	}

	public static IntInsnNode nextIntInsn(AbstractInsnNode ab) 
	{
		while(ab != null)
		{
			ab = ab.getNext();
			if(ab instanceof IntInsnNode)
				return (IntInsnNode) ab;
		}
		return null;
	}

	public static void clearVoidMethod(MethodNode method) 
	{
		LineNumberNode line = ASMHelper.getFirstInstruction(method);
		method.instructions.clear();
		LabelNode label = new LabelNode();
		method.instructions.add(label);
		method.instructions.add(new LineNumberNode(line.line, label));
		method.instructions.add(new InsnNode(Opcodes.RETURN));
	}

	public static AbstractInsnNode nextInsn(AbstractInsnNode ab, int opcode) 
	{
		while(ab != null)
		{
			ab = ab.getNext();
			if(ab != null && ab.getOpcode() == opcode)
				return ab;
		}
		return null;
	}

	/**
	 * make sure the start and end are fetched directly from the method node
	 */
	public static void removeInsn(MethodNode node, AbstractInsnNode start, AbstractInsnNode end) 
	{
		while(start != end)
		{
			AbstractInsnNode next = start.getNext();
			node.instructions.remove(start);
			start = next;
		}
		node.instructions.remove(end);
	}
	
	public static void removeInsn(MethodNode node, AbstractInsnNode start, int size) 
	{
		for(int i=0; i < size; i++)
		{
			AbstractInsnNode next = start.getNext();
			node.instructions.remove(start);
			start = next;
		}
	}

	public static void clearNextThrowable(MethodNode method, String desc_exception) 
	{
		AbstractInsnNode start = null;
		AbstractInsnNode end = null;
		for(AbstractInsnNode ab : method.instructions.toArray())
		{
			if(Opcodes.NEW == ab.getOpcode())
			{
				TypeInsnNode type = (TypeInsnNode)ab;
				if(type.desc.equals(desc_exception))
				{
					start = ab;
					end = ASMHelper.nextInsn(start, Opcodes.ATHROW);
					break;
				}
			}
		}
		if(start != null)
			ASMHelper.removeInsn(method, start, end);
	}
	
	/**
	 * this will determine if the node is static or not
	 */
	public static boolean isStaticMethod(MethodInsnNode min) 
	{
		int opcode = min.getOpcode();
		return Opcodes.INVOKESTATIC == opcode;
	}
	
	/**
	 *this will determine if the node is static or not
	 */
	public static boolean isStaticFeild(FieldInsnNode fin) 
	{
		int opcode = fin.getOpcode();
		return Opcodes.GETSTATIC == opcode || Opcodes.PUTSTATIC == opcode;
	}

	public static boolean isReturnOpcode(int opcode)
	{
		return opcode == Opcodes.RETURN || opcode == Opcodes.ARETURN || opcode == Opcodes.DRETURN || opcode == Opcodes.FRETURN || opcode == Opcodes.IRETURN || opcode == Opcodes.LRETURN;
	}
}

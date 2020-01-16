package jml.evilnotch.lib.asm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.objectweb.asm.tree.ClassNode;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.ResourceLocation;

public class TransformsReg {
	
	public static List<ITransformer> transformers = new ArrayList(2);
	private static boolean sorted;
	
	public static void registerTransformer(String transformerClass)
	{
		try
		{
			ITransformer transformer = (ITransformer) Launch.classLoader.loadClass(transformerClass).newInstance();
			transformers.add(transformer);
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
	}
	
	protected static void sort() 
	{
		if(sorted)
			return;
		Collections.sort(transformers, new Comparator<ITransformer>()
		{
			@Override
			public int compare(ITransformer o1, ITransformer o2) 
			{
				return ((Integer)o1.sortingIndex()).compareTo(o2.sortingIndex());
			}
		});
		sorted = true;
	}

	public static String printIds()
	{
		StringBuilder b = new StringBuilder();
		String space = "\n";
		for(ITransformer t : transformers)
			b.append(space + "ITransformer:(" + t.id() + ")");
		return b.toString();
	}

	public static List<ResourceLocation> getIds() 
	{
		List<ResourceLocation> li = new ArrayList();
		for(ITransformer t : transformers)
			li.add(t.id());
		return li;
	}

}

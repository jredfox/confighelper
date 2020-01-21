package jml.evilnotch.lib.asm;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;

import cpw.mods.fml.relauncher.CoreModManager;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import jml.evilnotch.lib.reflect.ReflectionHandler;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.util.ResourceLocation;

public class TransformsReg {
	
	public static List<ITransformer> transformers = new ArrayList(2);
	
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

	public static String printIds()
	{
		StringBuilder b = new StringBuilder();
		String space = "\n";
		for(ITransformer t : transformers)
			b.append(space + "ITransformer:(" + t.id() + ", class:" + t.getClass() + ")");
		return b.toString();
	}
	
	public static String printClasses()
	{
		StringBuilder b = new StringBuilder();
		String space = "\n";
		for(ITransformer t : transformers)
			b.append(space + "ITransformer:(" + t.getClass() + ")");
		return b.toString();
	}

	public static List<ResourceLocation> getIds() 
	{
		List<ResourceLocation> li = new ArrayList();
		for(ITransformer t : transformers)
			li.add(t.id());
		return li;
	}

	public static void registerCoreMods() 
	{
		List<Object> list = (List<Object>) ReflectionHandler.get(ReflectionHandler.getField(CoreModManager.class, "loadPlugins"), null);
		Class wrapperClass = ReflectionHandler.getClass("cpw.mods.fml.relauncher.CoreModManager$FMLPluginWrapper");
		Field cmodGetter = ReflectionHandler.getField(wrapperClass, "coreModInstance");
		for(Object wrapper : list)
		{
			IFMLLoadingPlugin plugin = (IFMLLoadingPlugin) ReflectionHandler.get(cmodGetter, wrapper);
			if(plugin instanceof Coremod)
			{
				System.out.println("Coremod found:" + plugin.getClass().getName());
				((Coremod)plugin).registerTransformers();
			}
		}
		TransformsReg.sort();//sort the transformers after registering them
	}
	
	protected static void sort() 
	{
		Collections.sort(transformers, new Comparator<ITransformer>()
		{
			@Override
			public int compare(ITransformer o1, ITransformer o2) 
			{
				return ((Integer)o1.sortingIndex()).compareTo(o2.sortingIndex());
			}
		});
	}

}

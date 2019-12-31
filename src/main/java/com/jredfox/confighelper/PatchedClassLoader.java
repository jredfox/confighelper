package com.jredfox.confighelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

import com.evilnotch.lib.reflect.ReflectionHandler;
import com.evilnotch.lib.util.simple.DummyMap;

import net.minecraft.launchwrapper.IClassNameTransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.launchwrapper.LogWrapper;

public class PatchedClassLoader extends LaunchClassLoader{

	public static final Field cachedClasses = ReflectionHandler.makePublicField(LaunchClassLoader.class, "cachedClasses");
	public static final Field resourceCache = ReflectionHandler.makePublicField(LaunchClassLoader.class, "resourceCache");
	public static final Field packageManifets = ReflectionHandler.makePublicField(LaunchClassLoader.class, "packageManifests");
	
	public ClassLoader orgLoader;
	public Set<String> invalidClasses;
	public Set<String> classLoaderExceptions;
    public Set<String> transformerExceptions;
	public List<IClassTransformer> transformers;
	public IClassNameTransformer renameTransformer;
	public Method fClass;
	
	private static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("legacy.debugClassLoading", "false"));
	private static final boolean DEBUG_SAVE = DEBUG && Boolean.parseBoolean(System.getProperty("legacy.debugClassLoadingSave", "false"));
	
	public PatchedClassLoader(LaunchClassLoader child) 
	{
		super(child.getURLs());
		this.orgLoader = (ClassLoader) ReflectionHandler.getObject(ReflectionHandler.makePublicField(LaunchClassLoader.class, "parent"), child);
		this.fClass = ReflectionHandler.makePublicMethod(ClassLoader.class, "findClass", String.class);
		this.invalidClasses = (Set<String>) ReflectionHandler.getObject(ReflectionHandler.makePublicField(LaunchClassLoader.class, "invalidClasses"), child);
		this.classLoaderExceptions = (Set<String>) ReflectionHandler.getObject(ReflectionHandler.makePublicField(LaunchClassLoader.class, "classLoaderExceptions"), child);
		this.transformerExceptions = (Set<String>) ReflectionHandler.getObject(ReflectionHandler.makePublicField(LaunchClassLoader.class, "transformerExceptions"), child);
		this.transformers = (List<IClassTransformer>) ReflectionHandler.getObject(ReflectionHandler.makePublicField(LaunchClassLoader.class, "transformers"), child);
		this.renameTransformer = (IClassNameTransformer) ReflectionHandler.getObject(ReflectionHandler.makePublicField(LaunchClassLoader.class, "renameTransformer"), child);
	}
	
    @Override
    public Class<?> findClass(final String name) throws ClassNotFoundException 
    {
        if (invalidClasses.contains(name))
        {
            throw new ClassNotFoundException(name);
        }

        for (final String exception : classLoaderExceptions)
        {
            if (name.startsWith(exception)) 
            {
                return super.findClass(name);
            }
        }

        for (final String exception : transformerExceptions) 
        {
            if (name.startsWith(exception)) 
            {
            	return super.findClass(name);
            }
        }

        try 
        {
            final String transformedName = transformName(name);
            final String untransformedName = untransformName(name);

            final int lastDot = untransformedName.lastIndexOf('.');
            final String packageName = lastDot == -1 ? "" : untransformedName.substring(0, lastDot);
            final String fileName = untransformedName.replace('.', '/').concat(".class");
            URLConnection urlConnection = findCodeSourceConnectionFor(fileName);
            CodeSigner[] signers = null;
            byte[] oldBytes = getClassBytes(untransformedName);
            final byte[] transformedClass = this.runTransformers(untransformedName, transformedName, oldBytes);
            if(DEBUG_SAVE) 
            {
                saveTransformedClass(transformedClass, transformedName);
            }

            final CodeSource codeSource = urlConnection == null ? null : new CodeSource(urlConnection.getURL(), signers);
            final Class<?> clazz = defineClass(transformedName, transformedClass, 0, transformedClass.length, codeSource);
            return clazz;
        } 
        catch (Throwable e) 
        {
            invalidClasses.add(name);
            if (DEBUG) 
            {
                LogWrapper.log(Level.TRACE, e, "Exception encountered attempting classloading of %s", name);
                LogManager.getLogger("LaunchWrapper").log(Level.ERROR, "Exception encountered attempting classloading of %s", e);
            }
            new ClassNotFoundException(name, e).printStackTrace();
            throw new ClassNotFoundException(name, e);
        }
    }
    
    public String untransformName(final String name) 
    {
        if (renameTransformer != null) 
        {
            return renameTransformer.unmapClassName(name);
        }
        return name;
    }

    public String transformName(final String name) 
    {
        if (renameTransformer != null) 
        {
           return renameTransformer.remapClassName(name);
        }
        return name;
    }
    
    public URLConnection findCodeSourceConnectionFor(final String name) 
    {
        URL resource = findResource(name);
        if (resource != null) 
        {
            try 
            {
                return resource.openConnection();
            } 
            catch (IOException e) 
            {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
    
    public byte[] runTransformers(final String name, final String transformedName, byte[] basicClass) 
    {
       for (final IClassTransformer transformer : transformers) 
       {
          basicClass = transformer.transform(name, transformedName, basicClass);
       }
       return basicClass;
    }
    
    public void saveTransformedClass(final byte[] clazz, final String transformedName) throws IOException
    {
    	 File outFile = new File(Launch.minecraftHome, "asm/" + transformedName.replace('.', File.separatorChar) + ".class");
    	 OutputStream output = new FileOutputStream(outFile);
         output.write(clazz);
         output.close();
    }
	
	/**
	 * stop memory overflow
	 */
	public static void stopMemoryOverflow(LaunchClassLoader loader)
	{
		ReflectionHandler.setObject(cachedClasses, loader, new DummyMap());
		ReflectionHandler.setObject(resourceCache, loader, new DummyMap());
		ReflectionHandler.setObject(packageManifets, loader, new DummyMap());
	}

}

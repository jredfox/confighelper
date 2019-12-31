package com.jredfox.confighelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
import scala.reflect.internal.Trees.This;

public class PatchedClassLoader {

	public static final Field cachedClasses = ReflectionHandler.makePublicField(LaunchClassLoader.class, "cachedClasses");
	public static final Field resourceCache = ReflectionHandler.makePublicField(LaunchClassLoader.class, "resourceCache");
	public static final Field packageManifets = ReflectionHandler.makePublicField(LaunchClassLoader.class, "packageManifests");
	
	/**
	 * stop memory overflow
	 */
	public static void stopMemoryOverflow(LaunchClassLoader loader)
	{
		ReflectionHandler.setObject(cachedClasses, loader, new DummyMap());
		ReflectionHandler.setObject(resourceCache, loader, new DummyMap());
		ReflectionHandler.setObject(packageManifets, loader, new DummyMap());
	}
	
	public static boolean isOptimized(ClassLoader loader)
	{
		return ReflectionHandler.getObject(cachedClasses, loader) instanceof DummyMap &&
			   ReflectionHandler.getObject(resourceCache, loader) instanceof DummyMap &&
			   ReflectionHandler.getObject(packageManifets, loader) instanceof DummyMap;
	}

	public static void checkClassLoader(ClassLoader loader) 
	{
    	boolean modded = Launch.classLoader != loader;
    	if(modded)
    	{
    		System.out.println("modded ClassLoader Detected!" + loader.getClass().getName() + "," + Launch.classLoader.getClass().getName());
    	}
    	if(!PatchedClassLoader.isOptimized(loader) || modded && !PatchedClassLoader.isOptimized(Launch.classLoader))
    	{
    		Registries.makeCrashReport("init", "LaunchClassLoader is unoptimized!");
    	}
	}

}

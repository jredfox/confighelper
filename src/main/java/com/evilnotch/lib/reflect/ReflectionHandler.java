package com.evilnotch.lib.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ReflectionHandler {
	
	public static Field modifiersField;
	static
	{
		try
		{
			modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
	}
	
    public static Field makePublicField(Class clazz, MCPSidedString mcp)
    {
    	return makePublicField(clazz, mcp.toString());
    }
	
	/**
	 * makes the field public and strips the final modifier
	 */
    public static Field makePublicField(Class clazz, String name)
    {
        try
        {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            return field;
        }
        catch(Throwable t)
        {
            t.printStackTrace();
        }
        return null;
    }
    
    public static Method makePublicMethod(Class clazz, MCPSidedString mcp)
    {
    	return makePublicMethod(clazz, mcp.toString());
    }
    
    public static Method makePublicMethod(Class clazz, String name, Class... params)
    {
        try
        {
            Method method = clazz.getDeclaredMethod(name, params);
            method.setAccessible(true);
            return method;
        }
        catch(Throwable t)
        {
            t.printStackTrace();
        }
        return null;
    }
    
    public static Object getObject(Field field, Object instance)
    {
    	try 
    	{
			return field.get(instance);
		} 
    	catch (Throwable t)
    	{
			t.printStackTrace();
		} 
    	return null;
    }
    
    public static void setObject(Field field, Object instance, Object toSet)
    {
    	try 
    	{
			field.set(instance, toSet);
		} 
    	catch (Throwable t)
    	{
			t.printStackTrace();
		} 
    }
    
    public static Object invoke(Method method, Object instance, Object... params)
    {
    	try
    	{
    		return method.invoke(instance, params);
    	}
    	catch(Throwable t)
    	{
    		t.printStackTrace();
    	}
    	return null;
    }

	public static boolean getBoolean(Field field, Object instance) 
	{
		try
		{
			return field.getBoolean(instance);
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
		return false;
	}

}

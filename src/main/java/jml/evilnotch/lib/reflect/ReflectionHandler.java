package jml.evilnotch.lib.reflect;

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
	
    public static Field getField(Class clazz, MCPSidedString mcp)
    {
    	return getField(clazz, mcp.toString());
    }
	
	/**
	 * makes the field public and strips the final modifier
	 */
    public static Field getField(Class clazz, String name)
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
    
    public static Method getMethod(Class clazz, MCPSidedString mcp)
    {
    	return getMethod(clazz, mcp.toString());
    }
    
    public static Method getMethod(Class clazz, String name, Class... params)
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
    
    public static Object get(Field field)
    {
    	return get(field, null);
    }
    
    public static Object get(Field field, Object instance)
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
    
    public static void set(Field field, Object toset)
    {
    	set(field, null, toset);
    }
    
    public static void set(Field field, Object instance, Object toSet)
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
    
    public static Object invoke(Method method, Object... params)
    {
    	return invoke(method, null, params);
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

    //Starting here downwards is to grab primitive values from the fields
	public static Boolean getBoolean(Field field, Object instance) 
	{
		return (Boolean) get(field, instance);
	}
	
	public static Byte getByte(Field field, Object instance)
	{
		return (Byte) get(field, instance);
	}
	
	public static Short getShort(Field field, Object instance)
	{
		return (Short) get(field, instance);
	}
	
	public static Integer getInt(Field field, Object instance)
	{
		return (Integer) get(field, instance);
	}
	
	public static Long getLong(Field field, Object instance)
	{
		return (Long) get(field, instance);
	}
	
	public static Double getDouble(Field field, Object instance)
	{
		return (Double) get(field, instance);
	}
	
	public static Boolean getBoolean(Field field) 
	{
		return (Boolean) get(field);
	}
	
	public static Byte getByte(Field field)
	{
		return (Byte) get(field);
	}
	
	public static Short getShort(Field field)
	{
		return (Short) get(field);
	}
	
	public static Integer getInt(Field field)
	{
		return (Integer) get(field);
	}
	
	public static Long getLong(Field field)
	{
		return (Long) get(field);
	}
	
	public static Double getDouble(Field field)
	{
		return (Double) get(field);
	}

}

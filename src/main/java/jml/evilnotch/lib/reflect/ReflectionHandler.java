package jml.evilnotch.lib.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.commons.lang3.ClassUtils;

import jml.evilnotch.lib.JavaUtil;
import jml.evilnotch.lib.Validate;
import net.minecraftforge.common.util.EnumHelper;

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
    
    public static Class getClass(String className)
    {
    	try
    	{
    		return Class.forName(className);
    	}
    	catch(Throwable t)
    	{
    		t.printStackTrace();
    	}
    	return null;
    }
    
    
    public static Class getClass(String name, boolean clinit, ClassLoader loader)
    {
    	try
    	{
    		return Class.forName(name, clinit, loader);
    	}
    	catch(Throwable t)
    	{
    		t.printStackTrace();
    	}
    	return null;
    } 
    
    public static ClassLoader getClassLoader(Class clazz)
    {
    	return clazz.getClassLoader();
    }
    
    public static Annotation getClassAnnotation(Class clazz, Class test)
    {
    	try
    	{
      		Validate.isTrue(test.isAnnotation());
    		Annotation[] annotations = clazz.getDeclaredAnnotations();
    		for(Annotation an : annotations)
    		{
    			if(an.annotationType().equals(test))
    				return an;
    		}
    	}
    	catch(Throwable t)
    	{
    		t.printStackTrace();
    	}
    	return null;
    }
    
    public static Class getAnotationClass(Annotation an)
    {
    	return an.annotationType();
    }
    
    public static boolean containsInterface(Class clazz, Class intf)
    {
    	try
    	{
    		Validate.isTrue(intf.isInterface());
    		if(clazz.isInterface())
    			return JavaUtil.isClassExtending(intf, clazz);
    		for(Class c : ClassUtils.getAllInterfaces(clazz))
    		{
    			Validate.isTrue(c.isInterface());
    			if(JavaUtil.isClassExtending(intf, c))
    				return true;
    		}
    	}
    	catch(Throwable t)
    	{
    		t.printStackTrace();
    	}
    	return false;
    }
    
    public static Enum getEnum(Class clazz, String name)
    {
    	try
    	{
    		return Enum.valueOf(clazz, name);
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

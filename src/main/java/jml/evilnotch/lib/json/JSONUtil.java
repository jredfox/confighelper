package jml.evilnotch.lib.json;

import java.util.Collection;
import java.util.Map;

import jml.evilnotch.lib.JavaUtil;
import jml.evilnotch.lib.json.internal.Util;

public final class JSONUtil {
	
	/**
	 * fixes any objects before inserting them into a json. Doesn't support static or dynamic arrays
	 */
	public static Object toJSONValue(Object value) 
	{
		if(JavaUtil.isStaticArray(value))
			throw new IllegalArgumentException("Use JSONArray Objects for non primitive values");
		else if(value instanceof Map && !(value instanceof JSONObject) || value instanceof Collection && !(value instanceof JSONArray))
			throw new IllegalArgumentException("Inserted Maps must be JSONObject and Inserted Collections Must be JSONArray");
		
		return canPut(value) ? value : value.toString();
	}
	
	/**
	 * can the object without modifications be inputted into the json object/json array
	 */
	public static boolean canPut(Object value) 
	{
		return value == null ||
			 value instanceof String ||
			 value instanceof Number || 
			 value instanceof Boolean ||
			 value instanceof JSONObject || 
			 value instanceof JSONArray;
	}
}

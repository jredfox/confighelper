package jml.evilnotch.lib.json;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import jml.evilnotch.lib.JavaUtil;
import jml.evilnotch.lib.json.internal.Util;
import jml.evilnotch.lib.json.serialize.JSONParseException;
import jml.evilnotch.lib.json.serialize.JSONParser;

/**
 * Main JSONObject
 * @author jredfox
 */
public class JSONObject extends LinkedHashMap<String, Object>{
	
	public JSONObject()
	{
		super();
	}
	
	public JSONObject(int capacity)
	{
		super(capacity);
	}

	public JSONObject(Map map) 
	{
		super((Map)JSONUtil.toJSONValue(map));
	}
	
	public JSONObject(String json)
	{
		super(new JSONParser().parseJSONObject(json));
	}

	@Override
	public Object put(String key, Object value)
	{
		value = JSONUtil.toJSONValue(value);
		return super.put(key, value);
	}

	@Override
	public Object putIfAbsent(String key, Object value)
	{
		value = JSONUtil.toJSONValue(value);
		return super.putIfAbsent(key, value);
	}
	
	@Override
	public void putAll(Map map)
	{
		map = (Map) JSONUtil.toJSONValue(map);
		super.putAll(map);
	}
	
	public Long getLong(String key)
	{
		return JavaUtil.castLong((Number)this.get(key));
	}

	public Integer getInt(String key)
	{
		return JavaUtil.castInt((Number)this.get(key));
	}
	
	public Short getShort(String key)
	{
		return JavaUtil.castShort((Number)this.get(key));
	}
	
	public Byte getByte(String key)
	{
		return JavaUtil.castByte((Number)this.get(key));
	}
	
	public Double getDouble(String key)
	{
		return JavaUtil.castDouble((Number)this.get(key));
	}
	
	public Float getFloat(String key)
	{
		return JavaUtil.castFloat((Number)this.get(key));
	}
	
	public boolean getBoolean(String key)
	{
		return (Boolean) this.get(key);
	}
	
	public char getChar(String key)
	{
		return this.getString(key).charAt(0);
	}
	
	public String getString(String key)
	{
		return (String) this.get(key);
	}
	
	public JSONObject getJSONObject(String key)
	{
		return (JSONObject)this.get(key);
	}
	
	public JSONArray getJSONArray(String key)
	{
		return (JSONArray)this.get(key);
	}
	
	/**
	 * @param non primitive object arrays are converted into valid json arrays no recursion this converts data into one JSONArray
	 */
	public Object putStaticArray(String key, Object[] value)
	{
		return this.put(key, new JSONArray(value));
	}
	
	public Object putStaticArray(String key, long[] value)
	{
		return this.put(key, new JSONArray(value));
	}
	
	public Object putStaticArray(String key, int[] value)
	{
		return this.put(key, new JSONArray(value));
	}
	
	public Object putStaticArray(String key, short[] value)
	{
		return this.put(key, new JSONArray(value));
	}
	
	public Object putStaticArray(String key, byte[] value)
	{
		return this.put(key, new JSONArray(value));
	}
	
	public Object putStaticArray(String key, double[] value)
	{
		return this.put(key, new JSONArray(value));
	}
	
	public Object putStaticArray(String key, float[] value)
	{
		return this.put(key, new JSONArray(value));
	}
	
	public Object putStaticArray(String key, boolean[] value)
	{
		return this.put(key, new JSONArray(value));
	}
	
	public Object putStaticArray(String key, char[] value)
	{
		return this.put(key, new JSONArray(value));
	}
	
	/**
	 * fetches the json array and converts back to static array. This is unoptimized only call it when needed
	 */
	public String[] getStringArray(String key)
	{
		return this.getJSONArray(key).getStringArray();
	}
	
	public long[] getLongArray(String key)
	{
		return this.getJSONArray(key).getLongArray();
	}
	
	public int[] getIntArray(String key)
	{
		return this.getJSONArray(key).getIntArray();
	}
	
	public short[] getShortArray(String key)
	{
		return this.getJSONArray(key).getShortArray();
	}
	
	public byte[] getByteArray(String key)
	{
		return this.getJSONArray(key).getByteArray();
	}
	
	public double[] getDoubleArray(String key)
	{
		return this.getJSONArray(key).getDoubleArray();
	}
	
	public float[] getFloatArray(String key)
	{
		return this.getJSONArray(key).getFloatArray();
	}
	
	public boolean[] getBooleanArray(String key)
	{
		return this.getJSONArray(key).getBooleanArray();
	}
	
	public char[] getCharArray(String key)
	{
		return this.getJSONArray(key).getCharArray();
	}
	
	/**
	 * get a date
	 */
	public Date getDate(String key, DateFormat format) throws ParseException
	{
		return format.parse(this.getString(key));
	}
	
	/**
	 * add a date into the json object
	 */
	public Object putDate(String key, Date date, DateFormat format)
	{
		return this.put(key, format.format(date));
	}
	
	//TODO:
	public void write(Writer writer) throws IOException 
	{	
		Util.write(this, writer);
	}
	
	/**
	 * clears null values from the JSONObject
	 */
	public void compact() 
	{
		JSONObject object = new JSONObject();
		Iterator<Object> it = this.values().iterator();
		while(it.hasNext())
		{
			Object value = it.next();
			if(value == null)
				it.remove();
		}
	}
	
	//TODO:
	@Override
	public String toString() 
	{
		StringWriter writer = new StringWriter();
		try 
		{
			Util.write(this, writer);
			return writer.toString();
		} 
		catch(IOException exception) 
		{
			exception.printStackTrace();
		}
		return null;
	}
	
	@Override
	public boolean equals(Object object) 
	{
		if(object instanceof JSONObject) 
		{	
			JSONObject other = (JSONObject)object;
			if(this.size() == other.size()) 
			{	
				for(Map.Entry<String, Object> thisEntry : this.entrySet()) 
				{	
					Object key = thisEntry.getKey();
					Object value = thisEntry.getValue();
					Object value2 = other.get(key);
					if(!other.containsKey(key) || value == null && value2 != null || !value.equals(value2)) 
					{	
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}
	
}

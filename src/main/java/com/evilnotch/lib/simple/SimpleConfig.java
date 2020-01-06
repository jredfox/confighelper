package com.evilnotch.lib.simple;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import com.evilnotch.lib.JavaUtil;

public class SimpleConfig {
	
	public Map<String, Object> list = new TreeMap();
	public File file;
	public static final String[] types = {"B", "S", "I", "L", "F", "D", "Z", "Str"};
	public static final String extension = ".scfg";
	public static final String version = "1.0";
	
	public SimpleConfig(File f)
	{
		this.file = f;
	}
	
	public void setFile(File f)
	{
		this.file = f;
	}
	
	public Boolean getBoolean(String key, boolean init)
	{
		return (Boolean) get(key, init);
	}
	
	public Integer getInt(String key, int init)
	{
		return (Integer) get(key, init);
	}
	
	public Long getLong(String key, long init)
	{
		return (Long) get(key, init);
	}
	
	public void set(String key, Object value)
	{
		list.put(key, value);
	}
	
	public Object get(String key, Object init)
	{
		Object value = this.list.get(key);
		if(value == null)
			list.put(key, init);
		return init;
	}
	
	public void parse() throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(this.file));
		String line = reader.readLine();
		while(line != null)
		{
			line = line.trim();
			if(line.indexOf('#') == 0)
				continue;
			String[] reg = JavaUtil.splitFirst(line, ':');
			String type = reg[0];
			line = reg[1];
			reg = JavaUtil.splitFirst(line, '=');
			String key = JavaUtil.parseQuotes(reg[0], 0, "\"");
			String strValue = reg[1];
			Object value = this.parseObj(type, strValue);
			this.list.put(key, value);
			
			line = reader.readLine();
		}
		reader.close();
	}
	
	public void save() throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(this.file));
		writer.write("#build:" + version);
		for(String key : this.list.keySet())
		{
			Object value = this.list.get(key);
			String type = this.getType(value);
			writer.write(type + ":" + "\"" + key + "\"=" + value.toString());
		}
		writer.close();
	}
	
	public Object parseObj(String type, String value)
	{
		if(types[0].equals(type))
			return Byte.parseByte(value);
		else if(types[1].equals(type))
			return Short.parseShort(value);
		else if(types[2].equals(type))
			return Integer.parseInt(value);
		else if(types[3].equals(type))
			return Long.parseLong(value);
		else if(types[4].equals(type))
			return Float.parseFloat(value);
		else if(types[5].equals(type))
			return Double.parseDouble(value);
		else if(types[6].equals(type))
			return Boolean.parseBoolean(value);
		else if(types[7].equals(type))
			return new String(value);
		
		return null;
	}
	
	public String getType(Object obj)
	{
		if(obj instanceof Byte)
			return types[0];
		else if(obj instanceof Short)
			return types[1];
		else if(obj instanceof Integer)
			return types[2];
		else if(obj instanceof Long)
			return types[3];
		else if(obj instanceof Float)
			return types[4];
		else if(obj instanceof Double)
			return types[5];
		else if(obj instanceof Boolean)
			return types[6];
		else if(obj instanceof String)
			return types[7];
		return null;
	}

}

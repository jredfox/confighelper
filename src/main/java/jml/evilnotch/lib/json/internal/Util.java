package jml.evilnotch.lib.json.internal;

import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import jml.evilnotch.lib.json.JSONArray;
import jml.evilnotch.lib.json.JSONObject;
import jml.evilnotch.lib.json.JSONUtil;

/**
 * Contains a few static methods for JSON values.
 * @author FangYidong(fangyidong@yahoo.com.cn)
 * @author Ralph Niemitz/RalleYTN(ralph.niemitz@gmx.de)
 * @version 2.0.0
 * @since 1.0.0
 */
public final class Util {

	// ==== 11.03.2018 | Ralph Niemitz/RalleYTN(ralph.niemitz@gmx.de)
	// -	Removed the "toJSONString" method because it was dead code
	// -	Moved the method "escape(String)" to the class "JSONUtil"
	// -	Renamed all "writeJSONString" methods to simply "write"
	// ====
	
	private Util() {}
	
    /**
     * Encode an array into JSON text and write it to a {@linkplain Writer}.
     * @see  #write(Object, Writer)
     * @param array the array which should be written on the {@linkplain Writer}
     * @param writer the {@linkplain Writer} to write to
     * @throws IOException if an I/O error occurs
     * @since 1.0.0
     */
	public static final void write(byte[] array, Writer writer) throws IOException {
		
		if(array == null) {
			
			writer.write("null");
			
		} else if(array.length == 0) {
			
			writer.write("[]");
			
		} else {
			
			writer.write("[");
			writer.write(""+ array[0]);
			
			for(int index = 1; index < array.length; index++) {
				
				writer.write(",");
				writer.write("" + array[index]);
			}
			
			writer.write("]");
		}
	}
	
    /**
     * Encode an array into JSON text and write it to a {@linkplain Writer}.
     * @see #write(Object, Writer)
     * @param array the array which should be written on the {@linkplain Writer}
     * @param writer the {@linkplain Writer} to write to
     * @throws IOException if an I/O error occurs
     * @since 1.0.0
     */
	public static final void write(short[] array, Writer writer) throws IOException {

		if(array == null) {
			
			writer.write("null");
			
		} else if(array.length == 0) {
			
			writer.write("[]");
			
		} else {
			
			writer.write("[");
			writer.write(""+ array[0]);
			
			for(int index = 1; index < array.length; index++) {
				
				writer.write(",");
				writer.write("" + array[index]);
			}
			
			writer.write("]");
		}
	}
	
    /**
     * Encode an array into JSON text and write it to a {@linkplain Writer}.
     * @see #write(Object, Writer)
     * @param array the array which should be written on the {@linkplain Writer}
     * @param writer the {@linkplain Writer} to write to
     * @throws IOException if an I/O error occurs
     * @since 1.0.0
     */
	public static final void write(int[] array, Writer writer) throws IOException {

		if(array == null) {
			
			writer.write("null");
			
		} else if(array.length == 0) {
			
			writer.write("[]");
			
		} else {
			
			writer.write("[");
			writer.write(""+ array[0]);
			
			for(int index = 1; index < array.length; index++) {
				
				writer.write(",");
				writer.write("" + array[index]);
			}
			
			writer.write("]");
		}
	}
	
    /**
     * Encode an array into JSON text and write it to a {@linkplain Writer}.
     * @see #write(Object, Writer)
     * @param array the array which should be written on the {@linkplain Writer}
     * @param writer the {@linkplain Writer} to write to
     * @throws IOException if an I/O error occurs
     * @since 1.0.0
     */
	public static final void write(long[] array, Writer writer) throws IOException {

		if(array == null) {
			
			writer.write("null");
			
		} else if(array.length == 0) {
			
			writer.write("[]");
			
		} else {
			
			writer.write("[");
			writer.write(""+ array[0]);
			
			for(int index = 1; index < array.length; index++) {
				
				writer.write(",");
				writer.write("" + array[index]);
			}
			
			writer.write("]");
		}
	}
	
    /**
     * Encode an array into JSON text and write it to a {@linkplain Writer}.
     * @see #write(Object, Writer)
     * @param array the array which should be written on the {@linkplain Writer}
     * @param writer the {@linkplain Writer} to write to
     * @throws IOException if an I/O error occurs
     * @since 1.0.0
     */
	public static final void write(float[] array, Writer writer) throws IOException {

		if(array == null) {
			
			writer.write("null");
			
		} else if(array.length == 0) {
			
			writer.write("[]");
			
		} else {
			
			writer.write("[");
			writer.write(""+ array[0]);
			
			for(int index = 1; index < array.length; index++) {
				
				writer.write(",");
				writer.write("" + array[index]);
			}
			
			writer.write("]");
		}
	}
	
    /**
     * Encode an array into JSON text and write it to a {@linkplain Writer}.
     * @see #write(Object, Writer)
     * @param array the array which should be written on the {@linkplain Writer}
     * @param writer the {@linkplain Writer} to write to
     * @throws IOException if an I/O error occurs
     * @since 1.0.0
     */
	public static final void write(double[] array, Writer writer) throws IOException {

		if(array == null) {
			
			writer.write("null");
			
		} else if(array.length == 0) {
			
			writer.write("[]");
			
		} else {
			
			writer.write("[");
			writer.write(""+ array[0]);
			
			for(int index = 1; index < array.length; index++) {
				
				writer.write(",");
				writer.write("" + array[index]);
			}
			
			writer.write("]");
		}
	}
	
    /**
     * Encode an array into JSON text and write it to a {@linkplain Writer}.
     * @see #write(Object, Writer)
     * @param array the array which should be written on the {@linkplain Writer}
     * @param writer the {@linkplain Writer} to write to
     * @throws IOException if an I/O error occurs
     * @since 1.0.0
     */
	public static final void write(boolean[] array, Writer writer) throws IOException {

		if(array == null) {
			
			writer.write("null");
			
		} else if(array.length == 0) {
			
			writer.write("[]");
			
		} else {
			
			writer.write("[");
			writer.write(""+ array[0]);
			
			for(int index = 1; index < array.length; index++) {
				
				writer.write(",");
				writer.write("" + array[index]);
			}
			
			writer.write("]");
		}
	}
	
    /**
     * Encode an array into JSON text and write it to a {@linkplain Writer}.
     * @see #write(Object, Writer)
     * @param array the array which should be written on the {@linkplain Writer}
     * @param writer the {@linkplain Writer} to write to
     * @throws IOException if an I/O error occurs
     * @since 1.0.0
     */
	public static final void write(char[] array, Writer writer) throws IOException {

		if(array == null) {
			
			writer.write("null");
			
		} else if(array.length == 0) {
			
			writer.write("[]");
			
		} else {
			
			writer.write("[");
			writer.write(""+ array[0]);
			
			for(int index = 1; index < array.length; index++) {
				
				writer.write(",");
				writer.write("" + array[index]);
			}
			
			writer.write("]");
		}
	}
	
    /**
     * Encode an array into JSON text and write it to a {@linkplain Writer}.
     * @see #write(Object, Writer)
     * @param array the array which should be written on the {@linkplain Writer}
     * @param writer the {@linkplain Writer} to write to
     * @param <T> type of the array
     * @throws IOException if an I/O error occurs
     * @since 1.0.0
     */
	public static final <T>void write(T[] array, Writer writer) throws IOException {
		
		if(array == null){
			
			writer.write("null");
			
		} else if(array.length == 0) {
			
			writer.write("[]");
			
		} else {
			
			writer.write("[");
			Util.write(array[0], writer);
			
			for(int i = 1; i < array.length; i++){
				
				writer.write(",");
				Util.write(array[i], writer);
			}
			
			writer.write("]");
		}
	}
	
    /**
     * Encode a {@linkplain Collection} into JSON text and write it to a {@linkplain Writer}.
     * @param collection the {@linkplain Collection} which should be written on the {@linkplain Writer}
     * @param writer the {@linkplain Writer} to write to
     * @throws IOException if an I/O error occurs
     * @since 1.0.0
     */
	public static final void write(Collection<?> collection, Writer writer) throws IOException {
		
		if(collection != null) {
			
			boolean first = true;
			Iterator<?> iterator = collection.iterator();
	        writer.write('[');
	        
			while(iterator.hasNext()) {
				
	            if(first) {
	            	
	                first = false;
	                
	            } else {
	            	
	                writer.write(',');
	            }
	            
				Object value = iterator.next();
				
				if(value == null) {
					
					writer.write("null");
					
				} else {
					
					Util.write(value, writer);
				}
			}
			
			writer.write(']');
			
		} else {
		
			writer.write("null");
		}
	}
	
    /**
     * Convert a {@linkplain Map} to a JSON string and write it on a {@linkplain Writer}. This method will not close or flush the given {@linkplain Writer}!
     * @param map the {@linkplain Map} to write
     * @param writer the {@linkplain Writer} to which the {@linkplain Map} should be written to
     * @throws IOException if an I/O error occurs
     * @since 1.0.0
     */
	public static final void write(Map<?, ?> map, Writer writer) throws IOException {
		
		if(map != null) {
			
			boolean first = true;
			writer.write('{');

			for(Map.Entry<?, ?> entry : map.entrySet()) {
				
				if(first) {
					
	                first = false;
	                
				} else {
					
	                writer.write(',');
				}
				
	            writer.write('\"');
	            writer.write(escape(String.valueOf(entry.getKey())));
	            writer.write('\"');
	            writer.write(':');
	            
				Util.write(entry.getValue(), writer);
			}
			
			writer.write('}');
			
		} else {
			
			writer.write("null");
		}
	}
	
	public static final String escape(String string) 
	{
		if(string != null) 
		{
			StringBuilder builder = new StringBuilder();
	        Util.escape(string, builder);
	        return builder.toString();
		}
		return null;
    }
	
    /**
     * Encode an {@linkplain Object} into JSON text and write it to a {@linkplain Writer}.
     * @param value the {@linkplain Object} to write on the {@linkplain Writer}
     * @param writer the {@linkplain Writer} to write on.
     * @throws IOException if an I/O error occurs
     * @since 1.0.0
     */
	@SuppressWarnings("unchecked")
	public static final void write(Object value, Writer writer) throws IOException {
		
		// ==== 11.03.2018 | Ralph Niemitz/RalleYTN(ralph.niemitz@gmx.de)
		// -	Made this method final
		// ====
		
		if(value == null) {
			
			writer.write("null");
			
		} else if(value instanceof String) {
			
            writer.write('\"');
			writer.write(escape((String)value));
            writer.write('\"');
            
		} else if(value instanceof Double)            {writer.write(((Double)value).isInfinite() || ((Double)value).isNaN() ? "null" : value.toString());
		} else if(value instanceof Float)             {writer.write(((Float)value).isInfinite() || ((Float)value).isNaN() ? "null" : value.toString());
		} else if(value instanceof Number)            {writer.write(value.toString());
		} else if(value instanceof Boolean)           {writer.write(value.toString());
		} else if(value instanceof JSONObject)        {((JSONObject)value).write(writer);
		} else if(value instanceof JSONArray)         {((JSONArray)value).write(writer);
		} else if(value instanceof Map)               {Util.write((Map<Object, Object>)value, writer);
		} else if(value instanceof Collection)        {Util.write((Collection<Object>)value, writer);
		} else if(value instanceof byte[])            {Util.write((byte[])value, writer);
		} else if(value instanceof short[])           {Util.write((short[])value, writer);
		} else if(value instanceof int[])             {Util.write((int[])value, writer);
		} else if(value instanceof long[])            {Util.write((long[])value, writer);
		} else if(value instanceof float[])           {Util.write((float[])value, writer);
		} else if(value instanceof double[])          {Util.write((double[])value, writer);
		} else if(value instanceof boolean[])         {Util.write((boolean[])value, writer);
		} else if(value instanceof char[])            {Util.write((char[])value, writer);
		} else if(value.getClass().isArray())         {Util.write((Object[])value, writer);
		} else {
			
			writer.write('"');
			writer.write(escape(value.toString()));
			writer.write('"');
		}
	}
	
	/**
	 * Tries to parse a value to an instance of {@linkplain JSONObject}.
	 * @param value the value that should be parsed
	 * @return the parsed value, or {@code null} if it could not be parsed
	 * @since 1.0.0
	 */
	public static final JSONObject getObject(Object value) {

		if(value != null) {
			
			       if(value instanceof JSONObject) {return (JSONObject)value;
			} else if(value instanceof Map)        {return new JSONObject((Map<?, ?>)value);
			}
		}
		
		return null;
	}
	
	/**
	 * Tries to parse a value to an instance of {@linkplain JSONArray}.
	 * @param value the value that should be parsed
	 * @return the parsed value, or {@code null} if it could not be parsed
	 * @since 1.0.0
	 */
	public static final JSONArray getArray(Object value) {

		if(value != null) {
			
			       if(value instanceof JSONArray)  {return (JSONArray)value;
			} else if(value instanceof boolean[])  {return new JSONArray((boolean[])value);
			} else if(value instanceof byte[])     {return new JSONArray((byte[])value);
			} else if(value instanceof char[])     {return new JSONArray((char[])value);
			} else if(value instanceof short[])    {return new JSONArray((short[])value);
			} else if(value instanceof int[])      {return new JSONArray((int[])value);
			} else if(value instanceof long[])     {return new JSONArray((long[])value);
			} else if(value instanceof float[])    {return new JSONArray((float[])value);
			} else if(value instanceof double[])   {return new JSONArray((double[])value);
			} else if(value instanceof Collection) {return new JSONArray((Collection<?>)value);
			} else if(value.getClass().isArray())  {return new JSONArray((Object[])value);
			}
		}
		
		return null;
	}

	/**
	 * Tries to parse a value to an instance of {@linkplain Boolean}.
	 * @param value the value that should be parsed
	 * @return the parsed value, or {@code null} if it could not be parsed
	 * @since 1.0.0
	 */
	public static final Boolean getBoolean(Object value) {

		if(value != null) {
			
			       if(value instanceof Boolean) {return (Boolean)value;
			} else if(value instanceof String)  {return Boolean.parseBoolean((String)value);
			} else if(value instanceof Number)  {return ((Number)value).longValue() == 1L;
			}
		}
		
		return null;
	}

	/**
	 * Tries to parse a value to an instance of {@linkplain Byte}.
	 * @param value the value that should be parsed
	 * @return the parsed value, or {@code null} if it could not be parsed
	 * @since 1.0.0
	 */
	public static final Byte getByte(Object value) {
		
		if(value != null) {
			
			       if(value instanceof Number)  {return ((Number)value).byteValue();
			} else if(value instanceof String)  {return Byte.parseByte((String)value);
			} else if(value instanceof Boolean) {return (Boolean)value ? (byte)1 : (byte)0;
			}
		}
		
		return null;
	}

	/**
	 * Tries to parse a value to an instance of {@linkplain Short}.
	 * @param value the value that should be parsed
	 * @return the parsed value, or {@code null} if it could not be parsed
	 * @since 1.0.0
	 */
	public static final Short getShort(Object value) {

		if(value != null) {
			
			       if(value instanceof Number)  {return ((Number)value).shortValue();
			} else if(value instanceof String)  {return Short.parseShort((String)value);
			} else if(value instanceof Boolean) {return (Boolean)value ? (short)1 : (short)0;
			}
		}
		
		return null;
	}

	/**
	 * Tries to parse a value to an instance of {@linkplain Integer}.
	 * @param value the value that should be parsed
	 * @return the parsed value, or {@code null} if it could not be parsed
	 * @since 1.0.0
	 */
	public static final Integer getInteger(Object value) {
		
		if(value != null) {
			
			       if(value instanceof Number)  {return ((Number)value).intValue();
			} else if(value instanceof String)  {return Integer.parseInt((String)value);
			} else if(value instanceof Boolean) {return (Boolean)value ? 1 : 0;
			}
		}
		
		return null;
	}

	/**
	 * Tries to parse a value to an instance of {@linkplain Long}.
	 * @param value the value that should be parsed
	 * @return the parsed value, or {@code null} if it could not be parsed
	 * @since 1.0.0
	 */
	public static final Long getLong(Object value) {

		if(value != null) {
			
			       if(value instanceof Number)  {return ((Number)value).longValue();
			} else if(value instanceof String)  {return Long.parseLong((String)value);
			} else if(value instanceof Boolean) {return (Boolean)value ? 1L : 0L;
			}
		}
		
		return null;
	}

	/**
	 * Tries to parse a value to an instance of {@linkplain Float}.
	 * @param value the value that should be parsed
	 * @return the parsed value, or {@code null} if it could not be parsed
	 * @since 1.0.0
	 */
	public static final Float getFloat(Object value) {

		if(value != null) {
			
			       if(value instanceof Number)  {return ((Number)value).floatValue();
			} else if(value instanceof String)  {return Float.parseFloat((String)value);
			} else if(value instanceof Boolean) {return (Boolean)value ? 1F : 0F;
			}
		}
		
		return null;
	}

	/**
	 * Tries to parse a value to an instance of {@linkplain Double}.
	 * @param value the value that should be parsed
	 * @return the parsed value, or {@code null} if it could not be parsed
	 * @since 1.0.0
	 */
	public static final Double getDouble(Object value) {
		
		if(value != null) {
			
			       if(value instanceof Number)  {return ((Number)value).doubleValue();
			} else if(value instanceof String)  {return Double.parseDouble((String)value);
			} else if(value instanceof Boolean) {return (Boolean)value ? 1D : 0D;
			}
		}
		
		return null;
	}

	/**
	 * Tries to parse a value to an instance of {@linkplain String}.
	 * @param value the value that should be parsed
	 * @return the parsed value, or {@code null} if it could not be parsed
	 * @since 1.0.0
	 */
	public static final String getString(Object value) {

		if(value != null) {
			
			return value.toString();
		}
		
		return null;
	}
	
	/**
	 * Tries to parse a value to an instance of {@linkplain Date}.
	 * @param value the value that should be parsed
	 * @param format the {@linkplain DateFormat} that should be used to parse the value
	 * @return an instance of {@linkplain Date} or {@code null} if the input value was already {@code null}
	 * @throws ParseException if the value could not be parsed to an instance of {@linkplain Date}
	 * @since 1.0.0
	 */
	public static final Date getDate(Object value, DateFormat format) throws ParseException {

		if(value != null) {
			
			return format.parse(value.toString());
		}
		
		return null;
	}
	
	/**
	 * Tries to parse a value to an enum.
	 * @param value the value that should be parsed
	 * @param type the enum class
	 * @param <T> the enum type
	 * @return the enum or {@code null} if the value could not be parsed
	 * @since 1.0.0
	 */
	@SuppressWarnings("unchecked")
	public static final <T extends Enum<T>>T getEnum(Object value, Class<T> type) {
		
		if(value != null) {
			
			for(Object enumConstant : type.getEnumConstants()) {
				
				if(((T)enumConstant).name().equals(value.toString())) {
					
					return (T)enumConstant;
				}
			}
		}
		
		return null;
	}

	/**
	 * Escapes a string according to the <a href="https://tools.ietf.org/html/rfc7159#section-8.1">JSON specification</a>.
	 * @param string the string which should be escaped
	 * @param builder the {@linkplain StringBuilder} on which the result will be written
	 * @since 1.0.0
	 */
    public static void escape(String string, StringBuilder builder) {
    	
    	for(int index = 0; index < string.length(); index++) {
    		
    		char character = string.charAt(index);
    		
    		// If is faster than Switch
    		       if(character == '"')  {builder.append("\\\"");
    		} else if(character == '\\') {builder.append("\\\\");
    		} else if(character == '\b') {builder.append("\\b");
    		} else if(character == '\f') {builder.append("\\f");
    		} else if(character == '\n') {builder.append("\\n");
    		} else if(character == '\r') {builder.append("\\r");
    		} else if(character == '\t') {builder.append("\\t");
    		} else if(character == '/')  {builder.append("\\/");
    		} else {
    			
    			if((character >= '\u0000' && character <= '\u001F') ||
    			   (character >= '\u007F' && character <= '\u009F') ||
    			   (character >= '\u2000' && character <= '\u20FF')) {
    				
    				String hex = Integer.toHexString(character);
					builder.append("\\u");
					
					for(int k = 0; k < (4 - hex.length()); k++) {
						
						builder.append('0');
					}
					
					builder.append(hex.toUpperCase());
					
    			} else {
    				
    				builder.append(character);
    			}
    		}
    	}
	}
}

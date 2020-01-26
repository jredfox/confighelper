package jml.evilnotch.lib.json.serialize;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Can format and minimize JSON data.
 * @author Ralph Niemitz/RalleYTN(ralph.niemitz@gmx.de)
 * @version 2.0.0
 * @since 1.0.0
 */
public class JSONFormatter {
	
	private static final String CRLF = "\r\n";
	private static final String LF = "\r\n";
	
	// ==== 10.03.2018 | Ralph Niemitz/RalleYTN(ralph.niemitz@gmx.de)
	// -	Refactored some of the parameter names from "jsonReader" to "reader"
	// 		and from "formattedWriter" and "minimizedWriter" to "writer"
	// -	Decided that it would be better to not make this class static. The formatting has some flags that would be better
	// 		as class attributes. It is a pain in the ass to always give the methods 4 or more parameters.
	// -	Updated the documentation
	// ====
	
	private int indent;
	private char indentCharacter;
	private String lineBreak;
	
	/**
	 * Constructs a new {@linkplain JSONFormatter}.
	 * Default indent is {@code 1} and tabulators will be used for the indent.
	 * @since 2.0.0
	 */
	public JSONFormatter() {
		
		this.indent = 1;
		this.lineBreak = JSONFormatter.LF;
		this.setUseTabs(true);
	}
	
	private final void writeIndent(int level, Writer writer) throws IOException {
		
		for(int currentLevel = 0; currentLevel < level; currentLevel++) {
			
			for(int indent = 0; indent < this.indent; indent++) {
				
				writer.write(this.indentCharacter);
			}
		}
	}
	
	/**
	 * Sets whether a CRLF or a LF line break should be used.
	 * @param crlf {@code true} = CRLF, {@code false} = LF
	 * @since 2.0.0
	 */
	public void setUseCRLF(boolean crlf) {
		
		this.lineBreak = crlf ? JSONFormatter.CRLF : JSONFormatter.LF;
	}
	
	/**
	 * Sets whether a tabulator or space should be used for the indent.
	 * @param tabs {@code true} = tabulator, {@code false} = space
	 * @since 2.0.0
	 */
	public void setUseTabs(boolean tabs) {
		
		this.indentCharacter = tabs ? '\t' : ' ';
	}
	
	/**
	 * Sets the indent.
	 * @param indent the indent
	 * @since 2.0.0
	 */
	public void setIndent(int indent) {
		
		this.indent = indent;
	}
	
	/**
	 * Formats minimized JSON data. Do not try to format already formatted JSON. The result does not look good.
	 * @param reader the {@linkplain Reader} with the JSON data
	 * @param writer the {@linkplain Writer} on which the formatted JSON data should be written
	 * @throws IOException if an I/O error occurs
	 * @since 1.0.0
	 */
	public void format(Reader reader, Writer writer) throws IOException {
		
		int level = 0;
		boolean inString = false;
		int read = -1;
		char lastChar = '\0';
		
		while((read = reader.read()) != -1) {
			
			char character = (char)read;
			
			if(character == '"') {
				
				inString = !(inString && lastChar != '\\');
			}
			
			if(!inString) {
				
				if(character == '{' || character == '[') {
					
					writer.write(character);
					writer.write(this.lineBreak);
					level++;
					this.writeIndent(level, writer);
					continue;
					
				} else if(character == '}' || character == ']') {
					
					writer.write(this.lineBreak);
					level--;
					this.writeIndent(level, writer);
					writer.write(character);
					continue;
					
				} else if(character == ',') {
					
					writer.write(character);
					writer.write(this.lineBreak);
					this.writeIndent(level, writer);
					continue;
					
				} else if(character == ':') {
					
					writer.write(character);
					writer.write(' ');
					continue;
				}
			}
			
			writer.write(character);
			lastChar = character;
		}
	}
	
	/**
	 * Formats minimized JSON data. Do not try to format already formatted JSON. The result does not look good.
	 * @param json the JSON data that should be formatted
	 * @return the formatted JSON data
	 * @since 1.0.0
	 */
	public String format(String json) {
		
		// ==== 10.03.2018 | Ralph Niemitz/RalleYTN(ralph.niemitz@gmx.de)
		// Method now calls #format(Reader,Writer) because it is less code to maintain
		// ====
		
		try
		{
			StringReader reader = new StringReader(json);
			StringWriter writer = new StringWriter();
			
			this.format(reader, writer);
			return writer.toString();
			
		} catch(IOException exception) {
			
			exception.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Minimizes formatted JSON data.
	 * @param reader the {@linkplain Reader} with the formatted JSON data
	 * @param writer the {@linkplain Writer} on which the minimized JSON data should be written
	 * @throws IOException if an I/O error occurs
	 * @since 1.0.0
	 */
	public void minimize(Reader reader, Writer writer) throws IOException {
		
		// ==== 10.03.2018 | Ralph Niemitz/RalleYTN(ralph.niemitz@gmx.de)
		// Forgot to remove \r from the JSON
		// ====
		
		boolean inString = false;
		char lastChar = '\0';
		int read = -1;
		
		while((read = reader.read()) != -1) {
			
			char character = (char)read;
			
			if(character != '\n' &&
			   character != '\t' &&
			   character != '\r' &&
			   character != '\b' &&
			   character != '\0' &&
			   character != '\f') {
				
				if(character == '"') {
					
					inString = !(inString && lastChar != '\\');
				}
				
				if(!(character == ' ' && !inString)) {
					
					writer.write(character);
				}
			}
			
			lastChar = character;
		}
	}
	
	/**
	 * Minimizes formatted JSON data.
	 * @param json the formatted JSON data
	 * @return the minimized JSON data
	 * @since 1.0.0
	 */
	public String minimize(String json) {
		
		// ==== 10.03.2018 | Ralph Niemitz/RalleYTN(ralph.niemitz@gmx.de)
		// Method now calls #minimize(Reader,Writer) because it is less code to maintain
		// ====
		
		try
		{
			StringReader reader = new StringReader(json);
			StringWriter writer = new StringWriter();
			
			this.minimize(reader, writer);
			return writer.toString();
			
		} catch(IOException exception) {
			
			// WILL NEVER HAPPEN!
			// DO NOTHING!
		}
		
		return null;
	}
	
	/**
	 * @return {@code true} when CRLF line breaks are used for formatting, else {@code false}
	 * @since 2.0.0
	 */
	public boolean usesCRLF() {
		
		return JSONFormatter.CRLF.equals(this.lineBreak);
	}
	
	/**
	 * @return {@code true} if tabulators are used for the indent, else {@code false}
	 * @since 2.0.0
	 */
	public boolean usesTabs() {
		
		return this.indentCharacter == '\t';
	}
	
	/**
	 * @return the indent
	 * @since 2.0.0
	 */
	public int getIndent() {
		
		return this.indent;
	}
}

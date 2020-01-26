package jml.evilnotch.lib.json.serialize;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import jml.evilnotch.lib.json.JSONArray;
import jml.evilnotch.lib.json.JSONObject;
import jml.evilnotch.lib.json.internal.Yylex;
import jml.evilnotch.lib.json.internal.Yytoken;


/**
 * Parses JSON data (<u>not</u> thread-safe).
 * @author Ralph Niemitz/RalleYTN(ralph.niemitz@gmx.de)
 */
public class JSONParser {
	private static final int S_INIT = 0;
	private static final int S_IN_FINISHED_VALUE = 1;
	private static final int S_IN_OBJECT = 2;
	private static final int S_IN_ARRAY = 3;
	private static final int S_PASSED_PAIR_KEY = 4;
	private static final int S_IN_PAIR_VALUE = 5;
	private static final int S_END = 6;
	private static final int S_IN_ERROR = -1;
	
	private Stack<Object> handlerStatusStack;
	private Yylex lexer = new Yylex((Reader)null);
	private Yytoken token;
	private int status = JSONParser.S_INIT;
	
	private final void nextToken() throws JSONParseException, IOException {
		
		this.token = this.lexer.yylex();
		
		if(this.token == null) {
			
			this.token = new Yytoken(Yytoken.TYPE_EOF, null);
		}
	}
	
	private final void init(Stack<Object> statusStack, Stack<Object> valueStack) {
		
		if(this.token.type == Yytoken.TYPE_VALUE) {
			
			this.status = JSONParser.S_IN_FINISHED_VALUE;
			statusStack.push(this.status);
			valueStack.push(this.token.value);
			
		} else if(this.token.type == Yytoken.TYPE_LEFT_BRACE) {
			
			this.status = JSONParser.S_IN_OBJECT;
			statusStack.push(this.status);
			valueStack.push(new JSONObject());
			
		} else if(this.token.type == Yytoken.TYPE_LEFT_SQUARE) {
			
			this.status = JSONParser.S_IN_ARRAY;
			statusStack.push(this.status);
			valueStack.push(new JSONArray());
			
		} else {
			
			this.status = JSONParser.S_IN_ERROR;
		}
	}
	
	private final Object inFinishedValue(Stack<Object> valueStack) throws JSONParseException {
		
		if(this.token.type == Yytoken.TYPE_EOF) {
			
			return valueStack.pop();
			
		} else {
			
			throw new JSONParseException(this.getPosition(), JSONParseException.ERROR_UNEXPECTED_TOKEN, this.token);
		}
	}
	
	private final void inObject(Stack<Object> statusStack, Stack<Object> valueStack) {
		
		if(this.token.type == Yytoken.TYPE_VALUE) {
			
			if(this.token.value instanceof String) {
				
				String key = (String)this.token.value;
				valueStack.push(key);
				this.status = JSONParser.S_PASSED_PAIR_KEY;
				statusStack.push(this.status);
				
			} else {
				
				this.status = S_IN_ERROR;
			}
			
		} else if(this.token.type == Yytoken.TYPE_RIGHT_BRACE) {
			
			if(valueStack.size() > 1){
				
				statusStack.pop();
				valueStack.pop();
				this.status = (Integer)statusStack.peek();
				
			} else {
				
				this.status = JSONParser.S_IN_FINISHED_VALUE;
			}
			
		} else if(this.token.type != Yytoken.TYPE_COMMA) {
			
			this.status = JSONParser.S_IN_ERROR;
		}
	}
	
	@SuppressWarnings("unchecked")
	private final void inPassedPairKey(Stack<Object> statusStack, Stack<Object> valueStack) {
		
		if(this.token.type == Yytoken.TYPE_VALUE) {
			
			statusStack.pop();
			String key = (String)valueStack.pop();
			Map<Object, Object> parent = (Map<Object, Object>)valueStack.peek();
			parent.put(key, this.token.value);
			this.status = (Integer)statusStack.peek();
			
		} else if(this.token.type == Yytoken.TYPE_LEFT_SQUARE) {
			
			statusStack.pop();
			String key = (String)valueStack.pop();
			Map<Object, Object> parent = (Map<Object, Object>)valueStack.peek();
			List<Object> newArray = new JSONArray();
			parent.put(key, newArray);
			this.status = JSONParser.S_IN_ARRAY;
			statusStack.push(this.status);
			valueStack.push(newArray);
			
		} else if(this.token.type == Yytoken.TYPE_LEFT_BRACE) {
			
			statusStack.pop();
			String key = (String)valueStack.pop();
			Map<Object, Object> parent = (Map<Object, Object>)valueStack.peek();
			Map<String, Object> newObject = new JSONObject();
			parent.put(key, newObject);
			this.status = JSONParser.S_IN_OBJECT;
			statusStack.push(this.status);
			valueStack.push(newObject);
			
		} else if(this.token.type != Yytoken.TYPE_COLON) {
			
			this.status = JSONParser.S_IN_ERROR;
		}
	}
	
	@SuppressWarnings("unchecked")
	private final void inArray(Stack<Object> statusStack, Stack<Object> valueStack) {
		
		if(this.token.type == Yytoken.TYPE_VALUE) {
			
			List<Object> val = (List<Object>)valueStack.peek();
			val.add(this.token.value);
			
		} else if(this.token.type == Yytoken.TYPE_RIGHT_SQUARE) {
			
			if(valueStack.size() > 1) {
				
				statusStack.pop();
				valueStack.pop();
				this.status = (Integer)statusStack.peek();
			
			} else {
				
				this.status = JSONParser.S_IN_FINISHED_VALUE;
			}
			
		} else if(this.token.type == Yytoken.TYPE_LEFT_BRACE) {
			
			List<Object> val = (List<Object>)valueStack.peek();
			Map<String, Object> newObject = new JSONObject();
			val.add(newObject);
			this.status = JSONParser.S_IN_OBJECT;
			statusStack.push(this.status);
			valueStack.push(newObject);
			
		} else if(this.token.type == Yytoken.TYPE_LEFT_SQUARE) {
			
			List<Object> val = (List<Object>)valueStack.peek();
			List<Object> newArray = new JSONArray();
			val.add(newArray);
			this.status = JSONParser.S_IN_ARRAY;
			statusStack.push(this.status);
			valueStack.push(newArray);
			
		} else if(this.token.type != Yytoken.TYPE_COMMA) {
			
			this.status = JSONParser.S_IN_ERROR;
		}
	}
	
    /**
     * Resets the parser to the initial state without resetting the underlying reader.
     * @since 1.0.0
     */
    public void reset() {
    	
        this.token = null;
        this.status = JSONParser.S_INIT;
        this.handlerStatusStack = null;
    }
    
    /**
     * Resets the parser to the initial state with a new character reader.
     * @param reader the new character reader
     * @since 1.0.0
     */
	public void reset(Reader reader) {
		
		this.lexer.yyreset(reader);
		this.reset();
	}
	
	/**
	 * @return the position where the current token begins
	 * @since 1.0.0
	 */
	public int getPosition() {
		
		return this.lexer.getPosition();
	}
	
	public JSONObject parseJSONObject(String s) 
	{
		try 
		{
			return (JSONObject) this.parse(s);
		} 
		catch (JSONParseException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public JSONObject parseJSONObject(Reader in) throws IOException, JSONParseException{
		return (JSONObject) this.parse(in);
	}
	
	public JSONArray parseJSONArray(String s)
	{
		try 
		{
			return (JSONArray) this.parse(s);
		} 
		catch (JSONParseException e) 
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public JSONArray parseJSONArray(Reader in) throws IOException, JSONParseException{
		return (JSONArray) this.parse(in);
	} 
	
	/**
	 * Parses JSON data.
	 * @param json the JSON data
	 * @return An instance of:
	 * <ul>
	 * <li>{@linkplain JSONObject}</li>
	 * <li>{@linkplain JSONArray}</li>
	 * <li>{@linkplain String}</li>
	 * <li>{@linkplain Number}</li>
	 * <li>{@linkplain Boolean}</li>
	 * <li>{@code null}</li>
	 * </ul>
	 * @throws JSONParseException if the JSON is invalid
	 * @since 1.0.0
	 */
	public Object parse(String json) throws JSONParseException 
	{
		StringReader reader = new StringReader(json);
		try 
		{	
			return this.parse(reader);	
		} 
		catch(IOException exception) 
		{
			exception.printStackTrace();
			throw new RuntimeException(exception);
		}
	}
	
	/**
	 * Parses JSON data from a {@linkplain Reader}.
	 * @param reader the {@linkplain Reader}
	 * @return An instance of:
	 * <ul>
	 * <li>{@linkplain JSONObject}</li>
	 * <li>{@linkplain JSONArray}</li>
	 * <li>{@linkplain String}</li>
	 * <li>{@linkplain Number}</li>
	 * <li>{@linkplain Boolean}</li>
	 * <li>{@code null}</li>
	 * </ul>
	 * @throws IOException if an I/O error occurs
	 * @throws JSONParseException if the JSON is invalid
	 * @since 1.0.0
	 */
	public Object parse(Reader reader) throws IOException, JSONParseException {
		
		// ==== 11.03.2018 | Ralph Niemitz/RalleYTN(ralph.niemitz@gmx.de)
		// -	Removed the unnecessary try-catch block
		// ====
		
		this.reset(reader);
		Stack<Object> statusStack = new Stack<Object>();
		Stack<Object> valueStack = new Stack<Object>();

		do {
			
			this.nextToken();
			
				   if(this.status == JSONParser.S_INIT)              {this.init(statusStack, valueStack);
			} else if(this.status == JSONParser.S_IN_FINISHED_VALUE) {return this.inFinishedValue(valueStack);
			} else if(this.status == JSONParser.S_IN_OBJECT)         {this.inObject(statusStack, valueStack);
			} else if(this.status == JSONParser.S_PASSED_PAIR_KEY)   {this.inPassedPairKey(statusStack, valueStack);
			} else if(this.status == JSONParser.S_IN_ARRAY)          {this.inArray(statusStack, valueStack);
			}
			
			if(this.status == JSONParser.S_IN_ERROR) {
				
				throw new JSONParseException(getPosition(), JSONParseException.ERROR_UNEXPECTED_TOKEN, token);
			}
	
		} while(this.token.type != Yytoken.TYPE_EOF);

		throw new JSONParseException(this.getPosition(), JSONParseException.ERROR_UNEXPECTED_TOKEN, this.token);
	}
	
	/**
	 * Goes over a JSON string step by step using a {@linkplain JSONContentHandler}.
	 * @param string the JSON string
	 * @param contentHandler the {@linkplain JSONContentHandler}
	 * @throws JSONParseException if the JSON is invalid or the {@linkplain JSONContentHandler} throws it
	 * @since 1.0.0
	 */
	public void parse(String string, JSONContentHandler contentHandler) throws JSONParseException {
		
		this.parse(string != null ? string.trim() : null, contentHandler, false);
	}
	
	/**
	 * Goes over a JSON string step by step using a {@linkplain JSONContentHandler}.
	 * @param string the JSON string
	 * @param contentHandler the {@linkplain JSONContentHandler}
	 * @param resume Indicates if the previous parsing operation should be continued.
	 * @throws JSONParseException if the JSON is invalid or the {@linkplain JSONContentHandler} throws it
	 * @since 1.0.0
	 */
	public void parse(String string, JSONContentHandler contentHandler, boolean resume) throws JSONParseException 
	{
		StringReader reader = new StringReader(string != null ? string.trim() : null);
		try 
		{	
			this.parse(reader, contentHandler, resume);
		} 
		catch(IOException exception)
		{
			throw new JSONParseException(-1, JSONParseException.ERROR_UNEXPECTED_EXCEPTION, exception);
		}
	}
	
	/**
	 * Goes over JSON data step by step using a {@linkplain JSONContentHandler}.
	 * @param reader the {@linkplain Reader}
	 * @param contentHandler the {@linkplain JSONContentHandler}
	 * @throws IOException if an I/O error occurs or the {@linkplain JSONContentHandler} throws it
	 * @throws JSONParseException if the JSON is invalid or the {@linkplain JSONContentHandler} throws it
	 * @since 1.0.0
	 */
	public void parse(Reader reader, JSONContentHandler contentHandler) throws IOException, JSONParseException {
		
		this.parse(reader, contentHandler, false);
	}
	
	/**
	 * Goes over JSON data step by step using a {@linkplain JSONContentHandler}.
	 * @see JSONContentHandler
	 * @param reader the {@linkplain Reader}
	 * @param contentHandler the {@linkplain JSONContentHandler}
	 * @param resume Indicates if the previous parsing operation should be continued.
	 * @throws IOException if an I/O error occurs or the {@linkplain JSONContentHandler} throws it
	 * @throws JSONParseException if the JSON is invalid or the {@linkplain JSONContentHandler} throws it
	 * @since 1.0.0
	 */
	public void parse(Reader reader, JSONContentHandler contentHandler, boolean resume) throws IOException, JSONParseException {
		
		if(!resume) {
			
			this.reset(reader);
			this.handlerStatusStack = new Stack<Object>();
		
		} else if(this.handlerStatusStack == null) {
				
			this.reset(reader);
			this.handlerStatusStack = new Stack<Object>();
		}
		
		Stack<Object> statusStack = this.handlerStatusStack;	
		
		try {
			
			do {
				
				if(this.status == JSONParser.S_INIT) {
					
					contentHandler.startJSON();
					this.nextToken();
					
					if(this.token.type == Yytoken.TYPE_VALUE) {
						
						this.status = JSONParser.S_IN_FINISHED_VALUE;
						statusStack.push(this.status);
						
						if(!contentHandler.primitive(this.token.value)) {
							
							return;
						}
						
					} else if(this.token.type == Yytoken.TYPE_LEFT_BRACE) {
						
						this.status = JSONParser.S_IN_OBJECT;
						statusStack.push(this.status);
						
						if(!contentHandler.startObject()) {
							
							return;
						}
						
					} else if(this.token.type == Yytoken.TYPE_LEFT_SQUARE) {
						
						this.status = JSONParser.S_IN_ARRAY;
						statusStack.push(this.status);
						
						if(!contentHandler.startArray()) {
							
							return;
						}
						
					} else {
						
						this.status = JSONParser.S_IN_ERROR;
					}
					
				} else if(this.status == JSONParser.S_IN_FINISHED_VALUE) {
					
					this.nextToken();
					
					if(this.token.type == Yytoken.TYPE_EOF) {
						
						contentHandler.endJSON();
						status = JSONParser.S_END;
						return;
						
					} else {
						
						this.status = JSONParser.S_IN_ERROR;
					}
					
				} else if(this.status == JSONParser.S_IN_OBJECT) {
					
					this.nextToken();
					
					if(this.token.type == Yytoken.TYPE_VALUE) {
						
						if(this.token.value instanceof String) {
							
							String key = (String)this.token.value;
							this.status = JSONParser.S_PASSED_PAIR_KEY;
							statusStack.push(this.status);
							
							if(!contentHandler.startObjectEntry(key)) {
								
								return;
							}
							
						} else {
							
							this.status = JSONParser.S_IN_ERROR;
						}
						
					} else if(this.token.type == Yytoken.TYPE_RIGHT_BRACE) {
						
						if(statusStack.size() > 1) {
							
							statusStack.pop();
							this.status = (Integer)statusStack.peek();
							
						} else {
							
							this.status = JSONParser.S_IN_FINISHED_VALUE;
						}
						
						if(!contentHandler.endObject()) {
							
							return;
						}
						
					} else if(this.token.type != Yytoken.TYPE_COMMA) {
						
						this.status = JSONParser.S_IN_ERROR;
					}
					
				} else if(this.status == JSONParser.S_PASSED_PAIR_KEY) {
					
					this.nextToken();
					
					if(this.token.type == Yytoken.TYPE_VALUE) {
						
						statusStack.pop();
						this.status = (Integer)statusStack.peek();
						
						if(!contentHandler.primitive(this.token.value) || !contentHandler.endObjectEntry()) {
							
							return;
						}
						
					} else if(this.token.type == Yytoken.TYPE_LEFT_SQUARE) {
						
						statusStack.pop();
						statusStack.push(JSONParser.S_IN_PAIR_VALUE);
						this.status = JSONParser.S_IN_ARRAY;
						statusStack.push(this.status);
						
						if(!contentHandler.startArray()) {
							
							return;
						}
						
					} else if(this.token.type == Yytoken.TYPE_LEFT_BRACE) {
						
						statusStack.pop();
						statusStack.push(JSONParser.S_IN_PAIR_VALUE);
						this.status = JSONParser.S_IN_OBJECT;
						statusStack.push(this.status);
						
						if(!contentHandler.startObject()) {
							
							return;
						}
						
					} else if(this.token.type != Yytoken.TYPE_COLON) {
						
						this.status = JSONParser.S_IN_ERROR;
					}
					
				} else if(this.status == JSONParser.S_IN_PAIR_VALUE) {
					
					statusStack.pop();
					this.status = (Integer)statusStack.peek();
					
					if(!contentHandler.endObjectEntry()) {
						
						return;
					}
					
				} else if(this.status == JSONParser.S_IN_ARRAY) {
					
					this.nextToken();
					
					if(this.token.type == Yytoken.TYPE_VALUE) {
						
						if(!contentHandler.primitive(this.token.value)) {
							
							return;
						}
						
					} else if(this.token.type == Yytoken.TYPE_RIGHT_SQUARE) {
						
						if(statusStack.size() > 1) {
							
							statusStack.pop();
							this.status = (Integer)statusStack.peek();
							
						} else {
							
							this.status = JSONParser.S_IN_FINISHED_VALUE;
						}
						
						if(!contentHandler.endArray()) {
							
							return;
						}
						
					} else if(this.token.type == Yytoken.TYPE_LEFT_BRACE) {
						
						this.status = JSONParser.S_IN_OBJECT;
						statusStack.push(this.status);
						
						if(!contentHandler.startObject()) {
							
							return;
						}
						
					} else if(this.token.type == Yytoken.TYPE_LEFT_SQUARE) {
						
						this.status = JSONParser.S_IN_ARRAY;
						statusStack.push(this.status);
						
						if(!contentHandler.startArray()) {
							
							return;
						}
						
					} else if(this.token.type != Yytoken.TYPE_COMMA) {
						
						this.status = JSONParser.S_IN_ERROR;
					}
					
				} else if(this.status == JSONParser.S_END) {
					
					return;
				}
				
				if(this.status == JSONParser.S_IN_ERROR) {
					
					throw new JSONParseException(this.getPosition(), JSONParseException.ERROR_UNEXPECTED_TOKEN, this.token);
				}
				
			} while(this.token.type != Yytoken.TYPE_EOF);
		
		} 
		catch(IOException exception) {
			this.status = JSONParser.S_IN_ERROR;
			throw exception;
		}
		catch(JSONParseException exception) {
			this.status = JSONParser.S_IN_ERROR;
			throw exception;
		}
		catch(RuntimeException exception) {
			this.status = JSONParser.S_IN_ERROR;
			throw exception;
		}
		catch(Error exception) {
			this.status = JSONParser.S_IN_ERROR;
			throw exception;
		}
		
		this.status = JSONParser.S_IN_ERROR;
		throw new JSONParseException(this.getPosition(), JSONParseException.ERROR_UNEXPECTED_TOKEN, this.token);
	}
}

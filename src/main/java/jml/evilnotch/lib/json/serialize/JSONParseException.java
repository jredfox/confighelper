package jml.evilnotch.lib.json.serialize;

import jml.evilnotch.lib.json.internal.Yytoken;

/**
 * Is thrown when an error occurs while parsing JSON data.
 * Explains what the problem is.
 * @author FangYidong(fangyidong@yahoo.com.cn)
 * @author Ralph Niemitz/RalleYTN(ralph.niemitz@gmx.de)
 * @version 2.0.0
 * @since 1.0.0
 */
public class JSONParseException extends Exception {
	
	// ==== 11.03.2018 | Ralph Niemitz/RalleYTN(ralph.niemitz@gmx.de)
	// -	Updated the documentation
	// ====
	
	private static final long serialVersionUID = -7880698968187728547L;
	
	/** @since 1.0.0 */ public static final int ERROR_UNEXPECTED_CHAR = 0;
	/** @since 1.0.0 */ public static final int ERROR_UNEXPECTED_TOKEN = 1;
	/** @since 1.0.0 */ public static final int ERROR_UNEXPECTED_EXCEPTION = 2;

	private int errorType;
	private Object unexpectedObject;
	private int position;
	
	/**
	 * @param errorType the error type
	 * @since 1.0.0
	 */
	public JSONParseException(int errorType) {
		
		this(-1, errorType, null);
	}
	
	/**
	 * @param errorType the error type
	 * @param unexpectedObject the object that represents what was unexpected
	 * @since 1.0.0
	 */
	public JSONParseException(int errorType, Object unexpectedObject) {
		
		this(-1, errorType, unexpectedObject);
	}
	
	/**
	 * @param position the character position (starting with 0) of the input where the error occurred
	 * @param errorType the error type
	 * @param unexpectedObject the object that represents what was unexpected
	 * @since 1.0.0
	 */
	public JSONParseException(int position, int errorType, Object unexpectedObject) {
		
		this.position = position;
		this.errorType = errorType;
		this.unexpectedObject = unexpectedObject;
	}
	
	/**
	 * @return the error type
	 * @since 1.0.0
	 */
	public int getErrorType() {
		
		return errorType;
	}
	
	/**
	 * @return the character position (starting with 0) of the input where the error occurred
	 * @since 1.0.0
	 */
	public int getPosition() {
		
		return this.position;
	}
	
	/**
	 * @return one of the following base on the value of errorType:
	 * <table border=1>
	 * <tr>
	 * <th>error type</th>
	 * <th>object type</th>
	 * </tr>
	 * <tr>
	 * <td>{@link #ERROR_UNEXPECTED_CHAR}</td>
	 * <td>{@linkplain Character}</td>
	 * </tr>
	 * <tr>
	 * <td>{@link #ERROR_UNEXPECTED_TOKEN}</td>
	 * <td>{@linkplain Yytoken}</td>
	 * </tr>
	 * <tr>
	 * <td>{@link #ERROR_UNEXPECTED_EXCEPTION}</td>
	 * <td>{@linkplain Exception}</td>
	 * </tr>
	 * </table>
	 * @since 1.0.0
	 */
	public Object getUnexpectedObject() {
		
		return this.unexpectedObject;
	}
	
	@Override
	public String getMessage() {
		
		StringBuilder builder = new StringBuilder();
		
		switch(this.errorType) {
		
			case ERROR_UNEXPECTED_CHAR:
				builder.append("Unexpected character (").append(this.unexpectedObject).append(") at position ").append(this.position).append(".");
				break;
				
			case ERROR_UNEXPECTED_TOKEN:
				builder.append("Unexpected token ").append(this.unexpectedObject).append(" at position ").append(this.position).append(".");
				break;
				
			case ERROR_UNEXPECTED_EXCEPTION:
				builder.append("Unexpected exception at position ").append(this.position).append(": ").append(this.unexpectedObject);
				break;
			
			default:
				builder.append("Unkown error at position ").append(this.position).append(".");
				break;
		}
		
		return builder.toString();
	}
}

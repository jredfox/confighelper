package jml.evilnotch.lib.json.internal;

/**
 * Represents a token.
 * @author FangYidong(fangyidong@yahoo.com.cn)
 * @author Ralph Niemitz/RalleYTN(ralph.niemitz@gmx.de)
 * @version 2.0.0
 * @since 1.0.0
 */
public class Yytoken {
	
	/** @since 1.0.0 */ public static final int TYPE_VALUE = 0;
	/** @since 1.0.0 */ public static final int TYPE_LEFT_BRACE = 1;
	/** @since 1.0.0 */ public static final int TYPE_RIGHT_BRACE = 2;
	/** @since 1.0.0 */ public static final int TYPE_LEFT_SQUARE = 3;
	/** @since 1.0.0 */ public static final int TYPE_RIGHT_SQUARE = 4;
	/** @since 1.0.0 */ public static final int TYPE_COMMA = 5;
	/** @since 1.0.0 */ public static final int TYPE_COLON = 6;
	/** @since 1.0.0 */ public static final int TYPE_EOF = -1;
	
	public int type;
	public Object value;
	
	/**
	 * @param type the token type
	 * @param value the value of this token
	 * @since 1.0.0
	 */
	public Yytoken(int type, Object value) {
		
		this.type = type;
		this.value = value;
	}
	
	// ==== 17.03.2018 | Ralph Niemitz/RalleYTN(ralph.niemitz@gmx.de)
	// Since this is an internal class and no classes inside this library use this method, it is dead code.
	// ====
	
	/*@Override
	public String toString() {
		
		StringBuilder builder = new StringBuilder();
		
		// ==== 01.03.2018 | Ralph Niemitz/RalleYTN(ralph.niemitz@gmx.de)
		// Replaced switch-case with if-else because if-else is faster
		// ====
		
		       if(this.type == Yytoken.TYPE_VALUE) {builder.append("VALUE(").append(this.value).append(")");
		} else if(this.type == Yytoken.TYPE_LEFT_BRACE) {builder.append("LEFT BRACE({)");
		} else if(this.type == Yytoken.TYPE_RIGHT_BRACE) {builder.append("RIGHT BRACE(})");
		} else if(this.type == Yytoken.TYPE_LEFT_SQUARE) {builder.append("LEFT SQUARE([)");
		} else if(this.type == Yytoken.TYPE_RIGHT_SQUARE) {builder.append("RIGHT SQUARE(])");
		} else if(this.type == Yytoken.TYPE_COMMA) {builder.append("COMMA(,)");
		} else if(this.type == Yytoken.TYPE_COLON) {builder.append("COLON(:)");
		} else if(this.type == Yytoken.TYPE_EOF) {builder.append("END OF FILE");
		}

		return builder.toString();
	}*/
}

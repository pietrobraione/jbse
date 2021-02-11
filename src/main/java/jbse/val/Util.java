package jbse.val;

public final class Util {
    public static String asCharacterLiteral(char character) {
    	return "\'" + toLiteral(character) + "\'";
    }
    
    public static String asStringLiteral(String text) {
    	final StringBuilder retVal = new StringBuilder();
    	retVal.append('\"');
    	for (char c : text.toCharArray()) {
    		retVal.append(toLiteral(c));
    	}
    	retVal.append('\"');
    	return retVal.toString();
    }
    
    private static String toLiteral(char character) {
    	if (character == '\b') {
    		return "\\b";
    	} else if (character == '\t') {
    		return "\\t";
    	} else if (character == '\n') {
    		return "\\n";
    	} else if (character == '\f') {
    		return "\\f";
    	} else if (character == '\r') {
    		return "\\r";
    	} else if (character == '\"') {
    		return "\\\"";
    	} else if (character == '\'') {
    		return "\\\'";
    	} else if (character == '\\') {
    		return "\\\\";
    	} else if (character < ' ' || 
    	(character >= '\u007f' && character <= '\u00a0') ||
    	character == '\u00ad' || character > '\u00ff') {
    		return "\\u" + String.format("%04x", (int) character);
    	} else {
    		return "" + character;
    	}
    }

	/**
	 * Do not instantiate!
	 */
	private Util() {
		
	}
}

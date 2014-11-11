package jbse;

import java.util.ArrayList;

/**
 * Class that contain public constants and static functions concerning 
 * JVM types.
 */
public final class Type {
	/** Not a type. */
    public static final char ERROR          = '\0'; //not recognized as a type
    
    /** The top type, which fits any datum. */
    public static final char UNKNOWN        = '?';
    
    /** The type {@code void}. */
    public static final char VOID           = 'V';
    
    /** The type of string literals in the constant pool. */
    public static final char STRING_LITERAL = '$';
    
    /** The primitive type {@code byte}. */
    public static final char BYTE     		= 'B';
    
    /** The primitive type {@code short}. */
    public static final char SHORT    		= 'S';
    
    /** The primitive type {@code int}. */
    public static final char INT      		= 'I';
    
    /** The primitive type {@code long}. */
    public static final char LONG     		= 'J';
    
    /** The primitive type {@code float}. */
    public static final char FLOAT    		= 'F';
    
    /** The primitive type {@code double}. */
    public static final char DOUBLE   		= 'D';
    
    /** The primitive type {@code char}. */
    public static final char CHAR     		= 'C';
    
    /** The primitive type {@code boolean}. */
    public static final char BOOLEAN  		= 'Z';
    
    /** The type for all the references to instances of classes. */
    public static final char REFERENCE		= 'L';
    
    /** The type for {@code null}. */
    public static final char NULLREF  		= '0';
    
    /** The type for all the references to arrays. */
    public static final char ARRAYOF        = '[';
    
    /** This is not a type, but rather marks the end of reference-to-instances types. */
    public static final char TYPEEND        = ';';
    
    public static boolean isVoid(String s) {
    	if (s == null || s.length() == 0) {
    		return false;
    	}
    	char c = s.charAt(0);
    	return (c == Type.VOID);
    }
    
    public static boolean isPrimitive(String s) {
    	if (s == null || s.length() != 1) {
    		return false;
    	} else {
	    	char c = s.charAt(0);
	    	return isPrimitive(c);
    	}
    }
    
    public static boolean isPrimitiveFloating(char c) {
    	return (c == Type.FLOAT ||
                c == Type.DOUBLE);
    }
    
    public static boolean isPrimitiveIntegralOpStack(char c) {
    	return (c == Type.INT ||
                c == Type.LONG);
    }
    
    /**
     * Checks whether a type is a primitive type
     * of the kind that is actually stored in the operand stack.
     * 
     * @param c a {@char}.
     * @return {@code true} iff {@code 
	 *           c == Type.INT || c == Type.LONG ||      
	 *           c == Type.FLOAT || c == Type.DOUBLE}.
     */
    public static boolean isPrimitiveOpStack(char c) {
    	return (isPrimitiveFloating(c) || isPrimitiveIntegralOpStack(c));
    }
    
    /**
     * Checks whether a type is a primitive integral type.
     * 
     * @param c a {@char}.
     * @return {@code true} iff {@code c == Type.BYTE ||
	 *           c == Type.INT || c == Type.LONG ||      
	 *           c == Type.SHORT || c == Type.CHAR ||
     *           c == Type.BOOLEAN}.
     */
    public static boolean isPrimitiveIntegral(char c) {
    	return (isPrimitiveIntegralOpStack(c) ||
    			c == Type.BYTE ||
                c == Type.SHORT ||
                c == Type.CHAR ||
                c == Type.BOOLEAN);
    }
    
    public static boolean isPrimitive(char c) {
    	return (isPrimitiveFloating(c) || isPrimitiveIntegral(c));
    }
    
    public static boolean isArray(char c) {
    	return (c == Type.ARRAYOF);
    }
    
    public static boolean isArray(String s) {
    	if (s == null || s.length() == 0) {
    		return false;
    	} else {
	    	char c = s.charAt(0);
	    	return isArray(c);
    	}
    }
    
    public static boolean isReference(char c) {
    	return (c == Type.REFERENCE);
    }
    
    public static boolean isReference(String s) {
    	if (s == null || s.length() == 0) {
    		return false;
    	}
    	char c = s.charAt(0);
    	char cc = s.charAt(s.length() - 1);
    	return (isReference(c) && cc == Type.TYPEEND);
    }
    
    public static String className(String s) {
    	//if reference, remove the L...; 
    	//otherwise is an array, so just return it
    	return (Type.isReference(s) ? Type.getClassFromReferenceType(s) : s);
    }
    
    public static boolean isCat_1(char c) {
    	return (c != Type.LONG && c != Type.DOUBLE);
    	//note that UNKNOWN ha category 1 because DefaultValues must
    	//fill every slot
    }
	
    /**
     * Checks if a type narrows another one.
     * 
	 * @param to a {@code char} representing a type.
	 * @param from a {@code char} representing a type.
	 * @return {@code true} iff {@code to} narrows {@code from}.
     */
	public static boolean narrows(char to, char from) {
		return  (from == INT && (to == BYTE || to == SHORT || to == CHAR)) ||
				(from == LONG && to == INT) ||
				(from == FLOAT && (to == INT || to == LONG)) ||
				(from == DOUBLE && (to == INT || to == LONG || to == FLOAT));
	}
	
	/**
     * Checks if a type widens another one.
	 * 
	 * @param to a {@code char} representing a type.
	 * @param from a {@code char} representing a type.
	 * @return {@code true} iff {@code to} widens {@code from}.
	 */
	public static boolean widens(char to, char from) {
		return  (from == BOOLEAN && to == INT) || //this just because symbolic booleans must be converted to ints 
				(from == INT && (to == LONG || to == FLOAT || to == DOUBLE)) ||
				(from == LONG && (to == FLOAT || to == DOUBLE)) ||
				(from == FLOAT && to == DOUBLE);
	}
    
	/**
	 * Given a descriptor of a method returns an array of 
	 * {@link String}s containing the descriptors of its parameters.
	 * 
	 * @param methodDescriptor a {@link String}, the descriptor of a method.
	 * @return a {@link String}{@code []}, whose i-th
	 *         element is the descriptor of the method's i-th
	 *         parameter.
	 */
	public static String[] splitParametersDescriptors(String methodDescriptor){
		ArrayList<String> myVector = new ArrayList<String>();
		for (int j = 1; j < methodDescriptor.lastIndexOf(')'); j++) {
			if (methodDescriptor.charAt(j) == REFERENCE) {
				int z = j;
				while (methodDescriptor.charAt(j) != TYPEEND) {
					j++;
				}
				myVector.add(methodDescriptor.substring(z, j + 1));
			} else if (methodDescriptor.charAt(j) == ARRAYOF) {
				int z = j;
				while (methodDescriptor.charAt(j) == ARRAYOF) {
					j++;
				}
				if (methodDescriptor.charAt(j) == REFERENCE) {
					while (methodDescriptor.charAt(j) != TYPEEND) {
						j++;
					}
				}
				myVector.add(methodDescriptor.substring(z, j + 1));
			} else {
				myVector.add("" + methodDescriptor.charAt(j));
			}
		}
		String[] retString = new String[myVector.size()];
		for (int b = 0; b < myVector.size(); b++) {
			retString[b] = myVector.get(b);
		}
		return (retString);
	}

	/**
	 * Given a descriptor of a method returns a 
	 * {@link String} containing the descriptor of its return value, 
	 * or {@code null} if the input is not the descriptor of a method.
	 * 
	 * @param methodDescriptor a {@link String}, the descriptor of a method.
	 * @return a {@link String}, the descriptor of the method's 
	 *         return value.
	 */
	public static String splitReturnValueDescriptor(String methodDescriptor) {
		int index = methodDescriptor.lastIndexOf(')') + 1;
		if (index == 0) {
			return null;
		} else {
			return methodDescriptor.substring(index);
		}
	}
	
	public static String getClassFromReferenceType(String refType) {
		if (isReference(refType)) {
			return refType.substring(1, refType.length() - 1);
		} else {
			return null;
		}
	}
	
	/**
	 * Gets the type of the array member.
	 * 
	 * @param type A {@code String}, an array type.
	 * @return The substring from the character 1 
	 *         henceforth (i.e., skipping the initial '[').
	 */
	public static String getArrayMemberType(String type) {
		return type.substring(1);
	}
	
	/**
	 * Given the type of an array, it returns the (declared) 
	 * number of dimensions (e.g., fed by {@code [[[Z;} it returns
	 * 3),
	 * @param arrayType a {@link String}, the type of the array.
	 * @return the number of dimension as declared in {@code arrayType}, 
	 *         or an unspecified number if {@code arrayType} is not
	 *         an array type.
	 */
	public static int getDeclaredNumberOfDimensions(String arrayType) {
		int retVal = 0;
		while (retVal < arrayType.length() && arrayType.charAt(retVal) == ARRAYOF) {
			retVal++;
		}
		return retVal;
	}

	/**
	 * Returns the lower upper bound of two types.
	 * 
	 * @param firstType a type.
	 * @param secondType another type.
	 * @return the smallest type that may contain the values of both 
	 *         {@code first} and {@code second}. Does behave well
	 *         only when the two types are both integral or both 
	 *         floating or both reference. If it cannot decide it returns
	 *         {@link Type#ERROR}.
	 */
	public static char lub(char firstType, char secondType) {
		if (isReference(firstType) && isReference(secondType)) {
			return REFERENCE;
		}
	    if (firstType == DOUBLE || secondType == DOUBLE) {
	        return DOUBLE;
	    } 
	    if (firstType == LONG || secondType == LONG) {
	        return LONG;
	    } 
	    if (firstType == FLOAT || secondType == FLOAT) {
	        return FLOAT;
	    } 
	    if (firstType == INT || secondType == INT) {
	        return INT;
	    } 
	    if (firstType == CHAR || secondType == CHAR) {
	        return CHAR;
	    } 
	    if (firstType == SHORT || secondType == SHORT) {
	        return SHORT;
	    } 
	    if (firstType == BYTE || secondType == BYTE) {
	        return BYTE;
	    } 
	    if (firstType == BOOLEAN || secondType == BOOLEAN) {
	        return BOOLEAN;
	    } 
	    return ERROR;
	}
	
	/**
	 * Do not instantiate it!
	 */
	private Type() { }
}

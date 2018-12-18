package jbse.common;

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

    /** The type of symbolic references to a class in the constant pool. */
    public static final char CLASS_SYMREF   = '%';

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

    /** 
     * This is not a type, but rather marks the end of
     * reference-to-instances types. 
     */
    public static final char TYPEEND        = ';';

    public static boolean isVoid(String s) {
        if (s == null || s.length() != 1) {
            return false;
        }
        final char c = s.charAt(0);
        return (c == Type.VOID);
    }

    /**
     * Checks whether a type is a primitive floating point type.
     * 
     * @param type a {@code char}.
     * @return {@code true} iff {@code 
     *           type ==  }{@link #FLOAT}{@code  || type ==  }{@link #DOUBLE}.
     */
    public static boolean isPrimitiveFloating(char type) {
        return (type == Type.FLOAT || type == Type.DOUBLE);
    }

    /**
     * Checks whether a type is a primitive integral type
     * of the kind that is actually stored in the operand stack.
     * 
     * @param type a {@code char}.
     * @return {@code true} iff {@code 
     *           type ==  }{@link #INT}{@code  || type ==  }{@link #LONG}.
     */
    public static boolean isPrimitiveIntegralOpStack(char type) {
        return (type == Type.INT || type == Type.LONG);
    }

    /**
     * Checks whether a type is a primitive type
     * of the kind that is actually stored in the operand stack.
     * 
     * @param type a {@code char}.
     * @return {@code true} iff {@code 
     *           type ==  }{@link #INT}{@code  || type ==  }{@link #LONG}{@code  ||
     *           type ==  }{@link #FLOAT}{@code  || type ==  }{@link #DOUBLE}.
     */
    public static boolean isPrimitiveOpStack(char type) {
        return (isPrimitiveFloating(type) || isPrimitiveIntegralOpStack(type));
    }

    /**
     * Checks whether a type is a primitive integral type.
     * 
     * @param type a {@code char}.
     * @return {@code true} iff {@code type ==  }{@link #BYTE}{@code  ||
     *           type ==  }{@link #INT}{@code  || type ==  }{@link #LONG}{@code  ||
     *           type ==  }{@link #SHORT}{@code  || type ==  }{@link #CHAR}{@code  ||
     *           type ==  }{@link #BOOLEAN}.
     */
    public static boolean isPrimitiveIntegral(char type) {
        return (isPrimitiveIntegralOpStack(type) ||
        type == Type.BYTE ||
        type == Type.SHORT ||
        type == Type.CHAR ||
        type == Type.BOOLEAN);
    }

    /**
     * Checks whether a type is a primitive type.
     * 
     * @param type a {@code char}.
     * @return {@code true} iff {@code type ==  }{@link #BYTE}{@code  ||
     *           type ==  }{@link #INT}{@code  || type ==  }{@link #LONG}{@code  ||
     *           type ==  }{@link #SHORT}{@code  || type ==  }{@link #CHAR}{@code  ||
     *           type ==  }{@link #BOOLEAN}{@code  ||
     *           type ==  }{@link #FLOAT}{@code  || type ==  }{@link #DOUBLE}.
     */
    public static boolean isPrimitive(char type) {
        return (isPrimitiveFloating(type) || isPrimitiveIntegral(type));
    }

    /**
     * Checks whether a type is a primitive type.
     * 
     * @param type a {@code char}.
     * @return same as {@link #isPrimitive(char) isPrimitive}{@code (type.charAt(0))}
     *         when {@code type.}{@link String#length() length()}{@code  == 1},
     *         otherwise {@code false}.
     */
    public static boolean isPrimitive(String type) {
        if (type == null || type.length() != 1) {
            return false;
        } else {
            final char c = type.charAt(0);
            return isPrimitive(c);
        }
    }

    public static boolean isArray(char type) {
        return (type == Type.ARRAYOF);
    }

    public static boolean isArray(String type) {
        if (type == null || type.length() < 2) { //at least [ + single char
            return false;
        } else {
            final char c = type.charAt(0);
            return isArray(c);
        }
    }

    public static boolean isReference(char c) {
        return (c == Type.REFERENCE);
    }

    public static boolean isReference(String type) {
        if (type == null || type.length() < 3) { //at least L + single char + ;
            return false;
        }
        final char c = type.charAt(0);
        final char cc = type.charAt(type.length() - 1);
        return (isReference(c) && cc == Type.TYPEEND);
    }

    private static String getReferenceClassName(String type) {
        if (isReference(type)) {
            return type.substring(1, type.length() - 1);
        } else {
            return null;
        }
    }

    public static String className(String type) {
        //if reference, remove REFERENCE and TYPEEND; 
        //if array, just return it
        return (isReference(type) ? getReferenceClassName(type) : 
            isArray(type) ? type : null);
    }

    /**
     * Checks whether its parameter is the canonical name
     * of a primitive class (including {@link java.lang.Void#TYPE})
     * 
     * @param type a {@link String}.
     * @return {@code true} iff {@code type} {@link Object#equals(Object) equals} one of 
     * {@code "byte"},
     * {@code "short"},
     * {@code "int"},
     * {@code "long"},
     * {@code "boolean"},
     * {@code "char"},
     * {@code "float"},
     * {@code "double"}, or
     * {@code "void"}.
     */
    public static boolean isPrimitiveOrVoidCanonicalName(String type) {
        return ("byte".equals(type) ||
                "short".equals(type) ||
                "int".equals(type) ||
                "long".equals(type) ||
                "boolean".equals(type) ||
                "char".equals(type) ||
                "float".equals(type) ||
                "double".equals(type) ||
                "void".equals(type));
    }

    public static String binaryClassName(String className) {
        return (className == null ? null : className.replace('/', '.'));
    }

    public static String internalClassName(String className) {
        return (className == null ? null : className.replace('.', '/'));
    }

    /**
     * Converts the internal name of a primitive type  
     * or void to its corresponding canonical name.
     * 
     * @param primitiveTypeInternal a {@code char}.
     * @return a {@link String}, the canonical name for
     *        {@code primitiveTypeInternal}, or {@code null}
     *        if {@code primitiveTypeInternal} is not the
     *        internal name of a primitive type or void.
     */
    public static String toPrimitiveOrVoidCanonicalName(char primitiveTypeInternal) {
        if (primitiveTypeInternal == BYTE) {
            return "byte";
        } else if (primitiveTypeInternal == SHORT) {
            return "short";
        } else if (primitiveTypeInternal == INT) {
            return "int";   
        } else if (primitiveTypeInternal == LONG) {
            return "long";
        } else if (primitiveTypeInternal == BOOLEAN) {
            return "boolean";
        } else if (primitiveTypeInternal == CHAR) {
            return "char";
        } else if (primitiveTypeInternal == FLOAT) {
            return "float";
        } else if (primitiveTypeInternal == DOUBLE) {
            return "double";
        } else if (primitiveTypeInternal == VOID) {
            return "void";
        } else {
            return null;
        }
    }

    /**
     * Converts the internal name of a primitive type  
     * or void to its corresponding canonical name.
     * 
     * @param primitiveTypeInternal a {@link String}.
     * @return same as 
     *         {@link #toPrimitiveOrVoidCanonicalName(char) toPrimitiveOrVoidCanonicalName}{@code (primitiveTypeInternal.}
     *         {@link String#charAt(int) charAt}{@code (0))} if 
     *         {@link #isPrimitive(String) isPrimitive}{@code (primitiveTypeInternal) || }{@link #isVoid(String) isVoid}{@code (primitiveTypeInternal)},
     *         otherwise {@code null}.
     */
    public static String toPrimitiveOrVoidCanonicalName(String primitiveTypeInternal) {
        if (isPrimitive(primitiveTypeInternal) || isVoid(primitiveTypeInternal)) {
            return toPrimitiveOrVoidCanonicalName(primitiveTypeInternal.charAt(0));
        } else {
            return null;
        }
    }

    /**
     * Converts the internal name of a primitive type  
     * (or of the void type) to its corresponding canonical name.
     * 
     * @param primitiveTypeCanonical a {@link String}, the canonical
     *        name of a primitive type or void.
     * @return a {@code char}, the internal name for
     *         {@code primitiveTypeCanonical}, or
     *         {@link #ERROR} if {@code primitiveTypeCanonical} is not the
     *         canonical name of a primitive type.
     */
    public static char toPrimitiveOrVoidInternalName(String primitiveTypeCanonical) {
        if ("byte".equals(primitiveTypeCanonical)) {
            return BYTE;
        } else if ("short".equals(primitiveTypeCanonical)) {
            return SHORT;
        } else if ("int".equals(primitiveTypeCanonical)) {
            return INT;   
        } else if ("long".equals(primitiveTypeCanonical)) {
            return LONG;
        } else if ("boolean".equals(primitiveTypeCanonical)) {
            return BOOLEAN;
        } else if ("char".equals(primitiveTypeCanonical)) {
            return CHAR;
        } else if ("float".equals(primitiveTypeCanonical)) {
            return FLOAT;
        } else if ("double".equals(primitiveTypeCanonical)) {
            return DOUBLE;
        } else if ("void".equals(primitiveTypeCanonical)) {
            return VOID;
        } else {
            return ERROR;
        }
    }

    public static boolean isCat_1(char c) {
        return (c != Type.LONG && c != Type.DOUBLE);
        //note that UNKNOWN ha category 1 because DefaultValues 
        //must be able to fill single slots
    }

    /**
     * Checks if a type narrows another one.
     * 
     * @param to a {@code char} representing a type.
     * @param from a {@code char} representing a type.
     * @return {@code true} iff {@code to} narrows {@code from}.
     */
    public static boolean narrows(char to, char from) {
        return (from == DOUBLE && (to == INT || to == LONG || to == FLOAT)) ||
               (from == FLOAT && (to == INT || to == LONG)) ||
               (from == LONG && to == INT) ||
               //this is for bastore, castore and sastore
               (from == INT && (to == BOOLEAN || to == BYTE || to == SHORT || to == CHAR));
    }

    /**
     * Checks if a type widens another one.
     * 
     * @param to a {@code char} representing a type.
     * @param from a {@code char} representing a type.
     * @return {@code true} iff {@code to} widens {@code from}.
     */
    public static boolean widens(char to, char from) {
        return  (from == INT && (to == LONG || to == FLOAT || to == DOUBLE)) ||
                (from == LONG && (to == FLOAT || to == DOUBLE)) ||
                (from == FLOAT && to == DOUBLE) ||
                //this is for baload, caload and saload
                (from == BOOLEAN && to == INT) || //also for Algo_XCMPY opstack trick 
                (from == BYTE && to == INT) ||
                (from == CHAR && to == INT) ||
                (from == SHORT && to == INT);
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
                final int z = j;
                while (methodDescriptor.charAt(j) != TYPEEND) {
                    j++;
                }
                myVector.add(methodDescriptor.substring(z, j + 1));
            } else if (methodDescriptor.charAt(j) == ARRAYOF) {
                final int z = j;
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
        final String[] retString = new String[myVector.size()];
        for (int b = 0; b < myVector.size(); b++) {
            retString[b] = myVector.get(b);
        }
        return (retString);
    }
    
    /**
     * Returns the number of effective parameters of a method.
     * 
     * @param methodDescriptor a {@link String}, the descriptor of a method.
     * @param isStatic a {@code boolean}, {@code true} iff the method is static.
     * @return an {@code int}.
     */
    public static int parametersNumber(String methodDescriptor, boolean isStatic) {
        final String[] paramsDescriptors = splitParametersDescriptors(methodDescriptor);
        return (isStatic ? paramsDescriptors.length : paramsDescriptors.length + 1);
    }

    /**
     * Given a descriptor of a method returns a 
     * {@link String} containing the descriptor of its return value, 
     * or {@code null} if the input is not the descriptor of a method.
     * 
     * @param methodDescriptor a {@link String}, the descriptor of a method.
     * @return a {@link String}.
     */
    public static String splitReturnValueDescriptor(String methodDescriptor) {
        final int index = methodDescriptor.lastIndexOf(')') + 1;
        if (index == 0) {
            return null;
        } else {
            return methodDescriptor.substring(index);
        }
    }

    /**
     * Gets the type of the array member.
     * 
     * @param type A {@code String}, an array type.
     * @return The substring from the character 1 
     *         henceforth (i.e., skipping the initial '['), 
     *         or {@code null} if {@code type} is not
     *         an array type.
     */
    public static String getArrayMemberType(String type) {
        if (isArray(type)) {
            return type.substring(1);
        } else {
            return null;
        }
    }

    /**
     * Given the type of an array, it returns the (declared) 
     * number of dimensions (e.g., fed by {@code [[[Z} it returns
     * 3).
     * 
     * @param arrayType a {@link String}, the type of the array.
     * @return the number of dimension as declared in {@code arrayType}, 
     *         or an unspecified number if {@code arrayType} is not
     *         an array type.
     */
    public static int getDeclaredNumberOfDimensions(String arrayType) {
        int retVal = 0;
        while (retVal < arrayType.length() && 
        arrayType.charAt(retVal) == ARRAYOF) {
            retVal++;
        }
        return retVal;
    }

    /**
     * Returns the lower upper bound of two types.
     * 
     * @param firstType a primitive type.
     * @param secondType another primitive type.
     * @return the smallest type that may contain the values of both 
     *         {@code first} and {@code second}. If {@code firstType} 
     *         or {@code secondType} does not denote a primitive type, 
     *         it returns {@link Type#ERROR}.
     */
    public static char lub(char firstType, char secondType) {
        if (!isPrimitive(firstType) || !isPrimitive(secondType)) {
            return ERROR;
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
        return BOOLEAN; //firstType == BOOLEAN && secondType == BOOLEAN
    }

    /**
     * Do not instantiate it!
     */
    private Type() {
        throw new AssertionError();
    }
}

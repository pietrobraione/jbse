package jbse.bc;

import java.util.List;

import jbse.bc.exc.AttributeNotFoundException;
import jbse.bc.exc.FieldNotFoundException;
import jbse.bc.exc.InvalidIndexException;
import jbse.bc.exc.MethodCodeNotFoundException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.common.Type;

/**
 * Abstract class for managing the information on a single 
 * class file.
 */
public abstract class ClassFile {
	protected static final String JAR_FILE_EXTENSION = ".jar";
	
	public abstract String getSourceFile();
	
    /**
     * Returns the name of the package where this class has been declared.
     * 
     * @return the package name of this class as a {@link String}.
     */
	public String getPackageName() {
        final String className = getClassName();
        int lastSlash = className.lastIndexOf('/');
        if (lastSlash == -1) {
            return "";
        } else {
            return className.substring(0, lastSlash);
        }
	}
	
    /**
     * Test whether the class is an array.
     * 
     * @return {@code true} iff the class is an interface.
     */
    public abstract boolean isArray();
    
    /**
     * Tests whether the class is primitive.
     * 
     * @return {@code true} iff the class is primitive.
     */
    public abstract boolean isPrimitive();

	/**
     * Test whether the class is an interface.
     * 
     * @return {@code true} iff the class is an interface.
     */
    public abstract boolean isInterface();
    
    /**
     * Tests whether the class is abstract.
     * 
     * @return {@code true} iff the class is abstract.
     */
    public abstract boolean isAbstract();

    /**
     * Tests whether the class has public visibility.
     * 
     * @return {@code true} iff the class is public.
     */
    public abstract boolean isPublic();

    /**
     * Tests whether the class has package visibility.
     * 
     * @return {@code true} iff the class has package visibility.
     */
    public abstract boolean isPackage();
    
    /**
     * Tests the {@code invokespecial} bytecode semantics required
     * for this class.
     *  
     * @return {@code true} if the ACC_SUPER flag of the class 
     * is set, i.e., if the class requires modern semantics,
     * {@code false} if the class requires backward-compatible 
     * semantics.  
     */
    public abstract boolean isSuperInvoke();

    /**
     * Tests whether this class is nested.
     * 
     * @return {@code true} iff this class is nested.
     */
	public abstract boolean isNested();
	
	/**
	 * Tests whether this class is inner to another one.
	 * 
	 * @param external another {@code ClassFile}.
	 * @return {@code true} iff this class is an inner class
	 *         of {@code external}.
	 */
	public final boolean isInner(ClassFile external) {
		/*
		 * TODO this implementation is very partial (a nested class can be declared 
		 * inside another one, nested class may also be anonymous, ...).
		 */
		return (this.isNested() && 
				!this.isStatic() && 
				this.classContainer().equals(external.getClassName()));
	}
	
	/**
	 * Returns the name of the class containing this class.
	 * 
	 * @return A {@link String}, the name of the class containing
	 *         this class; it has an unspecified effect in case
	 *         this class is not nested.
	 */
	public abstract String classContainer();

	/**
     * Tests whether the class is static.
     * 
     * @return {@code true} iff the class is static.
     */
	public abstract boolean isStatic();
	
    /**
     * Tests whether the class has an implementation (i.e., a declaration 
     * plus bytecode) for a method with a given signature. 
     * 
     * @param methodSignature the {@link Signature} of the method to be searched. 
     *                        Only the name of the method and its descriptor 
     *                        are considered (the signature's class name 
     *                        is ignored).
     * @return {@code true} if the class has an implementation for 
     *         the method with signature {@code methodSignature}, 
     *         {@code false} otherwise.
     */
    public abstract boolean hasMethodImplementation(Signature methodSignature);
    
    /**
     * Tests whether the class has a declaration for a method with a given 
     * signature. 
     * 
     * @param methodSignature the {@link Signature} of the method to be searched. 
     *                        Only the name of the method and its descriptor 
     *                        are considered (the signature's class name 
     *                        is ignored).
     * @return {@code true} iff the class declares a 
     *         method with signature {@code methodSignature}.
     */
    public abstract boolean hasMethodDeclaration(Signature methodSignature);
    
    /**
     * Tests whether a method in the class is declared abstract.
     * 
     * @param methodSignature the signature of the method to be checked.
     * @return {@code true} iff the method is abstract.
     * @throws MethodNotFoundException iff {@link #hasMethodDeclaration}{@code (methodSignature) == false}.
     */
    public abstract boolean isMethodAbstract(Signature methodSignature) throws MethodNotFoundException;
    
    /**
     * Tests whether a method in the class is declared static.
     * 
     * @param methodSignature the {@link Signature} of the method to be checked.
     * @return {@code true} iff the method is static.
     * @throws MethodNotFoundException iff {@link #hasMethodDeclaration}{@code (methodSignature) == false}.
     */
    public abstract boolean isMethodStatic(Signature methodSignature) throws MethodNotFoundException;
    
    /**
     * Tests whether a method in the class is declared public.
     * 
     * @param methodSignature the {@link Signature} of the method to be checked.
     * @return {@code true} iff the method is public.
     * @throws MethodNotFoundException iff {@link #hasMethodDeclaration}{@code (methodSignature) == false}.
     */
    public abstract boolean isMethodPublic(Signature methodSignature) throws MethodNotFoundException;
    
    /**
     * Tests whether a method in the class is declared protected.
     * 
     * @param methodSignature the {@link Signature} of the method to be checked.
     * @return {@code true} if the method is protected.
     * @throws MethodNotFoundException iff {@link #hasMethodDeclaration}{@code (methodSignature) == false}.
     */
    public abstract boolean isMethodProtected(Signature methodSignature) throws MethodNotFoundException;
    
    /**
     * Tests whether a method in the class is declared with package visibility.
     * 
     * @param methodSignature the {@link Signature} of the method to be checked.
     * @return {@code true} iff the method has package visibility.
     * @throws MethodNotFoundException iff {@link #hasMethodDeclaration}{@code (methodSignature) == false}.
     */
    public abstract boolean isMethodPackage(Signature methodSignature) throws MethodNotFoundException;
    
    /**
     * Tests whether a method in the class is declared private.
     * 
     * @param methodSignature the {@link Signature} of the method to be checked.
     * @return {@code true} if the method is private.
     * @throws MethodNotFoundException iff {@link #hasMethodDeclaration}{@code (methodSignature) == false}.
     */
    public abstract boolean isMethodPrivate(Signature methodSignature) throws MethodNotFoundException;
    
    /**
     * Tests whether a method in the class is declared native.
     * 
     * @param methodSignature the {@link Signature} of the method to be checked.
     * @return {@code true} iff the method is native.
     * @throws MethodNotFoundException iff {@link #hasMethodDeclaration}{@code (methodSignature) == false}.
     */
    public abstract boolean isMethodNative(Signature methodSignature) throws MethodNotFoundException;
    
    /**
     * Returns all the annotations of a method that are available on the current classpath.
     * 
     * @param methodSignature the {@link Signature} of a method.
     * @return an {@link Object}{@code []} containing all the annotations of the method.
     * @throws MethodNotFoundException iff {@link #hasMethodDeclaration}{@code (methodSignature) == false}.
     */
    public abstract Object[] getMethodAvailableAnnotations(Signature methodSignature) 
    throws MethodNotFoundException;
    
    /**
     * Given the signature of a method, returns its exception table.
     * 
     * @param MethodSignature the {@link Signature} of a method.
     * @return the {@link ExceptionTable} of the method (empty in the case 
     *         the method has no exception handler).
     * @throws MethodNotFoundException iff {@link #hasMethodDeclaration}{@code (methodSignature) == false}.
     * @throws MethodCodeNotFoundException iff the method has not the Code attribute.
     * @throws InvalidIndexException iff the exception type field in a row of the exception table 
     *         does not contain the index of a valid CONSTANT_Class in the class constant pool.
     */
    public abstract ExceptionTable getExceptionTable(Signature methodSignature) 
    throws MethodNotFoundException, MethodCodeNotFoundException, InvalidIndexException;
    
    /**
     * Given the signature of a method, returns a local variable table for that method.
     * 
     * @param methodSignature the structure that contains the signature of a method.
     * @return a {@link LocalVariableTable} for the method.
     * @throws MethodNotFoundException iff {@link #hasMethodDeclaration}{@code (methodSignature) == false}.
     * @throws MethodCodeNotFoundException iff the method has not the Code attribute.
     */
    public abstract LocalVariableTable getLocalVariableTable(Signature methodSignature) 
    throws MethodNotFoundException, MethodCodeNotFoundException;    

    /**
     * Returns a method's {@link LineNumberTable}. 
     * 
     * @param methodSignature a method's {@link Signature}.
     * @return the {@link LineNumberTable} for the method with signature {@code methodSignature}.
     * @throws MethodNotFoundException iff {@link #hasMethodDeclaration}{@code (methodSignature) == false}.
     * @throws MethodCodeNotFoundException iff the method has not the Code attribute.
     */
    public abstract LineNumberTable getLineNumberTable(Signature methodSignature) 
    throws MethodNotFoundException, MethodCodeNotFoundException;    

    /**
     * Returns a value for the passed index of constant pool.
     * 
     * @throws InvalidIndexException iff the constant pool has less entries than {@code index}.
     */
    public abstract ConstantPoolValue getValueFromConstantPool(int index) throws InvalidIndexException;

    /**
     * Given a CONSTANT_Methodref index in the constant pool, returns the array 
     * of byte code; it is equivalent to 
     * {@code getMethodCodeBySignature(getMethodSignature(methodRef))}.
     * @param methodRef the CONSTANT_Methodref of searched method
     * @return a {@code byte[]} containing the method's byte code.
     * @throws InvalidIndexException iff {@code methodRef} is not the index of a valid 
     *         CONSTANT_MethodRef in the class constant pool.
     * @throws MethodNotFoundException iff 
     * {@link #hasMethodDeclaration}{@code (}{@link #getMethodSignature}{@code (methodRef)) == false}.
     * @throws MethodCodeNotFoundException iff the method has not the Code attribute.
     */
    public final byte[] getMethodCodeByMethodRef(int methodRef) 
    throws InvalidIndexException, MethodNotFoundException, MethodCodeNotFoundException {
        return getMethodCodeBySignature(getMethodSignature(methodRef));
    }
    
    /**
     * Given the signature of a method, returns the bytecode of 
     * the method. 
     * 
     * @param methodSignature the {@link Signature} of a method.
     * @return a {@code byte[]} containing the bytecode.
     * @throws MethodNotFoundException iff {@link #hasMethodDeclaration}{@code (methodSignature) == false}.
     * @throws MethodCodeNotFoundException iff the method has not the Code attribute.
     */
    public abstract byte[] getMethodCodeBySignature(Signature methodSignature) 
    throws MethodNotFoundException, MethodCodeNotFoundException;
    
    /**
     * Checks whether the class declares a field.
     * 
     * @param fieldSignature the {@link Signature} of a method. The 
     * class of the signature is ignored.
     * @return {@code true} iff the class declares a field with  
     * {@code fieldSignature}'s name and type.
     */
    public abstract boolean hasFieldDeclaration(Signature fieldSignature);
    
    /**
     * Tests whether a field in the class is declared final.
     * 
     * @param fieldSignature the {@link Signature} of the field to be checked.
     * @return {@code true} iff the field is final.
     * @throws FieldNotFoundException iff {@link #hasFieldDeclaration}{@code (fieldSignature) == false}.
     */
    public abstract boolean isFieldFinal(Signature fieldSignature) throws FieldNotFoundException;
    
    
    /**
     * Tests whether a field in the class is declared public.
     * 
     * @param fieldSignature the {@link Signature} of the field to be checked.
     * @return {@code true} iff the field is public.
     * @throws FieldNotFoundException iff {@link #hasFieldDeclaration}{@code (fieldSignature) == false}.
     */
	public abstract boolean isFieldPublic(Signature fieldSignature) throws FieldNotFoundException;
    
    /**
     * Tests whether a field in the class is declared protected.
     * 
     * @param fieldSignature the {@link Signature} of the field to be checked.
     * @return {@code true} iff the field is protected.
     * @throws FieldNotFoundException iff {@link #hasFieldDeclaration}{@code (fieldSignature) == false}.
     */
	public abstract boolean isFieldProtected(Signature fieldSignature) throws FieldNotFoundException;	
    
    /**
     * Tests whether a field in the class is declared with package visibility.
     * 
     * @param fieldSignature the {@link Signature} of the field to be checked.
     * @return {@code true} iff the field has package visibility.
     * @throws FieldNotFoundException iff {@link #hasFieldDeclaration}{@code (fieldSignature) == false}.
     */
	public abstract boolean isFieldPackage(Signature fieldSignature) throws FieldNotFoundException;
	
    /**
     * Tests whether a field in the class is declared private.
     * 
     * @param fieldSignature the {@link Signature} of the field to be checked.
     * @return {@code true} iff the field is private.
     * @throws FieldNotFoundException iff {@link #hasFieldDeclaration}{@code (fieldSignature) == false}.
     */
	public abstract boolean isFieldPrivate(Signature fieldSignature) throws FieldNotFoundException;
	
    /**
     * Tests whether a field in the class is declared static.
     * 
     * @param fieldSignature the {@link Signature} of the field to be checked.
     * @return {@code true} iff the field is static.
     * @throws FieldNotFoundException iff {@link #hasFieldDeclaration}{@code (fieldSignature) == false}.
     */
	public abstract boolean isFieldStatic(Signature fieldSignature) throws FieldNotFoundException;

    /**
     * Tests whether a field in the class has a ConstantValue attribute.
     * 
     * @param fieldSignature the {@link Signature} of the field to be checked.
     * @return {@code true} iff the field is constant.
     * @throws FieldNotFoundException iff {@link #hasFieldDeclaration}{@code (fieldSignature) == false}.
     */
	public abstract boolean hasFieldConstantValue(Signature fieldSignature) throws FieldNotFoundException;
	
    /**
     * Tests whether a field in the class is both final and has a ConstantValue attribute.
     * 
     * @param fieldSignature the {@link Signature} of the field to be checked.
     * @return {@code true} iff the field is both final and constant.
     * @throws FieldNotFoundException iff {@link #hasFieldDeclaration}{@code (fieldSignature) == false}.
     */
	public final boolean isFieldConstant(Signature fieldSignature) throws FieldNotFoundException {
		return isFieldFinal(fieldSignature) && hasFieldConstantValue(fieldSignature);
	}

    /**
     * Returns the value of a constant field.
     * 
     * @param fieldSignature the {@link Signature} of the field.
     * @return its value as an {@link Object}.
     * @throws FieldNotFoundException iff {@link #hasFieldDeclaration}{@code (fieldSignature) == false}.
     * @throws AttributeNotFoundException iff {@link #hasFieldConstantValue}{@code (fieldSignature) == false}.
     * @throws InvalidIndexException iff the access to the constant pool fails.
     */
	public final ConstantPoolValue fieldConstantValue(Signature fieldSignature) 
	throws FieldNotFoundException, AttributeNotFoundException, InvalidIndexException {
	    final int index = fieldConstantValueIndex(fieldSignature);
		final ConstantPoolValue retVal = getValueFromConstantPool(index);
		if (retVal instanceof ConstantPoolClass) {
		    throw new InvalidIndexException(entryInvalidMessage(index));
		}
		return retVal;
	}


    /**
     * Returns the index in the constant pool where the value of a constant field is found.
     * 
     * @param fieldSignature the {@link Signature} of the field.
     * @return the constant pool index of its value as an {@code int}.
     * @throws FieldNotFoundException iff {@link #hasFieldDeclaration}{@code (fieldSignature) == false}.
     * @throws AttributeNotFoundException iff {@link #hasFieldConstantValue}{@code (fieldSignature) == false}.
     */
	public abstract int fieldConstantValueIndex(Signature fieldSignature) 
	throws FieldNotFoundException, AttributeNotFoundException;

	/**
     * Gets all the nonstatic (instance) fields declared by this class 
     * (not by its superclasses).
     *  
     * @return an array of {@link Signature}, one for each 
     *         nonstatic field declared in the class.
     */
    public abstract Signature[] getFieldsNonStatic();
    
    /**
     * Gets all the static (class) fields declared by this class 
     * (not by its superclasses).
     *  
     * @return an array of {@link Signature}s, one for each 
     *         static field declared in the class.
     */
    public abstract Signature[] getFieldsStatic();
    
    /**
     * Given an index of the constant pool of CONSTANT_FieldRef type, returns the signature of the field.
     * 
     * @param fieldRef a CONSTANT_Fieldref value in the constant pool.
     * @return the {@link Signature} of a field.
     * @throws InvalidIndexException iff {@code fieldRef} is not the index of a valid CONSTANT_FieldRef
     *         in the class constant pool.
     */
    public abstract Signature getFieldSignature(int fieldRef) throws InvalidIndexException;
    
    /**
     * Given an index of the constant pool of CONSTANT_MethodRef type, returns the signature of the Method
     * @param methodRef a CONSTANT_Methodref of searched field
     * @return the {@link Signature} of a method.
     * @throws InvalidIndexException iff {@code methodRef} is not the index of a valid CONSTANT_MethodRef
     *         in the class constant pool.
     */
    public abstract Signature getMethodSignature(int methodRef) throws InvalidIndexException;
    
    
    /**
     * Returns all the signatures of the methods declared in the class.
     * 
     * @return a {@link Signature}{@code []}.
     */
	public abstract Signature[] getMethodSignatures();

    /**
     * Given an index of the constant pool of CONSTANT_InterfaceMethodRef type, returns the signature of the Method
     * @param methodRef a CONSTANT_InferfaceMethodref of searched field
     * @return the {@link Signature} of a method.
     * @throws InvalidIndexException iff {@code methodRef} is not the index of a valid CONSTANT_InterfaceMethodRef
     *         in the class constant pool.
     */
    public abstract Signature getInterfaceMethodSignature(int methodRef) throws InvalidIndexException;
    
    /**
     * Given an index of the constant table of CONSTANT_ClassRef type, returns the signature 
     * of the class.
     * 
     * @param classRef the CONSTANT_ClassRef of searched class.
     * @return a {@link String}, the name of a class.
     * @throws InvalidIndexException iff {@code classRef} is not the index of a valid CONSTANT_ClassRef
     *         in the class constant pool.
     */
    public abstract String getClassSignature(int classRef) throws InvalidIndexException;
    
    /**
     * Returns the name of the superclass.
     * 
     * @return the name of the superclass as a {@link String}, or {@code null}
     * in the case the class has no superclass (e.g., {@code java.lang.Object}, 
     * primitive classes, bad classfiles).
     */
    public abstract String getSuperClassName();
    
    /**
     * Returns the list of the names of the superinterfaces.
     * 
     * @return an immutable {@link List}{@code <}{@link String}{@code >} 
     *         containing all the names of the superinterfaces of this 
     *         class (empty if none).
     */
    public abstract List<String> getSuperInterfaceNames();
    
    /**
     * Returns the name of the class.
     * 
     * @return the name of the class.
     */
    public abstract String getClassName();

    /**
     * Returns the length of the local variable table of a method.
     * 
     * @param methodSignature a method's {@link Signature}.
     * @return a {@code int} representing the length in bytes of the 
     *         local variable table of the method with signature 
     *         {@code methodSignature}, in the case such method exists 
     *         in the class and has code.
     * @throws MethodNotFoundException iff the method does not exist in the class.
     * @throws MethodCodeNotFoundException iff the method has not the 
     *         Code attribute.
     */
    public abstract int getLocalVariableLength(Signature methodSignature) 
    throws MethodNotFoundException, MethodCodeNotFoundException;
    
    /**
     * Returns the length of the bytecode of a method.
     * 
     * @param methodSignature a {@link Signature}.
     * @return a {@code int} representing the length in bytes of the 
     *         bytecode of the method with signature 
     *         {@code methodSignature}, in the case such method exists 
     *         in the class and has code.
     * @throws MethodNotFoundException iff the method does not exist in the class.
     * @throws MethodCodeNotFoundException iff the method has not the 
     *         Code attribute.
     */
    public abstract int getCodeLength(Signature methodSignature) 
    throws MethodNotFoundException, MethodCodeNotFoundException;
    
    /**
     * Creates a default local variable table from a method's signature.
     * 
     * @param methodSignature the {@link Signature} of a method.
     * @return a {@link LocalVariableTable} containing entries 
     *         inferred from the method's parameters.
     * @throws MethodNotFoundException iff {@link #hasMethodDeclaration}{@code (methodSignature) == false}.
     * @throws MethodCodeNotFoundException iff the method has not the Code attribute.
     */
    protected final LocalVariableTable defaultLocalVariableTable(Signature methodSignature) 
    throws MethodNotFoundException, MethodCodeNotFoundException {
        //if no LocalVariableTable attribute is found, tries to create the local 
        //variable table from information on the method's signature
    	boolean isStatic = isMethodStatic(methodSignature);
    	final String[] parDescList = Type.splitParametersDescriptors(methodSignature.getDescriptor());
    	final LocalVariableTable lvt = new LocalVariableTable(getLocalVariableLength(methodSignature));
    	int i = 0;
    	short slot = 0;
    	if (!isStatic) {
        	lvt.setEntry(slot, Type.REFERENCE + this.getClassName() + Type.TYPEEND, 
        			     "this", 0, this.getCodeLength(methodSignature));
    		++i; ++slot;
    	}
    	for (String descriptor : parDescList) {
    		lvt.setEntry(slot, descriptor, 
    				     "__PARAM[" + i + "]", 0, this.getCodeLength(methodSignature));
    		++i; ++slot;
    		if (!Type.isCat_1(descriptor.charAt(0))) {
    			++slot;
    		}
    	}
    	return lvt;
    }
    
    /**
     * Creates the default line number table to be returned in 
     * the case a method has not the LineNumberTable attribute.
     * 
     * @return the default (empty) {@link LineNumberTable}.
     */
    protected final LineNumberTable defaultLineNumberTable() {
    	return new LineNumberTable(0);
    }
    
    protected final String indexOutOfRangeMessage(int index) {
        return "index " + index + " not in constant pool of class " + getClassName();
    }
    
    protected final String entryInvalidMessage(int index) {
        return "index " + index + " did not correspond to a valid CONST_value entry in the constant pool of class " + getClassName();
    }

    @Override
    public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
    	if (this.getClass() != o.getClass()) {
    		return false; 
    	}
		return this.getClassName().equals(((ClassFile) o).getClassName());
    }
    
	@Override
	public int hashCode() {
		return this.getClassName().hashCode();
	}
}
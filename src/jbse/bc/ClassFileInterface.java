package jbse.bc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.HashMap;

import jbse.Type;
import jbse.exc.bc.ClassFileNotFoundException;
import jbse.exc.bc.InvalidClassFileFactoryClassException;
import jbse.exc.bc.NoArrayVisibilitySpecifiedException;
import jbse.exc.common.UnexpectedInternalException;

/**
 * Class that permits to retrieve information from a number of 
 * classfiles. Currently it does not support multiple class 
 * loaders, nor dynamic class loading.
 */ 
class ClassFileInterface {
	private ClassFileFactory f;
    private Map<String, ClassFile> jcrArray;
    
	/**
	 * Constructor.
	 * 
	 * @param cp a {@link Classpath}.
	 * @param fClass the {@link Class} of some subclass of {@link ClassFileFactory}.
	 *        The class must have an accessible constructor with two parameters, the first a 
	 *        {@link ClassFileInterface}, the second a {@link Classpath}.
	 * @throws InvalidClassFileFactoryClassException in the case {@link fClass}
	 *         has not the expected features (missing constructor, unaccessible 
	 *         constructor...).
	 */
    public ClassFileInterface(Classpath cp, Class<? extends ClassFileFactory> fClass) 
    throws InvalidClassFileFactoryClassException {
    	final Constructor<? extends ClassFileFactory> c;
		try {
			c = fClass.getConstructor(ClassFileInterface.class, Classpath.class);
	    	this.f = c.newInstance(this, cp);
		} catch (SecurityException | NoSuchMethodException | IllegalArgumentException | 
				InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new InvalidClassFileFactoryClassException(e);
		}
    }
    
    /**
     * Given a class name returns the correspondent {@link ClassFile}.
     * To avoid name clashes it does not manage primitive classes.
     * 
     * @param className the searched class.
     * @return the classFile structure of the correspondent class.
     * @throws ClassFileNotFoundException when the class file cannot be 
     * found in the classpath.
     */
    public ClassFile getClassFile(String className) 
    throws ClassFileNotFoundException {
    	ClassFile retVal = null;
        //lazily initializes jcrArray
        if (jcrArray == null) {
            jcrArray = new HashMap<String, ClassFile>();
        }
        
        if (!jcrArray.containsKey(className)) {        
	        //if the class file is not already in jcrArray, adds it
	        final ClassFile tempCF;
	        try {
				tempCF = this.f.newClassFile(className);
			} catch (NoArrayVisibilitySpecifiedException e) {
				throw new UnexpectedInternalException(e);
			}
	        jcrArray.put(className, tempCF);
        }
        
        retVal = jcrArray.get(className);
        if (retVal == null) {
        	throw new ClassFileNotFoundException(className);
        }
        return retVal; 
    }
    
    private final ClassFileBoolean primitiveClassBoolean = new ClassFileBoolean(); 
    private final ClassFileByte primitiveClassByte = new ClassFileByte();	
	private final ClassFileCharacter primitiveClassCharacter = new ClassFileCharacter();	
    private final ClassFileShort primitiveClassShort = new ClassFileShort();	
    private final ClassFileInteger primitiveClassInteger = new ClassFileInteger();	
    private final ClassFileLong primitiveClassLong = new ClassFileLong();	
    private final ClassFileFloat primitiveClassFloat = new ClassFileFloat();	
    private final ClassFileDouble primitiveClassDouble = new ClassFileDouble();	
    private final ClassFileVoid primitiveClassVoid = new ClassFileVoid();	
    
    
    /**
     * Given the name of a primitive type returns the correspondent 
     * {@link ClassFile}.
     * 
     * @param typeName the name of a primitive type (see the class {@link Type}).
     * @return the classFile structure of the correspondent class.
     */
    public ClassFile getClassFilePrimitive(String typeName) {
    	switch (typeName.charAt(0)) {
    	case Type.BOOLEAN:
    		return this.primitiveClassBoolean;
    	case Type.BYTE:
    		return this.primitiveClassByte;
    	case Type.CHAR:
    		return this.primitiveClassCharacter;
    	case Type.SHORT:
    		return this.primitiveClassShort;
    	case Type.INT:
    		return this.primitiveClassInteger;
    	case Type.LONG:
    		return this.primitiveClassLong;
    	case Type.FLOAT:
    		return this.primitiveClassFloat;
    	case Type.DOUBLE:
    		return this.primitiveClassDouble;
    	case Type.VOID:
    		return this.primitiveClassVoid;
    	default:
    		return null;
    	}
    }
}
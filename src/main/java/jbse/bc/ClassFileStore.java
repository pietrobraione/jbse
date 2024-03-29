package jbse.bc;

import static jbse.bc.ClassLoaders.CLASSLOADER_NONE;

import java.util.ArrayList;
import java.util.HashMap;

import jbse.common.Type;
import jbse.common.exc.InvalidInputException;

/**
 * A container for the loaded classfiles. Implements
 * the loaded class cache, similarly to what
 * in the Hotspot JVM implementation does the system
 * dictionary. 
 */ 
final class ClassFileStore implements Cloneable {  
    /** 
     * The loaded class cache; maps the initiating loader id plus the class name
     * to the {@link ClassFile} for the loaded class.
     */
    private ArrayList<HashMap<String, ClassFile>> loadedClassCache = new ArrayList<>(); //not final because of clone
    
    /** The cache for the anonymous classes. */
    private HashMap<String, ClassFile> anonymousClasses = new HashMap<>(); //not final because of clone

    // The primitive classfiles.
    private final ClassFileBoolean primitiveClassFileBoolean = new ClassFileBoolean(); 
    private final ClassFileByte primitiveClassFileByte = new ClassFileByte();   
    private final ClassFileCharacter primitiveClassFileCharacter = new ClassFileCharacter();    
    private final ClassFileShort primitiveClassFileShort = new ClassFileShort();    
    private final ClassFileInteger primitiveClassFileInteger = new ClassFileInteger();  
    private final ClassFileLong primitiveClassFileLong = new ClassFileLong();   
    private final ClassFileFloat primitiveClassFileFloat = new ClassFileFloat();    
    private final ClassFileDouble primitiveClassFileDouble = new ClassFileDouble(); 
    private final ClassFileVoid primitiveClassFileVoid = new ClassFileVoid();
    
    /**
     * Puts a {@link ClassFile} in this store.
     * 
     * @param initiatingLoader an {@code int}, the identifier of 
     *        a classloader.
     * @param classFile a {@link ClassFile}.
     * @throws InvalidInputException if {@code initiatingLoader} is invalid (negative),
     *         or {@code classFile == null}, or {@code classFile.}{@link ClassFile#isPrimitiveOrVoid() isPrimitiveOrVoid()}, or 
     *         {@code classFile.}{@link ClassFile#isAnonymousUnregistered() isAnonymousUnregistered()}, 
     *         or {@code classFile.}{@link ClassFile#isDummy() isDummy()}, 
     *         or there is already a different {@link ClassFile} in the loaded class cache for the pair
     *         {@code (initiatingLoader, classFile.}{@link ClassFile#getClassName() getClassName}{@code ())}.
     */
    void putClassFile(int initiatingLoader, ClassFile classFile) 
    throws InvalidInputException {
        //checks parameters
        if (initiatingLoader <= CLASSLOADER_NONE) {
            throw new InvalidInputException("Attemped to invoke " + ClassFileStore.class.getName() + ".putLoadedClassCache with an invalid (negative) initiatingLoader value.");
        }
        if (classFile == null) {
            throw new InvalidInputException("Attemped to invoke " + ClassFileStore.class.getName() + ".putLoadedClassCache with an invalid (null) classFile value.");
        }
        if (classFile.isPrimitiveOrVoid()) {
            throw new InvalidInputException("Invoked " + this.getClass().getName() + ".putLoadedClassCache with a classFile parameter that the classfile for the primitive type " + classFile.getClassName() + ".");
        }
        if (classFile.isAnonymousUnregistered()) {
            throw new InvalidInputException("Invoked " + this.getClass().getName() + ".putLoadedClassCache with a classFile parameter that is a an anonymous (unregistered) classfile with name " + classFile.getClassName() + ".");
        }
        if (classFile.isDummy()) {
            throw new InvalidInputException("Invoked " + this.getClass().getName() + ".putLoadedClassCache with a classFile parameter that is a dummy classfile.");
        }
        
        //makes room
        for (int i = this.loadedClassCache.size(); i <= initiatingLoader; ++i) {
            this.loadedClassCache.add(new HashMap<>());
        }

        final ClassFile previousClassFile = getClassFile(initiatingLoader, classFile.getClassName());
        if (previousClassFile == null) {
            this.loadedClassCache.get(initiatingLoader).put(classFile.getClassName(), classFile);
        } else if (previousClassFile == classFile) {
            //reinsertion of the same classfile, does nothing
            return;
        } else {
            //attempted modification
            throw new InvalidInputException("Attemped to invoke " + ClassFileStore.class.getName() + ".putLoadedClassCache to modify, rather than increase, the loaded class cache.");
        }
    }
    
    /**
     * Puts an anonymous (in the sense of
     * {@link sun.misc.Unsafe#defineAnonymousClass}) {@link ClassFile} in this store.
     * 
     * @param classFile a {@link ClassFile}.
     * @throws InvalidInputException if {@code classFile == null}, or {@code classFile.}{@link ClassFile#isPrimitiveOrVoid() isPrimitiveOrVoid()}, or 
     *         {@code !classFile.}{@link ClassFile#isAnonymousUnregistered() isAnonymousUnregistered()}, 
     *         or {@code classFile.}{@link ClassFile#isDummy() isDummy()}.
     */
    @Deprecated
    void putClassFileAnonymous(ClassFile classFile) 
    throws InvalidInputException {
        if (classFile == null) {
            throw new InvalidInputException("Attemped to invoke " + ClassFileStore.class.getName() + ".putAnonymousClassCache with an invalid (null) classFile value.");
        }
        if (classFile.isPrimitiveOrVoid()) {
            throw new InvalidInputException("Invoked " + this.getClass().getName() + ".putAnonymousClassCache with a classFile parameter that the classfile for the primitive type " + classFile.getClassName() + ".");
        }
        if (!classFile.isAnonymousUnregistered()) {
            throw new InvalidInputException("Invoked " + this.getClass().getName() + ".putAnonymousClassCache with a classFile parameter that is not a an anonymous (unregistered) classfile with name " + classFile.getClassName() + ".");
        }
        if (classFile.isDummy()) {
            throw new InvalidInputException("Invoked " + this.getClass().getName() + ".putAnonymousClassCache with a classFile parameter that is a dummy classfile.");
        }
        this.anonymousClasses.put(classFile.getClassName(), classFile);
    }
    
    /**
     * Given a class name and the identifier of an initiating class loader 
     * returns the corresponding {@link ClassFile} stored in this 
     * store, if present. It does not manage primitive classes.
     * 
     * @param initiatingLoader an {@code int}, the identifier of 
     *        a classloader.
     * @param className a {@link String}, the name of a class.
     * @return the {@link ClassFile} corresponding to the pair 
     *         {@code (initiatingLoader, className)} in the
     *         loaded class cache, if there is one, {@code null}
     *         otherwise. 
     */
    ClassFile getClassFile(int initiatingLoader, String className) {
        if (0 <= initiatingLoader && initiatingLoader < this.loadedClassCache.size()) {
            final HashMap<String, ClassFile> classFiles = this.loadedClassCache.get(initiatingLoader);
            if (classFiles.containsKey(className)) {
                return classFiles.get(className);
            }
        }
        return null;
    }

    /**
     * Given the name of a primitive type returns the corresponding 
     * {@link ClassFile}.
     * 
     * @param typeName a {@code String}, the internal name of a primitive type 
     *        (see the class {@link Type}).
     * @return same as {@link #getClassFilePrimitiveOrVoid(char) getClassFilePrimitiveOrVoid}{@code (typeName.charAt(0))}.
     * @throws InvalidInputException if {@code typeName} is not the internal name of a primitive type.
     */
    ClassFile getClassFilePrimitiveOrVoid(String typeName) throws InvalidInputException {
        return getClassFilePrimitiveOrVoid(typeName.charAt(0));
    }
    
    /**
     * Given the name of a primitive type returns the corresponding 
     * {@link ClassFile}.
     * 
     * @param type a {@code char}, the internal name of a primitive type 
     *        (see the class {@link Type}).
     * @return the {@link ClassFile} of the corresponding primitive class,
     *         possibly a {@link ClassFileBad}.
     * @throws InvalidInputException if {@code type} is not valid.
     */
    ClassFile getClassFilePrimitiveOrVoid(char type) throws InvalidInputException {
        switch (type) {
        case Type.BOOLEAN:
            return this.primitiveClassFileBoolean;
        case Type.BYTE:
            return this.primitiveClassFileByte;
        case Type.CHAR:
            return this.primitiveClassFileCharacter;
        case Type.SHORT:
            return this.primitiveClassFileShort;
        case Type.INT:
            return this.primitiveClassFileInteger;
        case Type.LONG:
            return this.primitiveClassFileLong;
        case Type.FLOAT:
            return this.primitiveClassFileFloat;
        case Type.DOUBLE:
            return this.primitiveClassFileDouble;
        case Type.VOID:
            return this.primitiveClassFileVoid;
        default:
            throw new InvalidInputException("Attempted to invoke " + this.getClass().getName() + ".getClassFilePrimitive with parameter type equal to " + type);
        }
    }
    
    /**
     * Returns the initiating loader of a classfile. Useful for
     * inverse lookups.
     * 
     * @param classFile a {@link ClassFile}.
     * @return an {@code int}, the initiating loader of {@code classFile}, 
     *         or {@code -1} if {@code classFile} is not found (also when
     *         {@code classFile == null}, or is anonymous, or is primitive). 
     */
    int getInitiatingLoader(ClassFile classFile) {
    	int retVal = -1;
    	for (int i = 0; i < this.loadedClassCache.size(); ++i) {
    		final HashMap<String, ClassFile> classFiles = this.loadedClassCache.get(i);
    		if (classFile.equals(classFiles.get(classFile.getClassName()))) {
    			retVal = i;
    			break;
    		}
    	}
    	return retVal;
    }
    
    @Override
    protected ClassFileStore clone() {
        final ClassFileStore o;
        try {
            o = (ClassFileStore) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
        
        //loadedClassCache
        o.loadedClassCache = new ArrayList<>();
        for (HashMap<String, ClassFile> map : this.loadedClassCache) {
            o.loadedClassCache.add(new HashMap<>(map));
        }
        
        //anonymousClasses
        o.anonymousClasses = new HashMap<>(o.anonymousClasses);
        
        return o;
    }
}
package jbse.bc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.InvalidClassFileFactoryClassException;
import jbse.common.Type;

/**
 * Container of all classfiles. Currently it does not support 
 * multiple class loaders, nor dynamic class loading.
 */ 
class ClassFileStore {
    private final ClassFileFactory f;
    private final HashMap<String, ClassFile> cache = new HashMap<>();
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
     * Constructor.
     * 
     * @param cp a {@link Classpath}.
     * @param fClass the {@link Class} of some subclass of {@link ClassFileFactory}.
     *        The class must have an accessible constructor with two parameters, the first a 
     *        {@link ClassFileStore}, the second a {@link Classpath}.
     * @throws InvalidClassFileFactoryClassException in the case {@link fClass}
     *         has not the expected features (missing constructor, unaccessible 
     *         constructor...).
     */
    ClassFileStore(Classpath cp, Class<? extends ClassFileFactory> fClass) 
    throws InvalidClassFileFactoryClassException {
        final Constructor<? extends ClassFileFactory> c;
        try {
            c = fClass.getConstructor(ClassFileStore.class, Classpath.class);
            this.f = c.newInstance(this, cp);
        } catch (SecurityException | NoSuchMethodException | IllegalArgumentException | 
        InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new InvalidClassFileFactoryClassException(e);
        }
    }

    /**
     * Given a class name returns the corresponding {@link ClassFile}.
     * To avoid name clashes it does not manage primitive classes.
     * 
     * @param className the searched class.
     * @return the {@link ClassFile} of the corresponding class, 
     *         possibly a {@link ClassFileBad}.
     */
    ClassFile getClassFile(String className) {
        //if the class file is not already in cache, adds it
        if (!this.cache.containsKey(className)) {        
            ClassFile tempCF;
            try {
                tempCF = this.f.newClassFile(className);
            } catch (BadClassFileException e) {
                tempCF = new ClassFileBad(className, e);
            }
            this.cache.put(className, tempCF);
        }
        return this.cache.get(className);
    }

    /**
     * Given the name of a primitive type returns the corresponding 
     * {@link ClassFile}.
     * 
     * @param typeName a {@code String}, the internal name of a primitive type 
     *        (see the class {@link Type}).
     * @return same as {@link #getClassFilePrimitive(char) getClassFilePrimitive}{@code (typeName.charAt(0))}.
     */
    ClassFile getClassFilePrimitive(String typeName) {
        return getClassFilePrimitive(typeName.charAt(0));
    }
    
    /**
     * Given the name of a primitive type returns the corresponding 
     * {@link ClassFile}.
     * 
     * @param type a {@code char}, the internal name of a primitive type 
     *        (see the class {@link Type}).
     * @return the {@link ClassFile} of the corresponding primitive class,
     *         possibly a {@link ClassFileBad}.
     */
    ClassFile getClassFilePrimitive(char type) {
        switch (type) {
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
            return new ClassFileBad("" + type, new ClassFileNotFoundException("" + type));
        }
    }
}
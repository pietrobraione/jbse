package jbse.bc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.InvalidClassFileFactoryClassException;
import jbse.common.Type;

/**
 * Container of all classfiles. Currently it does not support 
 * multiple class loaders, nor dynamic class loading.
 */ 
final class ClassFileStore implements Cloneable {
    private final ClassFileFactory f;
    private final ClassFileBoolean primitiveClassFileBoolean = new ClassFileBoolean(); 
    private final ClassFileByte primitiveClassFileByte = new ClassFileByte();   
    private final ClassFileCharacter primitiveClassFileCharacter = new ClassFileCharacter();    
    private final ClassFileShort primitiveClassFileShort = new ClassFileShort();    
    private final ClassFileInteger primitiveClassFileInteger = new ClassFileInteger();  
    private final ClassFileLong primitiveClassFileLong = new ClassFileLong();   
    private final ClassFileFloat primitiveClassFileFloat = new ClassFileFloat();    
    private final ClassFileDouble primitiveClassFileDouble = new ClassFileDouble(); 
    private final ClassFileVoid primitiveClassFileVoid = new ClassFileVoid();   
    private HashMap<String, ClassFile> classFiles = new HashMap<>(); //not final because of clone

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
        if (!this.classFiles.containsKey(className)) {        
            ClassFile tempCF;
            try {
                tempCF = this.f.newClassFile(className);
            } catch (BadClassFileException e) {
                tempCF = new ClassFileBad(className, e);
            }
            this.classFiles.put(className, tempCF);
        }
        return this.classFiles.get(className);
    }
    
    /**
     * Wraps a {@link ClassFile} to (temporarily) add
     * some constants to its constant pool.
     * 
     * @param classToWrap a {@link String}, 
     *        the name of the class to wrap.
     * @param constants a {@link Map}{@code <}{@link Integer}{@code , }{@link ConstantPoolValue}{@code >}, 
     *        mapping indices to corresponding constant 
     *        values in the expanded constant pool.
     * @param signatures a {@link Map}{@code <}{@link Integer}{@code , }{@link Signature}{@code >}, 
     *        mapping indices to corresponding {@link Signature}s 
     *        in the expanded constant pool.
     * @param classes a {@link Map}{@code <}{@link Integer}{@code , }{@link String}{@code >}, 
     *        mapping indices to corresponding class names 
     *        in the expanded constant pool.
     */
    void wrapClassFile(String classToWrap,
                     Map<Integer, ConstantPoolValue> constants, 
                     Map<Integer, Signature> signatures,
                     Map<Integer, String> classes) {
        final ClassFile classFileToWrap = getClassFile(classToWrap);
        if (classFileToWrap instanceof ClassFileBad) {
            return;
        }
        final ClassFileWrapper wrapper = new ClassFileWrapper(classFileToWrap, constants, signatures, classes);
        this.classFiles.put(classToWrap, wrapper);
    }
    
    /**
     * Unwraps a previously wrapped {@link ClassFile}.
     * 
     * @param classToUnwrap classToWrap a {@link String}, 
     *        the name of the class to unwrap.
     */
    void unwrapClassFile(String classToUnwrap) {
        final ClassFile classFileToUnwrap = getClassFile(classToUnwrap);
        if (classFileToUnwrap instanceof ClassFileWrapper) {
            final ClassFileWrapper wrapper = (ClassFileWrapper) classFileToUnwrap;
            this.classFiles.put(classToUnwrap, wrapper.getWrapped());
        }
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
            return new ClassFileBad("" + type, new ClassFileNotFoundException("" + type));
        }
    }
    
    @Override
    protected ClassFileStore clone() {
        final ClassFileStore o;
        try {
            o = (ClassFileStore) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
        
        //classFiles
        o.classFiles = new HashMap<>(o.classFiles);
        
        return o;
    }
}
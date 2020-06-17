package jbse.bc;

import jbse.bc.exc.ClassFileIllFormedException;
import jbse.common.Type;
import jbse.common.exc.InvalidInputException;

/**
 * Factory for {@link ClassFile}s.
 * 
 * @author Pietro Braione
 */
public abstract class ClassFileFactory implements Cloneable {
    protected abstract ClassFile newClassFileClass(int definingClassLoader, String className, byte[] bytecode, ClassFile superClass, ClassFile[] superInterfaces) 
    throws InvalidInputException, ClassFileIllFormedException;
    
    protected abstract ClassFile newClassFileAnonymous(byte[] bytecode, ClassFile cf_JAVA_OBJECT, ConstantPoolValue[] cpPatches, ClassFile hostClass)
    throws InvalidInputException, ClassFileIllFormedException;

    protected final ClassFile newClassFileArray(String className, ClassFile memberClass, ClassFile cf_JAVA_OBJECT, ClassFile cf_JAVA_CLONEABLE, ClassFile cf_JAVA_SERIALIZABLE) 
    throws InvalidInputException {
        if (className == null) {
            throw new InvalidInputException("The className parameter to " + ClassFileFactory.class.getCanonicalName() + ".newClassFileArray was null.");
        } 
        if (!Type.isArray(className)) {
            throw new InvalidInputException("The className parameter to " + ClassFileFactory.class.getCanonicalName() + ".newClassFileArray was not an array type.");
        } 
        if (cf_JAVA_OBJECT == null) {
            throw new InvalidInputException("The cf_JAVA_OBJECT parameter to " + ClassFileFactory.class.getCanonicalName() + ".newClassFileArray was null.");
        } 
        if (cf_JAVA_CLONEABLE == null) {
            throw new InvalidInputException("The cf_JAVA_CLONEABLE parameter to " + ClassFileFactory.class.getCanonicalName() + ".newClassFileArray was null.");
        } 
        if (cf_JAVA_SERIALIZABLE == null) {
            throw new InvalidInputException("The cf_JAVA_SERIALIZABLE parameter to " + ClassFileFactory.class.getCanonicalName() + ".newClassFileArray was null.");
        } 

        return new ClassFileArray(className, memberClass, cf_JAVA_OBJECT, cf_JAVA_CLONEABLE, cf_JAVA_SERIALIZABLE);
    }
}

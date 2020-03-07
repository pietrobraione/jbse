package jbse.bc;

import jbse.bc.exc.ClassFileIllFormedException;
import jbse.common.exc.InvalidInputException;

/**
 * A {@link ClassFileFactory} that uses the <a href="http://www.javassist.org/">Javassist</a> library
 * to analyze class files at a low level.
 * 
 * @author Pietro Braione
 */
public class ClassFileFactoryJavassist extends ClassFileFactory {
    @Override
    protected ClassFile newClassFileClass(int definingClassLoader, String className, byte[] bytecode, ClassFile superClass, ClassFile[] superInterfaces) 
    throws InvalidInputException, ClassFileIllFormedException {
        if (definingClassLoader < 0) {
            throw new InvalidInputException("The definingClassLoader parameter to " + ClassFileFactoryJavassist.class.getName() + ".newClassFileClass method was negative.");
        }
        if (className == null) {
            throw new InvalidInputException("The className parameter to " + ClassFileFactoryJavassist.class.getName() + ".newClassFileClass method was null.");
        }
        if (bytecode == null) {
            throw new InvalidInputException("The bytecode parameter to " + ClassFileFactoryJavassist.class.getName() + ".newClassFileClass method was null.");
        }
        
        return new ClassFileJavassist(definingClassLoader, className, bytecode, superClass, superInterfaces);
    }
    
    @Override
    protected ClassFile newClassFileAnonymous(byte[] bytecode, ClassFile cfJAVA_OBJECT, ConstantPoolValue[] cpPatches, ClassFile hostClass) 
    throws InvalidInputException, ClassFileIllFormedException {
        return new ClassFileJavassist(bytecode, cfJAVA_OBJECT, cpPatches, hostClass);
    }
}

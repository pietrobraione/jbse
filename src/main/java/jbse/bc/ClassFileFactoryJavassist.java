package jbse.bc;

import javassist.ClassPool;
import javassist.NotFoundException;

import jbse.bc.exc.BadClassFileException;

/**
 * A {@link ClassFileFactory} that uses the <a href="http://www.javassist.org/">Javassist</a> library
 * to analyze class files at a low level.
 * 
 * @author Pietro Braione
 */
public class ClassFileFactoryJavassist extends ClassFileFactory {
    private ClassPool cpool;

    public ClassFileFactoryJavassist(ClassFileStore cfi, Classpath cp) { 
        super(cfi);
        this.cpool = new ClassPool();
        for (String s : cp.classPath()) {
            try {
                this.cpool.appendClassPath(s);
            } catch (NotFoundException e) {
                //does nothing
            }
        }
    }

    @Override
    protected ClassFile newClassFileClass(String className) 
    throws BadClassFileException {
        return new ClassFileJavassist(className, this.cpool);
    }
}

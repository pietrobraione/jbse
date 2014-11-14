package jbse.bc;

import javassist.ClassPool;
import javassist.NotFoundException;

import jbse.bc.exc.ClassFileNotFoundException;

public class ClassFileFactoryJavassist extends ClassFileFactory {
	private ClassPool cpool;

	public ClassFileFactoryJavassist(ClassFileInterface cfi, Classpath cp) { 
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
	throws ClassFileNotFoundException {
		return new ClassFileJavassist(className, this.cpool);
	}
}

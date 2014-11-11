package jbse.bc;

import javassist.ClassPool;
import javassist.NotFoundException;
import jbse.exc.bc.ClassFileNotFoundException;

public class ClassFileFactoryJavassist extends ClassFileFactory {
	private ClassPool cpool;

	public ClassFileFactoryJavassist(ClassFileInterface cfi, Classpath env) { 
		super(cfi, env);
		this.cpool = new ClassPool();
		for (String s : env.classPath()) {
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

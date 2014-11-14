package jbse.bc;

import jbse.bc.ClassFileArray.Visibility;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.NoArrayVisibilitySpecifiedException;
import jbse.common.Type;

/**
 * Factory for {@link ClassFile}s.
 * 
 * @author Pietro Braione
 *
 */
public abstract class ClassFileFactory {
	/** Backlink to owner {@link ClassFileInterface}. */
	private ClassFileInterface cfi;
	
	public ClassFileFactory(ClassFileInterface cfi) {
		this.cfi = cfi;
	}
	
	protected abstract ClassFile newClassFileClass(String className) throws ClassFileNotFoundException;
	
	public ClassFile newClassFile(String className) 
	throws NoArrayVisibilitySpecifiedException, ClassFileNotFoundException {
		if (Type.isArray(className)) {
        	//(recursively) gets the member class of an array
			final String memberType = Type.getArrayMemberType(className);
			final ClassFile classFileMember;
			if (Type.isPrimitive(memberType)) {
				classFileMember = this.cfi.getClassFilePrimitive(memberType);
			} else {
				final String memberClass = Type.className(memberType);
				classFileMember = this.cfi.getClassFile(memberClass);
			}

			//calculates package name
			//TODO couldn't find any specification for calculating this! Does it work for nested classes?
			final String packageName = classFileMember.getPackageName();
			
			//calculates visibility (JVM spec, 5.3.3, this
			//implementation exploits primitive class files)
			final Visibility visibility;
			if (classFileMember.isPublic()) {
				visibility = ClassFileArray.Visibility.PUBLIC;
			} else if (classFileMember.isPackage()) {
				visibility = ClassFileArray.Visibility.PACKAGE;
			} else {
				//TODO is this branch reachable for nested classes?
				throw new NoArrayVisibilitySpecifiedException();
			}
			return new ClassFileArray(className, packageName, visibility);
		} else {
			return newClassFileClass(className);
		}
	}	
}

package jbse.bc;

import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.MethodNotFoundException;

/**
 * Some utility functions.
 * 
 * @author Pietro Braione
 *
 */
public final class Util {
	/**
	 * Finds an annotation on a method.
	 * 
	 * @param hier a {@link ClassHierarchy}.
	 * @param methodSignatureResolved the {@link Signature} of the resolved method where to look
	 *        for the annotation
	 * @param annotation the {@link Class} of the annotation to look for
	 * @return an {@link Object}, the annotation, or {@code null} if the method is not
	 *         annotated with {@code annotation}.
	 * @throws ClassFileNotFoundException 
	 * @throws MethodNotFoundException 
	 */
	public static Object findMethodAnnotation(ClassHierarchy hier, Signature methodSignatureResolved, Class<?> annotation) 
	throws ClassFileNotFoundException, MethodNotFoundException {
		final ClassFile cf = hier.getClassFile(methodSignatureResolved.getClassName());	
		final Object[] annotations = cf.getMethodAvailableAnnotations(methodSignatureResolved);
		for (Object o : annotations) {
			if (annotation.isInstance(o)) {
				return o;
			}
		}
		return null;
	}


	/**
	 * Do not instantiate it!
	 */
	private Util() { }
}

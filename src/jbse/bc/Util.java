package jbse.bc;

import jbse.exc.bc.ClassFileNotFoundException;
import jbse.exc.bc.MethodNotFoundException;
import jbse.exc.common.UnexpectedInternalException;

/**
 * Some utility functions.
 * 
 * @author Pietro Braione
 *
 */
public final class Util {
	//classes
	public static final String JAVA_CLONEABLE    = "java/lang/Cloneable";
	public static final String JAVA_OBJECT       = "java/lang/Object";
	public static final String JAVA_SERIALIZABLE = "java/io/Serializable";
	
	//methods
	public static final Signature JAVA_LANG_THROWABLE_FILLINSTACKTRACE = 
			new Signature("java/lang/Throwable", "()Ljava/lang/Throwable;", "fillInStackTrace");
	public static final Signature JAVA_LANG_THROWABLE_GETSTACKTRACEDEPTH = 
			new Signature("java/lang/Throwable", "()I", "getStackTraceDepth");
	public static final Signature JAVA_LANG_THROWABLE_GETSTACKTRACEELEMENT = 
			new Signature("java/lang/Throwable", "(I)Ljava/lang/StackTraceElement;", "getStackTraceElement");

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
	 * @throws UnexpectedInternalException 
	 */
	public static Object findMethodAnnotation(ClassHierarchy hier, Signature methodSignatureResolved, Class<?> annotation) 
	throws ClassFileNotFoundException, MethodNotFoundException, UnexpectedInternalException {
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

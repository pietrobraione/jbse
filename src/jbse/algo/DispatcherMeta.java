package jbse.algo;

import jbse.algo.exc.MetaUnsupportedException;
import jbse.bc.ClassHierarchy;
import jbse.bc.Dispatcher;
import jbse.bc.Signature;
import jbse.bc.Util;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.meta.annotations.MetaOverridden;
import jbse.meta.annotations.Uninterpreted;

/**
 * Dispatches the {@link Signature} of a method with a meta-level alternative
 * implementation. This {@link Dispatcher} populates itself by looking for 
 * method annotations, and can be further populated by invoking its public
 * methods.
 * 
 * @author Pietro Braione
 */
public class DispatcherMeta extends Dispatcher<Signature, Algorithm> {
	/**
	 * Constructor.
	 */
	public DispatcherMeta() {
		this.setDispatchNonexistentStrategy(new DispatchStrategy<Algorithm>() {
			@Override
			public Algorithm doIt() {
				return null;
			}
		});
		
		//meta delegates for some native methods
		loadMetaDelegate(Util.JAVA_LANG_THROWABLE_FILLINSTACKTRACE,     new SEFillInStackTrace());
		loadMetaDelegate(Util.JAVA_LANG_THROWABLE_GETSTACKTRACEDEPTH,   new SEGetStackTraceDepth());
		loadMetaDelegate(Util.JAVA_LANG_THROWABLE_GETSTACKTRACEELEMENT, new SEGetStackTraceElement());
	}

	/**
	 * Returns the {@link Algorithm} associated to a {@link Signature}.
	 * 
	 * @param methodSignatureResolved the {@link Signature} of a  <em>resolved</em> method.
	 * @return an {@link Algorithm}, or {@code null} if the {@link Signature} is
	 *         not associated to an {@link Algorithm}. Note that this method
	 *         does <em>not</em> populate the dispatcher, so it must be 
	 *         always preceded by a call to {@link #isMeta}.
	 */
	@Override
	public Algorithm select(Signature methodSignatureResolved) {
		final Algorithm retVal;
        try {
            retVal = super.select(methodSignatureResolved);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
		return retVal;
	}
	
	/**
	 * Checks whether a method has a {@link MetaOverridden} or {@link Uninterpreted} 
	 * annotation, and as a side effect populates this {@link DispatcherMeta} with the 
	 * {@link Algorithm} to manage it.
	 *  
	 * @param hier a {@link ClassHierarchy}.
	 * @param methodSignatureResolved the {@link Signature} of a  <em>resolved</em> method.
	 * @return {@code true} iff the method has a {@link MetaOverridden} 
	 *         or {@link Uninterpreted} annotation.
	 * @throws ClassFileNotFoundException if it is unable to find the class file for 
	 *         {@code methodSignatureResolved} in the classpath.
	 * @throws MethodNotFoundException  if it is unable to find {@code methodSignatureResolved} in 
	 *         the class file.
	 * @throws MetaUnsupportedException if it is unable to find the specified {@link Algorithm}, 
	 *         to load it, or to instantiate it for any reason (misses from the meta-level classpath, 
	 *         has insufficient visibility, does not implement {@link Algorithm}...).
	 */
	public boolean isMeta(ClassHierarchy hier, Signature methodSignatureResolved) 
	throws ClassFileNotFoundException, MethodNotFoundException, 
	MetaUnsupportedException {
		//already loaded: returns true
		if (this.select(methodSignatureResolved) != null) {
			return true;
		}
		
		//looks for annotations, and in case returns false
		final MetaOverridden metaAnnotation = (MetaOverridden) Util.findMethodAnnotation(hier, methodSignatureResolved, MetaOverridden.class);
		final Uninterpreted unintAnnotation = (Uninterpreted) Util.findMethodAnnotation(hier, methodSignatureResolved, Uninterpreted.class);
		if (metaAnnotation == null && unintAnnotation == null) {
			return false;
		} else if (metaAnnotation != null) { //MetaOverridden has highest priority
			loadAlgoMetaOverridden(methodSignatureResolved, metaAnnotation.value());
		} else { //unintAnnotation != null
			loadAlgoUninterpreted(methodSignatureResolved, unintAnnotation.value());
		}
		return true;
	}
	
	/**
	 * Loads an {@link Algorithm} to manage the invocation of a method with the
	 * MetaOverridden directive.
	 * 
	 * @param methodSignatureResolved the {@link Signature} of a <em>resolved</em> method.
	 * @param metaDelegateClassName the class name of the {@link Algorithm} that
	 *        implements the invocation semantics for {@code methodSignatureResolved}.
	 * @throws MetaUnsupportedException if the class indicated in {@code metaDelegateClassName} 
	 *         does not exist, or cannot be loaded or instantiated for any reason 
	 *         (misses from the meta-level classpath, has insufficient visibility, does not 
	 *         implement {@link Algorithm}...).
	 */
	public void loadAlgoMetaOverridden(Signature methodSignatureResolved, String metaDelegateClassName) 
	throws MetaUnsupportedException {
		try {
			final Class<?> metaDelegateClass = ClassLoader.getSystemClassLoader().loadClass(metaDelegateClassName); //TODO make it a parameter Class<Algorithm> and delete the row
			final Algorithm metaDelegate = (Algorithm) metaDelegateClass.newInstance();
			loadMetaDelegate(methodSignatureResolved, metaDelegate);
		} catch (ClassNotFoundException e) {
			throw new MetaUnsupportedException("meta implementation class " + metaDelegateClassName + " not found.");
		} catch (InstantiationException e) {
			throw new MetaUnsupportedException("meta implementation class " + metaDelegateClassName + " cannot be instantiated.");
		} catch (IllegalAccessException e) {
			throw new MetaUnsupportedException("meta implementation class " + metaDelegateClassName + " cannot be accessed.");
		} catch (ClassCastException e) {
			throw new MetaUnsupportedException("meta implementation class " + metaDelegateClassName + " does not implement " + Algorithm.class);
		}
	}
	
	/**
	 * Loads an {@link Algorithm} to manage the invocation of a method with the
	 * Uninterpreted directive.
	 * 
	 * @param methodSignatureResolved the {@link Signature} of a <em>resolved</em> method.
	 * @param functionName the name chosen for the uninterpreted function. If {@code null}, 
	 *        then the (unqualified) name of the method will be used.
	 */
	public void loadAlgoUninterpreted(Signature methodSignatureResolved, String functionName) {
		final SEInvokeUninterpreted metaDelegate = new SEInvokeUninterpreted();
		metaDelegate.methodSignatureResolved = methodSignatureResolved;
		if (functionName == null) {
			metaDelegate.functionName = methodSignatureResolved.getName();
		} else {
			metaDelegate.functionName = functionName;
		}
		loadMetaDelegate(methodSignatureResolved, metaDelegate);
	}
	
	private void loadMetaDelegate(Signature methodSignatureResolved, final Algorithm metaDelegate) {
		this.setDispatchStrategy(methodSignatureResolved, () -> metaDelegate);
	}
}

package jbse.algo;

import jbse.algo.exc.MetaUnsupportedException;
import jbse.bc.ClassHierarchy;
import jbse.bc.Dispatcher;
import jbse.bc.Signature;
import jbse.bc.Util;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.meta.annotations.MetaOverriddenBy;
import jbse.meta.annotations.Uninterpreted;

/**
 * Dispatches the {@link Signature} of a method with a meta-level alternative
 * implementation. This {@link Dispatcher} populates itself by looking for 
 * method annotations, and can be further populated by invoking its public
 * methods.
 * 
 * @author Pietro Braione
 */
class DispatcherMeta extends Dispatcher<Signature, Algo_INVOKEMETA<?, ?, ?, ?>> {
	/**
	 * Constructor.
	 */
	public DispatcherMeta() {
		setDefault(new DispatchStrategy<Algo_INVOKEMETA<?, ?, ?, ?>>() {
			@Override
			public Algo_INVOKEMETA<?, ?, ?, ?> doIt() {
				return null;
			}
		});
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
	public Algo_INVOKEMETA<?, ?, ?, ?> select(Signature methodSignatureResolved) {
        try {
            final Algo_INVOKEMETA<?, ?, ?, ?> retVal = super.select(methodSignatureResolved);
            return retVal;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
	}
	
	/**
	 * Checks whether a method has a {@link MetaOverriddenBy} or {@link Uninterpreted} 
	 * annotation, and as a side effect populates this {@link DispatcherMeta} with the 
	 * {@link Algorithm} to manage it.
	 *  
	 * @param hier a {@link ClassHierarchy}.
	 * @param methodSignatureResolved the {@link Signature} of a  <em>resolved</em> method.
	 * @return {@code true} iff the method has a {@link MetaOverriddenBy} 
	 *         or {@link Uninterpreted} annotation.
	 * @throws BadClassFileException if there is no classfile for 
	 *         {@code methodSignatureResolved.}{@link Signature#getClassName() getClassName()} 
	 *         in the classpath, or if it is ill-formed.
	 * @throws MethodNotFoundException  if it is unable to find {@code methodSignatureResolved} in 
	 *         the class file.
	 * @throws MetaUnsupportedException if it is unable to find the specified {@link Algorithm}, 
	 *         to load it, or to instantiate it for any reason (misses from the meta-level classpath, 
	 *         has insufficient visibility, does not implement {@link Algorithm}...).
	 */
	public boolean isMeta(ClassHierarchy hier, Signature methodSignatureResolved) 
	throws BadClassFileException, MethodNotFoundException, 
	MetaUnsupportedException {
		//already loaded: returns true
		if (select(methodSignatureResolved) != null) {
			return true;
		}
		
		//looks for annotations, and in case returns false
		final MetaOverriddenBy overridAnnotation = (MetaOverriddenBy) Util.findMethodAnnotation(hier, methodSignatureResolved, MetaOverriddenBy.class);
		final Uninterpreted unintAnnotation = (Uninterpreted) Util.findMethodAnnotation(hier, methodSignatureResolved, Uninterpreted.class);
		if (overridAnnotation == null && unintAnnotation == null) {
			return false;
		} else if (overridAnnotation != null) { //MetaOverridden has highest priority
			loadAlgoMetaOverridden(methodSignatureResolved, overridAnnotation.value());
		} else { //unintAnnotation != null
			loadAlgoUninterpreted(methodSignatureResolved, unintAnnotation.value());
		}
		return true;
	}
	
	/**
	 * Loads an {@link Algorithm} to manage the invocation of a method with the
	 * {@link MetaOverriddenBy} annotation.
	 * 
	 * @param methodSignatureResolved the {@link Signature} of a <em>resolved</em> method.
	 * @param metaDelegateClass the class of the {@link Algorithm} that
	 *        implements the invocation semantics for {@code methodSignatureResolved}.
	 *        The class must be a subclass of {@link Algo_INVOKEMETA}.
	 * @throws MetaUnsupportedException if the class indicated in {@code metaDelegateClassName} 
	 *         does not exist, or cannot be loaded or instantiated for any reason 
	 *         (has insufficient visibility or has not a parameterless constructor).
	 */
	public void loadAlgoMetaOverridden(Signature methodSignatureResolved, 
	                                   Class<? extends Algo_INVOKEMETA<?, ?, ?, ?>> metaDelegateClass) 
	throws MetaUnsupportedException {
		try {
			final Algo_INVOKEMETA<?, ?, ?, ?> metaDelegate = metaDelegateClass.newInstance();
			loadMetaDelegate(methodSignatureResolved, metaDelegate);
		} catch (InstantiationException e) {
			throw new MetaUnsupportedException("meta-level implementation class " + metaDelegateClass + " cannot be instantiated.");
		} catch (IllegalAccessException e) {
			throw new MetaUnsupportedException("meta-level implementation class " + metaDelegateClass + " cannot be accessed.");
		}
	}
	
	/**
	 * Loads an {@link Algorithm} to manage the invocation of a method with the
	 * {@link Uninterpreted} annotation.
	 * 
	 * @param methodSignatureResolved the {@link Signature} of a <em>resolved</em> method.
	 * @param functionName the name chosen for the uninterpreted function. If {@code null}, 
	 *        then the (unqualified, to uppercase) name of the method will be used.
	 */
	public void loadAlgoUninterpreted(Signature methodSignatureResolved, String functionName) {
	    final String functionNameDflt = 
	        (functionName == null ? methodSignatureResolved.getName().toUpperCase() : functionName);
		final Algo_INVOKEUNINTERPRETED metaDelegate = 
		    new Algo_INVOKEUNINTERPRETED(methodSignatureResolved, functionNameDflt);
		loadMetaDelegate(methodSignatureResolved, metaDelegate);
	}
	
	private void loadMetaDelegate(Signature methodSignatureResolved, final Algo_INVOKEMETA<?, ?, ?, ?> metaDelegate) {
		setCase(methodSignatureResolved, () -> metaDelegate);
	}
}

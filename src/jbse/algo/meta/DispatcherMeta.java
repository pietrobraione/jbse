package jbse.algo.meta;

import static jbse.bc.Signatures.JAVA_CLASS_GETPRIMITIVECLASS;
import static jbse.bc.Signatures.JAVA_OBJECT_HASHCODE;
import static jbse.bc.Signatures.JAVA_STRING_INTERN;
import static jbse.bc.Signatures.JAVA_THROWABLE_FILLINSTACKTRACE;
import static jbse.bc.Signatures.JAVA_THROWABLE_GETSTACKTRACEDEPTH;
import static jbse.bc.Signatures.JAVA_THROWABLE_GETSTACKTRACEELEMENT;
import static jbse.bc.Signatures.JBSE_ANALYSIS_ANY;
import static jbse.bc.Signatures.JBSE_ANALYSIS_ASSERTREPOK;
import static jbse.bc.Signatures.JBSE_ANALYSIS_DISABLEASSUMPTIONVIOLATION;
import static jbse.bc.Signatures.JBSE_ANALYSIS_ENDGUIDANCE;
import static jbse.bc.Signatures.JBSE_ANALYSIS_FAIL;
import static jbse.bc.Signatures.JBSE_ANALYSIS_IGNORE;
import static jbse.bc.Signatures.JBSE_ANALYSIS_ISRESOLVED;
import static jbse.bc.Signatures.JBSE_ANALYSIS_ISRUNBYJBSE;
import static jbse.bc.Signatures.JBSE_ANALYSIS_SUCCEED;

import jbse.algo.Algorithm;
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
		
		//meta delegates for some JRE methods
        loadMetaDelegate(JAVA_CLASS_GETPRIMITIVECLASS,             new Algo_JAVA_CLASS_GETPRIMITIVECLASS());
        loadMetaDelegate(JAVA_OBJECT_HASHCODE,                     new Algo_JAVA_OBJECT_HASHCODE());
        loadMetaDelegate(JAVA_STRING_INTERN,                       new Algo_JAVA_STRING_INTERN());
		loadMetaDelegate(JAVA_THROWABLE_FILLINSTACKTRACE,          new Algo_JAVA_THROWABLE_FILLINSTACKTRACE());
		loadMetaDelegate(JAVA_THROWABLE_GETSTACKTRACEDEPTH,        new Algo_JAVA_THROWABLE_GETSTACKTRACEDEPTH());
		loadMetaDelegate(JAVA_THROWABLE_GETSTACKTRACEELEMENT,      new Algo_JAVA_THROWABLE_GETSTACKTRACEELEMENT());

		//meta delegates for some JBSE methods
        loadMetaDelegate(JBSE_ANALYSIS_ANY,                        new Algo_JBSE_ANALYSIS_ANY());
        loadMetaDelegate(JBSE_ANALYSIS_ASSERTREPOK,                new Algo_JBSE_ANALYSIS_ASSERTREPOK());
        loadMetaDelegate(JBSE_ANALYSIS_DISABLEASSUMPTIONVIOLATION, new Algo_JBSE_ANALYSIS_DISABLEASSUMPTIONVIOLATION());
        loadMetaDelegate(JBSE_ANALYSIS_ENDGUIDANCE,                new Algo_JBSE_ANALYSIS_ENDGUIDANCE());
        loadMetaDelegate(JBSE_ANALYSIS_FAIL,                       new Algo_JBSE_ANALYSIS_FAIL());
        loadMetaDelegate(JBSE_ANALYSIS_IGNORE,                     new Algo_JBSE_ANALYSIS_IGNORE());
        loadMetaDelegate(JBSE_ANALYSIS_ISRESOLVED,                 new Algo_JBSE_ANALYSIS_ISRESOLVED());
        loadMetaDelegate(JBSE_ANALYSIS_ISRUNBYJBSE,                new Algo_JBSE_ANALYSIS_ISRUNBYJBSE());
        loadMetaDelegate(JBSE_ANALYSIS_SUCCEED,                    new Algo_JBSE_ANALYSIS_SUCCEED());
		
		//meta delegates for some jbse.meta.Analysis methods
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
		if (this.select(methodSignatureResolved) != null) {
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
	 * MetaOverridden directive.
	 * 
	 * @param methodSignatureResolved the {@link Signature} of a <em>resolved</em> method.
	 * @param metaDelegateClass the class name of the {@link Algorithm} that
	 *        implements the invocation semantics for {@code methodSignatureResolved}.
	 * @throws MetaUnsupportedException if the class indicated in {@code metaDelegateClassName} 
	 *         does not exist, or cannot be loaded or instantiated for any reason 
	 *         (has insufficient visibility or has not a parameterless constructor).
	 */
	public void loadAlgoMetaOverridden(Signature methodSignatureResolved, Class<? extends Algorithm> metaDelegateClass) 
	throws MetaUnsupportedException {
		try {
			final Algorithm metaDelegate = metaDelegateClass.newInstance();
			loadMetaDelegate(methodSignatureResolved, metaDelegate);
		} catch (InstantiationException e) {
			throw new MetaUnsupportedException("meta-level implementation class " + metaDelegateClass + " cannot be instantiated.");
		} catch (IllegalAccessException e) {
			throw new MetaUnsupportedException("meta-level implementation class " + metaDelegateClass + " cannot be accessed.");
		}
	}
	
	/**
	 * Loads an {@link Algorithm} to manage the invocation of a method with the
	 * Uninterpreted directive.
	 * 
	 * @param methodSignatureResolved the {@link Signature} of a <em>resolved</em> method.
	 * @param functionName the name chosen for the uninterpreted function. If {@code null}, 
	 *        then the (unqualified, to uppercase) name of the method will be used.
	 */
	public void loadAlgoUninterpreted(Signature methodSignatureResolved, String functionName) {
		final Algo_INVOKEUNINTERPRETED metaDelegate = new Algo_INVOKEUNINTERPRETED();
		metaDelegate.methodSignatureResolved = methodSignatureResolved;
		if (functionName == null) {
			metaDelegate.functionName = methodSignatureResolved.getName().toUpperCase();
		} else {
			metaDelegate.functionName = functionName;
		}
		loadMetaDelegate(methodSignatureResolved, metaDelegate);
	}
	
	private void loadMetaDelegate(Signature methodSignatureResolved, final Algorithm metaDelegate) {
		this.setDispatchStrategy(methodSignatureResolved, () -> metaDelegate);
	}
}

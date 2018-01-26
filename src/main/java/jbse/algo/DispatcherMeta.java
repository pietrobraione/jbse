package jbse.algo;

import static jbse.common.Type.binaryClassName;
import static jbse.common.Type.internalClassName;

import jbse.algo.exc.MetaUnsupportedException;
import jbse.bc.ClassFile;
import jbse.bc.Dispatcher;
import jbse.bc.Signature;
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
     * @param methodClass the {@link ClassFile} of a <em>resolved</em> method, or {@code null}
     *        for a classless method.
     * @param methodSignature the {@link Signature} of a <em>resolved</em> method.
     * @return {@code true} iff the method has a {@link MetaOverriddenBy} 
     *         or {@link Uninterpreted} annotation.
     * @throws MethodNotFoundException  if it is unable to find a declaration for 
     *         {@code methodSignature} in {@code methodClass}.
     * @throws MetaUnsupportedException if it is unable to find the specified {@link Algorithm}, 
     *         to load it, or to instantiate it for any reason (misses from the meta-level classpath, 
     *         has insufficient visibility, does not implement {@link Algorithm}...).
     */
    public boolean isMeta(ClassFile methodClass, Signature methodSignature) 
    throws MethodNotFoundException, MetaUnsupportedException {
        //already loaded: returns true
        if (select(methodSignature) != null) {
            return true;
        }
        
        //method without class: returns false (otherwise the previous select
        //would have returned it
        if (methodClass == null) {
            return false;
        }

        //looks for annotations, and in case returns false
        final String metaOverriddenBy = internalClassName(MetaOverriddenBy.class.getName());
        final String uninterpreted = internalClassName(Uninterpreted.class.getName());
        final boolean overridAnnotationPresent = findMethodAnnotation(methodClass, methodSignature, metaOverriddenBy);
        final boolean unintAnnotationPresent = findMethodAnnotation(methodClass, methodSignature, uninterpreted);
        if (overridAnnotationPresent) { //MetaOverridden has highest priority
            final String value = methodClass.getMethodAnnotationParameterValueString(methodSignature, metaOverriddenBy, "value");
            loadAlgoMetaOverridden(methodSignature, value);
        } else if (unintAnnotationPresent) {
            final String value = methodClass.getMethodAnnotationParameterValueString(methodSignature, uninterpreted, "value");
            loadAlgoUninterpreted(methodSignature, value);
        } else {
            return false;
        }
        return true;
    }
    
    /**
     * Finds an annotation on a method.
     * 
     * @param classFileMethod the {@link ClassFile} where the method is declared.
     * @param methodSignature the {@link Signature} of the method. Only the name and 
     *        descriptor are considered.
     * @param annotation a {@code String}, the name of the annotation to look for.
     * @return {@code true} iff the method in {@code classFileMethod}
     *         with name and descriptor as in {@code methodSignature} is 
     *         annotated with {@code annotation}.
     * @throws MethodNotFoundException if {@code classFileMethod} does not contain a 
     *         method with name and descriptor as {@code methodSignature}.
     */
    private static boolean findMethodAnnotation(ClassFile classFileMethod, Signature methodSignature, String annotation) 
    throws MethodNotFoundException {
        final String[] annotations = classFileMethod.getMethodAvailableAnnotations(methodSignature);
        for (String o : annotations) {
            if (annotation.equals(o)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Loads an {@link Algorithm} to manage the invocation of a method with the
     * {@link MetaOverriddenBy} annotation.
     * 
     * @param methodSignatureResolved the {@link Signature} of a <em>resolved</em> method.
     * @param metaDelegate a {@link String}, the name of the class of the {@link Algorithm} that
     *        implements the invocation semantics for {@code methodSignatureResolved}.
     *        The class must be a subclass of {@link Algo_INVOKEMETA} and must be on the
     *        classpath.
     * @throws MetaUnsupportedException if the class indicated in {@code metaDelegateClassName} 
     *         does not exist in the classpath, or cannot be loaded or instantiated for any reason 
     *         (has insufficient visibility or has not a parameterless constructor).
     */
    public void loadAlgoMetaOverridden(Signature methodSignatureResolved, String metaDelegateClassName) 
    throws MetaUnsupportedException {
        try {
            @SuppressWarnings("unchecked")
            final Class<? extends Algo_INVOKEMETA<?, ?, ?, ?>> metaDelegateClass = (Class<? extends Algo_INVOKEMETA<?, ?, ?, ?>>) 
                ClassLoader.getSystemClassLoader().loadClass(binaryClassName(metaDelegateClassName)).asSubclass(Algo_INVOKEMETA.class);            
            final Algo_INVOKEMETA<?, ?, ?, ?> metaDelegate = metaDelegateClass.newInstance();
            loadMetaDelegate(methodSignatureResolved, metaDelegate);
        } catch (ClassNotFoundException e) {
            throw new MetaUnsupportedException("Meta-level implementation class " + metaDelegateClassName + " for method " + methodSignatureResolved + " does not exist.");
        } catch (InstantiationException e) {
            throw new MetaUnsupportedException("Meta-level implementation class " + metaDelegateClassName + " for method " + methodSignatureResolved + " cannot be instantiated.");
        } catch (IllegalAccessException e) {
            throw new MetaUnsupportedException("Meta-level implementation class " + metaDelegateClassName + " for method " + methodSignatureResolved + " cannot be accessed.");
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

package jbse.algo;

import static jbse.bc.Signatures.JAVA_PROPERTIES;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.TYPEEND;

import jbse.bc.Signature;

/**
 * This class defines class names and method signatures used as overriding
 * implementations.
 * 
 * @author Pietro Braione
 *
 */
public final class Overrides {
	//Overriding meta-level implementations of standard methods
    public static final String ALGO_JAVA_CLASS_DESIREDASSERTIONSTATUS0  = jbse.algo.meta.Algo_JAVA_CLASS_DESIREDASSERTIONSTATUS0.class.getCanonicalName().replace('.', '/');
    public static final String ALGO_JAVA_CLASS_GETCOMPONENTTYPE         = jbse.algo.meta.Algo_JAVA_CLASS_GETCOMPONENTTYPE.class.getCanonicalName().replace('.', '/');
    public static final String ALGO_JAVA_CLASS_GETPRIMITIVECLASS        = jbse.algo.meta.Algo_JAVA_CLASS_GETPRIMITIVECLASS.class.getCanonicalName().replace('.', '/');
    public static final String ALGO_JAVA_CLASS_ISINSTANCE               = jbse.algo.meta.Algo_JAVA_CLASS_ISINSTANCE.class.getCanonicalName().replace('.', '/');
    public static final String ALGO_JAVA_OBJECT_GETCLASS                = jbse.algo.meta.Algo_JAVA_OBJECT_GETCLASS.class.getCanonicalName().replace('.', '/');
    public static final String ALGO_JAVA_OBJECT_HASHCODE                = jbse.algo.meta.Algo_JAVA_OBJECT_HASHCODE.class.getCanonicalName().replace('.', '/');
    public static final String ALGO_JAVA_REFLECT_ARRAY_NEWARRAY         = jbse.algo.meta.Algo_JAVA_REFLECT_ARRAY_NEWARRAY.class.getCanonicalName().replace('.', '/');
    public static final String ALGO_JAVA_STRING_HASHCODE                = jbse.algo.meta.Algo_JAVA_STRING_HASHCODE.class.getCanonicalName().replace('.', '/');
    public static final String ALGO_JAVA_STRING_INTERN                  = jbse.algo.meta.Algo_JAVA_STRING_INTERN.class.getCanonicalName().replace('.', '/');
    public static final String ALGO_JAVA_SYSTEM_ARRAYCOPY               = jbse.algo.meta.Algo_JAVA_SYSTEM_ARRAYCOPY.class.getCanonicalName().replace('.', '/');
    public static final String ALGO_JAVA_SYSTEM_IDENTITYHASHCODE        = jbse.algo.meta.Algo_JAVA_SYSTEM_IDENTITYHASHCODE.class.getCanonicalName().replace('.', '/');
    public static final String ALGO_JAVA_THROWABLE_FILLINSTACKTRACE     = jbse.algo.meta.Algo_JAVA_THROWABLE_FILLINSTACKTRACE.class.getCanonicalName().replace('.', '/');
    public static final String ALGO_JAVA_THROWABLE_GETSTACKTRACEDEPTH   = jbse.algo.meta.Algo_JAVA_THROWABLE_GETSTACKTRACEDEPTH.class.getCanonicalName().replace('.', '/');
    public static final String ALGO_JAVA_THROWABLE_GETSTACKTRACEELEMENT = jbse.algo.meta.Algo_JAVA_THROWABLE_GETSTACKTRACEELEMENT.class.getCanonicalName().replace('.', '/');

	//Overriding meta-level implementations of jbse.meta.Analysis methods
    public static final String ALGO_JBSE_ANALYSIS_ANY                       = jbse.algo.meta.Algo_JBSE_ANALYSIS_ANY.class.getCanonicalName().replace('.', '/');
    public static final String ALGO_JBSE_ANALYSIS_ENDGUIDANCE               = jbse.algo.meta.Algo_JBSE_ANALYSIS_ENDGUIDANCE.class.getCanonicalName().replace('.', '/');
    public static final String ALGO_JBSE_ANALYSIS_FAIL                      = jbse.algo.meta.Algo_JBSE_ANALYSIS_FAIL.class.getCanonicalName().replace('.', '/');
    public static final String ALGO_JBSE_ANALYSIS_IGNORE                    = jbse.algo.meta.Algo_JBSE_ANALYSIS_IGNORE.class.getCanonicalName().replace('.', '/');
    public static final String ALGO_JBSE_ANALYSIS_ISRESOLVED                = jbse.algo.meta.Algo_JBSE_ANALYSIS_ISRESOLVED.class.getCanonicalName().replace('.', '/');
    public static final String ALGO_JBSE_ANALYSIS_ISRUNBYJBSE               = jbse.algo.meta.Algo_JBSE_ANALYSIS_ISRUNBYJBSE.class.getCanonicalName().replace('.', '/');
    public static final String ALGO_JBSE_ANALYSIS_SUCCEED                   = jbse.algo.meta.Algo_JBSE_ANALYSIS_SUCCEED.class.getCanonicalName().replace('.', '/');
    public static final String ALGO_JBSE_ANALYSIS_ASSUMECLASSNOTINITIALIZED = jbse.algo.meta.Algo_JBSE_ANALYSIS_ASSUMECLASSNOTINITIALIZED.class.getCanonicalName().replace('.', '/');
    
    //Overriding base-level implementation of standard methods
    private static final String JBSE_BASE = jbse.base.Base.class.getCanonicalName().replace('.', '/');
    public static final Signature BASE_JAVA_SYSTEM_INITPROPERTIES = new Signature(JBSE_BASE, 
                                                                                  "(" + REFERENCE + JAVA_PROPERTIES + TYPEEND + ")" + REFERENCE + JAVA_PROPERTIES + TYPEEND, 
                                                                                  "base_JAVA_SYSTEM_INITPROPERTIES");
}

package jbse.algo;

import static jbse.bc.Signatures.JAVA_ACCESSCONTROLCONTEXT;
import static jbse.bc.Signatures.JAVA_CLASS;
import static jbse.bc.Signatures.JAVA_FIELD;
import static jbse.bc.Signatures.JAVA_OBJECT;
import static jbse.bc.Signatures.JAVA_PRIVILEGEDACTION;
import static jbse.bc.Signatures.JAVA_PRIVILEGEDEXCEPTIONACTION;
import static jbse.bc.Signatures.JAVA_PROPERTIES;
import static jbse.bc.Signatures.JAVA_THREAD;
import static jbse.bc.Signatures.SUN_UNSAFE;
import static jbse.common.Type.BOOLEAN;
import static jbse.common.Type.INT;
import static jbse.common.Type.internalClassName;
import static jbse.common.Type.LONG;
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
    public static final String ALGO_JAVA_CLASS_DESIREDASSERTIONSTATUS0  = internalClassName(jbse.algo.meta.Algo_JAVA_CLASS_DESIREDASSERTIONSTATUS0.class.getCanonicalName());
    public static final String ALGO_JAVA_CLASS_FORNAME0                 = internalClassName(jbse.algo.meta.Algo_JAVA_CLASS_FORNAME0.class.getCanonicalName());
    public static final String ALGO_JAVA_CLASS_GETCOMPONENTTYPE         = internalClassName(jbse.algo.meta.Algo_JAVA_CLASS_GETCOMPONENTTYPE.class.getCanonicalName());
    public static final String ALGO_JAVA_CLASS_GETDECLAREDCONSTRUCTORS0 = internalClassName(jbse.algo.meta.Algo_JAVA_CLASS_GETDECLAREDCONSTRUCTORS0.class.getCanonicalName());
    public static final String ALGO_JAVA_CLASS_GETDECLAREDFIELDS0       = internalClassName(jbse.algo.meta.Algo_JAVA_CLASS_GETDECLAREDFIELDS0.class.getCanonicalName());
    public static final String ALGO_JAVA_CLASS_GETPRIMITIVECLASS        = internalClassName(jbse.algo.meta.Algo_JAVA_CLASS_GETPRIMITIVECLASS.class.getCanonicalName());
    public static final String ALGO_JAVA_CLASS_ISASSIGNABLEFROM         = internalClassName(jbse.algo.meta.Algo_JAVA_CLASS_ISASSIGNABLEFROM.class.getCanonicalName());
    public static final String ALGO_JAVA_CLASS_ISINSTANCE               = internalClassName(jbse.algo.meta.Algo_JAVA_CLASS_ISINSTANCE.class.getCanonicalName());
    public static final String ALGO_JAVA_CLASS_ISINTERFACE              = internalClassName(jbse.algo.meta.Algo_JAVA_CLASS_ISINTERFACE.class.getCanonicalName());
    public static final String ALGO_JAVA_CLASS_ISPRIMITIVE              = internalClassName(jbse.algo.meta.Algo_JAVA_CLASS_ISPRIMITIVE.class.getCanonicalName());
    public static final String ALGO_JAVA_OBJECT_CLONE                   = internalClassName(jbse.algo.meta.Algo_JAVA_OBJECT_CLONE.class.getCanonicalName());
    public static final String ALGO_JAVA_OBJECT_GETCLASS                = internalClassName(jbse.algo.meta.Algo_JAVA_OBJECT_GETCLASS.class.getCanonicalName());
    public static final String ALGO_JAVA_OBJECT_HASHCODE                = internalClassName(jbse.algo.meta.Algo_JAVA_OBJECT_HASHCODE.class.getCanonicalName());
    public static final String ALGO_JAVA_REFLECT_ARRAY_NEWARRAY         = internalClassName(jbse.algo.meta.Algo_JAVA_REFLECT_ARRAY_NEWARRAY.class.getCanonicalName());
    public static final String ALGO_JAVA_STRING_HASHCODE                = internalClassName(jbse.algo.meta.Algo_JAVA_STRING_HASHCODE.class.getCanonicalName());
    public static final String ALGO_JAVA_STRING_INTERN                  = internalClassName(jbse.algo.meta.Algo_JAVA_STRING_INTERN.class.getCanonicalName());
    public static final String ALGO_JAVA_SYSTEM_ARRAYCOPY               = internalClassName(jbse.algo.meta.Algo_JAVA_SYSTEM_ARRAYCOPY.class.getCanonicalName());
    public static final String ALGO_JAVA_SYSTEM_IDENTITYHASHCODE        = internalClassName(jbse.algo.meta.Algo_JAVA_SYSTEM_IDENTITYHASHCODE.class.getCanonicalName());
    public static final String ALGO_JAVA_THREAD_CURRENTTHREAD           = internalClassName(jbse.algo.meta.Algo_JAVA_THREAD_CURRENTTHREAD.class.getCanonicalName());
    public static final String ALGO_JAVA_THROWABLE_FILLINSTACKTRACE     = internalClassName(jbse.algo.meta.Algo_JAVA_THROWABLE_FILLINSTACKTRACE.class.getCanonicalName());
    public static final String ALGO_JAVA_THROWABLE_GETSTACKTRACEDEPTH   = internalClassName(jbse.algo.meta.Algo_JAVA_THROWABLE_GETSTACKTRACEDEPTH.class.getCanonicalName());
    public static final String ALGO_JAVA_THROWABLE_GETSTACKTRACEELEMENT = internalClassName(jbse.algo.meta.Algo_JAVA_THROWABLE_GETSTACKTRACEELEMENT.class.getCanonicalName());
    public static final String ALGO_SUN_REFLECTION_GETCALLERCLASS       = internalClassName(jbse.algo.meta.Algo_SUN_REFLECTION_GETCALLERCLASS.class.getCanonicalName());
    public static final String ALGO_SUN_REFLECTION_GETCLASSACCESSFLAGS  = internalClassName(jbse.algo.meta.Algo_SUN_REFLECTION_GETCLASSACCESSFLAGS.class.getCanonicalName());
    public static final String ALGO_SUN_UNSAFE_OBJECTFIELDOFFSET        = internalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_OBJECTFIELDOFFSET.class.getCanonicalName());
    public static final String ALGO_SUN_UNSAFE_COMPAREANDSWAPINT        = internalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_COMPAREANDSWAPINT.class.getCanonicalName());
    public static final String ALGO_SUN_UNSAFE_COMPAREANDSWAPOBJECT     = internalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_COMPAREANDSWAPOBJECT.class.getCanonicalName());
    public static final String ALGO_SUN_UNSAFE_GETINTVOLATILE           = internalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_GETINTVOLATILE.class.getCanonicalName());

    //Overriding meta-level implementations of jbse.meta.Analysis methods
    public static final String ALGO_JBSE_ANALYSIS_ANY                       = internalClassName(jbse.algo.meta.Algo_JBSE_ANALYSIS_ANY.class.getCanonicalName());
    public static final String ALGO_JBSE_ANALYSIS_ENDGUIDANCE               = internalClassName(jbse.algo.meta.Algo_JBSE_ANALYSIS_ENDGUIDANCE.class.getCanonicalName());
    public static final String ALGO_JBSE_ANALYSIS_FAIL                      = internalClassName(jbse.algo.meta.Algo_JBSE_ANALYSIS_FAIL.class.getCanonicalName());
    public static final String ALGO_JBSE_ANALYSIS_IGNORE                    = internalClassName(jbse.algo.meta.Algo_JBSE_ANALYSIS_IGNORE.class.getCanonicalName());
    public static final String ALGO_JBSE_ANALYSIS_ISRESOLVED                = internalClassName(jbse.algo.meta.Algo_JBSE_ANALYSIS_ISRESOLVED.class.getCanonicalName());
    public static final String ALGO_JBSE_ANALYSIS_ISRUNBYJBSE               = internalClassName(jbse.algo.meta.Algo_JBSE_ANALYSIS_ISRUNBYJBSE.class.getCanonicalName());
    public static final String ALGO_JBSE_ANALYSIS_SUCCEED                   = internalClassName(jbse.algo.meta.Algo_JBSE_ANALYSIS_SUCCEED.class.getCanonicalName());
    public static final String ALGO_JBSE_ANALYSIS_ASSUMECLASSNOTINITIALIZED = internalClassName(jbse.algo.meta.Algo_JBSE_ANALYSIS_ASSUMECLASSNOTINITIALIZED.class.getCanonicalName());

    //Overriding base-level implementation of standard methods
    private static final String JBSE_BASE = internalClassName(jbse.base.Base.class.getCanonicalName());
    public static final Signature BASE_JAVA_ACCESSCONTROLLER_DOPRIVILEGED_EXCEPTION = 
        new Signature(JBSE_BASE, 
                      "(" + REFERENCE + JAVA_PRIVILEGEDEXCEPTIONACTION + TYPEEND + ")" + REFERENCE + JAVA_OBJECT + TYPEEND, 
                      "base_JAVA_ACCESSCONTROLLER_DOPRIVILEGED_EXCEPTION");
    public static final Signature BASE_JAVA_ACCESSCONTROLLER_DOPRIVILEGED_NOEXCEPTION = 
        new Signature(JBSE_BASE, 
                      "(" + REFERENCE + JAVA_PRIVILEGEDACTION + TYPEEND + ")" + REFERENCE + JAVA_OBJECT + TYPEEND, 
                      "base_JAVA_ACCESSCONTROLLER_DOPRIVILEGED_NOEXCEPTION");
    public static final Signature BASE_JAVA_ACCESSCONTROLLER_GETSTACKACCESSCONTROLCONTEXT = 
        new Signature(JBSE_BASE, 
                      "()" + REFERENCE + JAVA_ACCESSCONTROLCONTEXT + TYPEEND, 
                      "base_JAVA_ACCESSCONTROLLER_GETSTACKACCESSCONTROLCONTEXT");
    public static final Signature BASE_JAVA_SYSTEM_INITPROPERTIES =
        new Signature(JBSE_BASE, 
                      "(" + REFERENCE + JAVA_PROPERTIES + TYPEEND + ")" + REFERENCE + JAVA_PROPERTIES + TYPEEND, 
                      "base_JAVA_SYSTEM_INITPROPERTIES");
    public static final Signature BASE_JAVA_THREAD_ISALIVE =
        new Signature(JBSE_BASE, 
                      "(" + REFERENCE + JAVA_THREAD + TYPEEND + ")" + BOOLEAN, 
                      "base_JAVA_THREAD_ISALIVE");
    public static final Signature BASE_SUN_UNSAFE_ADDRESSSIZE =
        new Signature(JBSE_BASE, 
                      "(" + REFERENCE + SUN_UNSAFE + TYPEEND + ")" + INT, 
                      "base_SUN_UNSAFE_ADDRESSSIZE");
    public static final Signature BASE_SUN_UNSAFE_ARRAYBASEOFFSET =
        new Signature(JBSE_BASE, 
                      "(" + REFERENCE + SUN_UNSAFE + TYPEEND + REFERENCE + JAVA_CLASS + TYPEEND + ")" + INT, 
                      "base_SUN_UNSAFE_ARRAYBASEOFFSET");
    public static final Signature BASE_SUN_UNSAFE_ARRAYINDEXSCALE =
        new Signature(JBSE_BASE, 
                      "(" + REFERENCE + SUN_UNSAFE + TYPEEND + REFERENCE + JAVA_CLASS + TYPEEND + ")" + INT, 
                      "base_SUN_UNSAFE_ARRAYINDEXSCALE");
    public static final Signature BASE_SUN_UNSAFE_OBJECTFIELDOFFSET =
        new Signature(JBSE_BASE, 
                      "(" + REFERENCE + SUN_UNSAFE + TYPEEND + REFERENCE + JAVA_FIELD + TYPEEND + ")" + LONG, 
                      "base_SUN_UNSAFE_OBJECTFIELDOFFSET");
}

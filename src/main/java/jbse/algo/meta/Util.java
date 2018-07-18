package jbse.algo.meta;

import static jbse.bc.Signatures.JAVA_METHODHANDLE_INVOKEBASIC;
import static jbse.bc.Signatures.JAVA_METHODHANDLE_LINKTOINTERFACE;
import static jbse.bc.Signatures.JAVA_METHODHANDLE_LINKTOSPECIAL;
import static jbse.bc.Signatures.JAVA_METHODHANDLE_LINKTOSTATIC;
import static jbse.bc.Signatures.JAVA_METHODHANDLE_LINKTOVIRTUAL;

import java.lang.reflect.Modifier;

class Util {
    /**
     * Checks if a method name is the name of an intrinsic signature polymorphic
     * method.
     * 
     * @param methodName a {@link String}.
     * @return {@code true} if {@code methodName} is one of {@code invokeBasic}, 
     *         {@code linkToInterface}, {@code linkToSpecial}, {@code linkToStatic},
     *         or {@code linkToVirtual}. 
     */
    static boolean isSignaturePolymorphicMethodIntrinsic(String methodName) {
        return 
        (JAVA_METHODHANDLE_INVOKEBASIC.getName().equals(methodName) ||
        JAVA_METHODHANDLE_LINKTOINTERFACE.getName().equals(methodName) ||
        JAVA_METHODHANDLE_LINKTOSPECIAL.getName().equals(methodName) ||
        JAVA_METHODHANDLE_LINKTOSTATIC.getName().equals(methodName) ||
        JAVA_METHODHANDLE_LINKTOVIRTUAL.getName().equals(methodName));
    }

    //taken from java.lang.reflect.Modifier
    static final short BRIDGE    = 0x0040;
    static final short VARARGS   = 0x0080;
    static final short SYNTHETIC = 0x1000;
    static final short ENUM      = 0x4000;
    
    //taken from hotspot:/src/share/vm/prims/jvm.h, lines 1216-1237
    static final short JVM_RECOGNIZED_FIELD_MODIFIERS = 
        Modifier.PUBLIC    | Modifier.PRIVATE | Modifier.PROTECTED | 
        Modifier.STATIC    | Modifier.FINAL   | Modifier.VOLATILE  | 
        Modifier.TRANSIENT | ENUM             | SYNTHETIC;

    static final short JVM_RECOGNIZED_METHOD_MODIFIERS =
        Modifier.PUBLIC    | Modifier.PRIVATE | Modifier.PROTECTED    | 
        Modifier.STATIC    | Modifier.FINAL   | Modifier.SYNCHRONIZED | 
        BRIDGE             | VARARGS          | Modifier.NATIVE       |
        Modifier.ABSTRACT  | Modifier.STRICT  | SYNTHETIC;

    //taken from java.lang.invoke.MethodHandleNatives.Constants
    static final int  IS_METHOD            = 0x00010000; // method (not constructor)
    static final int  IS_CONSTRUCTOR       = 0x00020000; // constructor
    static final int  IS_FIELD             = 0x00040000; // field
    static final int  CALLER_SENSITIVE     = 0x00100000; // @CallerSensitive annotation detected
    static final int  REFERENCE_KIND_SHIFT = 24; // refKind
    static final int  REFERENCE_KIND_MASK  = 0x0F000000 >> REFERENCE_KIND_SHIFT;
    static final byte REF_getField         = 1;
    static final byte REF_getStatic        = 2;
    static final byte REF_putField         = 3;
    static final byte REF_invokeVirtual    = 5;
    static final byte REF_invokeStatic     = 6;
    static final byte REF_invokeSpecial    = 7;
    static final byte REF_invokeInterface  = 9;

    //taken from java.lang.invoke.MemberName
    static boolean isMethod(int flags) {
        return (flags & IS_METHOD) == IS_METHOD;
    }

    //taken from java.lang.invoke.MemberName
    static boolean isConstructor(int flags) {
        return (flags & IS_CONSTRUCTOR) == IS_CONSTRUCTOR;
    }

    //taken from java.lang.invoke.MemberName
    static boolean isField(int flags) {
        return (flags & IS_FIELD) == IS_FIELD;
    }
    
    //taken from java.lang.invoke.MemberName
    static boolean isStatic(int flags) {
        return Modifier.isStatic(flags);
    }

    //taken from java.lang.invoke.MemberName.getReferenceKind
    static boolean isInvokeInterface(int flags) {
        return ((byte) ((flags >>> REFERENCE_KIND_SHIFT) & REFERENCE_KIND_MASK)) == REF_invokeInterface;
    }

    //taken from java.lang.invoke.MemberName.getReferenceKind
    static boolean isSetter(int flags) {
        return ((byte) ((flags >>> REFERENCE_KIND_SHIFT) & REFERENCE_KIND_MASK)) > REF_getStatic;
    }
    
    //do not instantiate!
    private Util() {
        throw new AssertionError();
    }
}

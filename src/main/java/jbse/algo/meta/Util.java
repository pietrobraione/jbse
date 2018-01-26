package jbse.algo.meta;

import static jbse.bc.Signatures.JAVA_METHODHANDLE_INVOKEBASIC;
import static jbse.bc.Signatures.JAVA_METHODHANDLE_LINKTOINTERFACE;
import static jbse.bc.Signatures.JAVA_METHODHANDLE_LINKTOSPECIAL;
import static jbse.bc.Signatures.JAVA_METHODHANDLE_LINKTOSTATIC;
import static jbse.bc.Signatures.JAVA_METHODHANDLE_LINKTOVIRTUAL;

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
    
    //do not instantiate!
    private Util() {
        throw new AssertionError();
    }
}

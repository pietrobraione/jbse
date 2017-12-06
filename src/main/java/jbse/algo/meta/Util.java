package jbse.algo.meta;

import static jbse.bc.Signatures.JAVA_METHODHANDLE_INVOKEBASIC;
import static jbse.bc.Signatures.JAVA_METHODHANDLE_LINKTOINTERFACE;
import static jbse.bc.Signatures.JAVA_METHODHANDLE_LINKTOSPECIAL;
import static jbse.bc.Signatures.JAVA_METHODHANDLE_LINKTOSTATIC;
import static jbse.bc.Signatures.JAVA_METHODHANDLE_LINKTOVIRTUAL;

import jbse.bc.Signature;

class Util {
    static boolean isSignaturePolymorphicMethodIntrinsic(Signature methodSignature) {
        final Signature methodSignatureNoDescriptor = new Signature(methodSignature.getClassName(), null, methodSignature.getName());
        return 
        (JAVA_METHODHANDLE_INVOKEBASIC.equals(methodSignatureNoDescriptor) ||
        JAVA_METHODHANDLE_LINKTOINTERFACE.equals(methodSignatureNoDescriptor) ||
        JAVA_METHODHANDLE_LINKTOSPECIAL.equals(methodSignatureNoDescriptor) ||
        JAVA_METHODHANDLE_LINKTOSTATIC.equals(methodSignatureNoDescriptor) ||
        JAVA_METHODHANDLE_LINKTOVIRTUAL.equals(methodSignatureNoDescriptor));
    }
    
    //do not instantiate!
    private Util() {
        throw new AssertionError();
    }
}

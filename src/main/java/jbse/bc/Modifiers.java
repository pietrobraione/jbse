package jbse.bc;

import java.lang.reflect.Modifier;

import jbse.bc.exc.MethodNotFoundException;

/**
 * Class containing constants and methods to query the
 * modifiers of reflective objects.
 * 
 * @author Pietro Braione
 *
 */
public final class Modifiers {
    //taken from java.lang.reflect.Modifier
    public static final short BRIDGE    = 0x0040;
    public static final short VARARGS   = 0x0080;
    public static final short SYNTHETIC = 0x1000;
    public static final short ENUM      = 0x4000;
    
    //taken from hotspot:/src/share/vm/prims/jvm.h, lines 1216-1237
    public static final short JVM_RECOGNIZED_FIELD_MODIFIERS = 
        Modifier.PUBLIC    | Modifier.PRIVATE | Modifier.PROTECTED | 
        Modifier.STATIC    | Modifier.FINAL   | Modifier.VOLATILE  | 
        Modifier.TRANSIENT | ENUM             | SYNTHETIC;

    public static final short JVM_RECOGNIZED_METHOD_MODIFIERS =
        Modifier.PUBLIC    | Modifier.PRIVATE | Modifier.PROTECTED    | 
        Modifier.STATIC    | Modifier.FINAL   | Modifier.SYNCHRONIZED | 
        BRIDGE             | VARARGS          | Modifier.NATIVE       |
        Modifier.ABSTRACT  | Modifier.STRICT  | SYNTHETIC;

    //taken from java.lang.invoke.MethodHandleNatives.Constants
    public static final int  IS_METHOD            = 0x00010000; // method (not constructor)
    public static final int  IS_CONSTRUCTOR       = 0x00020000; // constructor
    public static final int  IS_FIELD             = 0x00040000; // field
    public static final int  IS_TYPE              = 0x00080000; // nested type
    public static final int  CALLER_SENSITIVE     = 0x00100000; // @CallerSensitive annotation detected
    public static final int  REFERENCE_KIND_SHIFT = 24; // refKind
    public static final int  REFERENCE_KIND_MASK  = 0x0F000000 >> REFERENCE_KIND_SHIFT;
    public static final int  SEARCH_SUPERCLASSES  = 0x00100000;
    public static final int  SEARCH_INTERFACES    = 0x00200000;
    public static final byte REF_getField         = 1;
    public static final byte REF_getStatic        = 2;
    public static final byte REF_putField         = 3;
    public static final byte REF_putStatic        = 4;
    public static final byte REF_invokeVirtual    = 5;
    public static final byte REF_invokeStatic     = 6;
    public static final byte REF_invokeSpecial    = 7;
    public static final byte REF_newInvokeSpecial = 8;
    public static final byte REF_invokeInterface  = 9;

    //taken from java.lang.invoke.MemberName
    public static boolean isMethod(int flags) {
        return (flags & IS_METHOD) == IS_METHOD;
    }

    //taken from java.lang.invoke.MemberName
    public static boolean isConstructor(int flags) {
        return (flags & IS_CONSTRUCTOR) == IS_CONSTRUCTOR;
    }

    //taken from java.lang.invoke.MemberName
    public static boolean isField(int flags) {
        return (flags & IS_FIELD) == IS_FIELD;
    }
    
    //taken from java.lang.invoke.MemberName
    public static boolean isStatic(int flags) {
        return Modifier.isStatic(flags);
    }

    //taken from java.lang.invoke.MemberName.getReferenceKind
    public static boolean isInvokeInterface(int flags) {
        return ((byte) ((flags >>> REFERENCE_KIND_SHIFT) & REFERENCE_KIND_MASK)) == REF_invokeInterface;
    }

    //taken from java.lang.invoke.MemberName.getReferenceKind
    public static boolean isSetter(int flags) {
        return ((byte) ((flags >>> REFERENCE_KIND_SHIFT) & REFERENCE_KIND_MASK)) > REF_getStatic;
    }
    
    public static boolean methodHandleKindIsField(int refKind) {
    	return (REF_getField <= refKind && refKind <= REF_putStatic);
    }
    
	//The check has some code taken from CallInfo::CallInfo(Method*, Klass*)
	//for unreflecting the method, see hotspot:/src/share/vm/interpreter/linkResolver.cpp,
	//that is invoked by MethodHandles::init_MemberName at row 169, 
	//hotspot:/src/share/vm/prims/methodHandles.cpp (note that we skip
	//the case miranda+default because they seem specific to the
	//Hotspot case)
    public static int getMemberNameFlagsMethod(ClassFile cfMethod, Signature methodSignature) throws MethodNotFoundException {
		int flags = (short) (((short) cfMethod.getMethodModifiers(methodSignature)) & JVM_RECOGNIZED_METHOD_MODIFIERS);
		if (canBeStaticallyBound(cfMethod, methodSignature)) {
			if (cfMethod.isMethodStatic(methodSignature)) {
				flags |= IS_METHOD      | (REF_invokeStatic  << REFERENCE_KIND_SHIFT);
			} else if ("<init>".equals(methodSignature.getName()) || "<clinit>".equals(methodSignature.getName())) {
				flags |= IS_CONSTRUCTOR | (REF_invokeSpecial << REFERENCE_KIND_SHIFT);
			} else { //it is private (invokespecial) or is final (invokevirtual)
				flags |= IS_METHOD      | (REF_invokeSpecial << REFERENCE_KIND_SHIFT);
			}
		} else if (cfMethod.isInterface()) {
			flags |= IS_METHOD | (REF_invokeInterface << REFERENCE_KIND_SHIFT);
		} else {
			flags |= IS_METHOD | (REF_invokeVirtual << REFERENCE_KIND_SHIFT);
		}
		
        if (cfMethod.isMethodCallerSensitive(methodSignature)) {
            flags |= CALLER_SENSITIVE;
        }
        
        return flags;
    }
    	
	private static boolean canBeStaticallyBound(ClassFile cfMethod, Signature methodSignature) throws MethodNotFoundException {
		//a default method cannot be statically bound
		if (cfMethod.isInterface() && !cfMethod.isMethodAbstract(methodSignature)) {
			return false;
		}
		
		//a final method can be statically bound
		if (cfMethod.isMethodFinal(methodSignature) || cfMethod.isFinal()) {
			return true;
		}
		
		//a static method is statically bound (invokestatic)
		if (cfMethod.isMethodStatic(methodSignature)) {
			return true;
		}
		
		//a constructor is statically bound (invokespecial)
		if ("<init>".equals(methodSignature.getName()) || "<clinit>".equals(methodSignature.getName())) {
			return true;
		}
		
		//a private method is statically bound (invokespecial)
		if (cfMethod.isMethodPrivate(methodSignature)) {
			return true;
		}
		
		//no other method can be statically bound
		return false;
	}
		
	/** Do not instantiate! */
	private Modifiers() {
		//nothing to do
	}
}

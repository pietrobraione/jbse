package jbse.algo.meta;

import static jbse.algo.Util.failExecution;
import static jbse.bc.Signatures.JAVA_METHODHANDLE_INVOKEBASIC;
import static jbse.bc.Signatures.JAVA_METHODHANDLE_LINKTOINTERFACE;
import static jbse.bc.Signatures.JAVA_METHODHANDLE_LINKTOSPECIAL;
import static jbse.bc.Signatures.JAVA_METHODHANDLE_LINKTOSTATIC;
import static jbse.bc.Signatures.JAVA_METHODHANDLE_LINKTOVIRTUAL;
import static jbse.common.Type.INT;

import java.lang.reflect.Modifier;

import jbse.algo.InterruptException;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.bc.exc.MethodNotFoundException;
import jbse.common.exc.ClasspathException;
import jbse.mem.Array;
import jbse.mem.Instance;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.Value;

class Util {
	static final long INVALID_FILE_ID = -1L; //luckily, both an invalid file descriptor and an invalid file handle are -1
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
    static final int  IS_TYPE              = 0x00080000; // nested type
    static final int  CALLER_SENSITIVE     = 0x00100000; // @CallerSensitive annotation detected
    static final int  REFERENCE_KIND_SHIFT = 24; // refKind
    static final int  REFERENCE_KIND_MASK  = 0x0F000000 >> REFERENCE_KIND_SHIFT;
    static final int  SEARCH_SUPERCLASSES  = 0x00100000;
    static final int  SEARCH_INTERFACES    = 0x00200000;
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
    
	//The check has some code taken from CallInfo::CallInfo(Method*, Klass*)
	//for unreflecting the method, see hotspot:/src/share/vm/interpreter/linkResolver.cpp,
	//that is invoked by MethodHandles::init_MemberName at row 169, 
	//hotspot:/src/share/vm/prims/methodHandles.cpp (note that we skip
	//the case miranda+default because they seem specific to the
	//Hotspot case)
    static int getMemberNameFlagsMethod(ClassFile cfMethod, Signature methodSignature) throws MethodNotFoundException {
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
	
	@FunctionalInterface
	interface ErrorAction {
		void doIt(String s) throws InterruptException, SymbolicValueNotAllowedException, ClasspathException;
	}

    static final ErrorAction OK                                             = msg -> { };
	static final ErrorAction FAIL_JBSE                                      = msg -> { failExecution(msg); };
	static final ErrorAction INTERRUPT_SYMBOLIC_VALUE_NOT_ALLOWED_EXCEPTION = msg -> { throw new SymbolicValueNotAllowedException(msg); };

	static Instance getInstance(State state, Value ref, String methodName, String paramName, 
	ErrorAction whenNoRef, ErrorAction whenNull, ErrorAction whenUnresolved)
	throws InterruptException, SymbolicValueNotAllowedException, ClasspathException, FrozenStateException {
		//TODO handle cast errors
		if (ref == null) {
			whenNoRef.doIt("Unexpected null value while accessing " + paramName + " parameter to " + methodName + ".");
			return null;
		}
		final Reference theReference = (Reference) ref;
		if (state.isNull(theReference)) {
			whenNull.doIt("The " + paramName + " parameter to " + methodName + " was null.");
			return null;
		}
		final Instance theInstance = (Instance) state.getObject(theReference);
		if (theInstance == null) {
			whenUnresolved.doIt("The " + paramName + " parameter to " + methodName + " was an unresolved symbolic reference on the operand stack.");
			return null;
		}
		return theInstance;
	}

	static Array getArray(State state, Value ref, String methodName, String paramName, 
	ErrorAction whenNoRef, ErrorAction whenNull, ErrorAction whenUnresolved, 
	ErrorAction whenNotSimple)
	throws InterruptException, SymbolicValueNotAllowedException, ClasspathException, FrozenStateException {
		//TODO handle cast errors
		if (ref == null) {
			whenNoRef.doIt("Unexpected null value while accessing " + paramName + " parameter to " + methodName + ".");
			return null;
		}
		final Reference theReference = (Reference) ref;
		if (state.isNull(theReference)) {
			whenNull.doIt("The " + paramName + " parameter to " + methodName + " was null.");
			return null;
		}
		final Array theArray = (Array) state.getObject(theReference);
		if (theArray == null) {
			whenUnresolved.doIt("The " + paramName + " parameter to " + methodName + " was an unresolved symbolic reference on the operand stack.");
			return null;
		}
		if (!theArray.isSimple()) {
			whenNotSimple.doIt("The " + paramName + " parameter to " + methodName + " was not a simple array.");
		}
		return theArray;
	}
	
	static Primitive getInteger(State state, Value intVal, String methodName, String paramName, 
	ErrorAction whenNull, ErrorAction whenNotPrimitiveInteger, ErrorAction whenSymbolic) 
	throws SymbolicValueNotAllowedException, InterruptException, ClasspathException, FrozenStateException {
		if (intVal == null) {
			whenNull.doIt("Unexpected null value while accessing " + paramName + " parameter to " + methodName + ".");
			return null;
		}
		if (!(intVal instanceof Primitive)) {
			whenNotPrimitiveInteger.doIt("The " + paramName + " parameter to " + methodName + " was not a primitive or an integer value.");
			return null;
		}
		final Primitive thePrimitive = (Primitive) intVal;
		if (thePrimitive.getType() != INT) {
			whenNotPrimitiveInteger.doIt("The " + paramName + " parameter to " + methodName + " was not a primitive or an integer value.");
			return null;
		}
		
		if (thePrimitive.isSymbolic()) {
			whenSymbolic.doIt("The " + paramName + " parameter to " + methodName + " was not a concrete integer value.");
		}
		
		return thePrimitive;
	}

    //do not instantiate!
    private Util() {
        throw new AssertionError();
    }
}

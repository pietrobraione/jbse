package jbse.algo.meta;

import static jbse.algo.Util.failExecution;
import static jbse.common.Type.INT;

import jbse.algo.InterruptException;
import jbse.algo.exc.SymbolicValueNotAllowedException;
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

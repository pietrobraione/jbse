package jbse.algo.meta;

import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.algo.Util.valueString;
import static jbse.bc.Offsets.INVOKESPECIALSTATICVIRTUAL_OFFSET;
import static jbse.bc.Signatures.CLASS_NOT_FOUND_EXCEPTION;

import jbse.algo.Algorithm;
import jbse.algo.ExecutionContext;
import jbse.algo.exc.InterruptException;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;

public class Algo_JAVA_CLASS_GETPRIMITIVECLASS implements Algorithm {
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException, SymbolicValueNotAllowedException, 
	InterruptException {
		try {			
			//gets the binary name of the primitive type and converts it to a string
            final Reference typeNameRef = (Reference) state.popOperand();
			final String typeName = valueString(state, typeNameRef);
			if (typeName == null) {
				throw new SymbolicValueNotAllowedException("the String parameter to java.lang.Class.getPrimitiveClass method cannot be a symbolic String");
			}

			//gets the instance of the class
			state.ensureInstance_JAVA_CLASS_primitive(typeName);
			final Reference classRef = state.referenceToInstance_JAVA_CLASS_primitive(typeName);
			state.pushOperand(classRef);
        } catch (ClassFileNotFoundException e) {
            throwNew(state, CLASS_NOT_FOUND_EXCEPTION);  //this is how Hotspot behaves
            throw InterruptException.getInstance();
		} catch (OperandStackEmptyException | ClassCastException e) {
		    throwVerifyError(state);
		    throw InterruptException.getInstance();
		}

		try {
			state.incPC(INVOKESPECIALSTATICVIRTUAL_OFFSET);
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
		}
		throw InterruptException.getInstance();
	}
}

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
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;

public class Algo_JAVA_CLASS_ISINSTANCE implements Algorithm {
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException, SymbolicValueNotAllowedException, 
	InterruptException {
		try {			
			//gets the binary name of the primitive type and converts it to a string
            final Reference javaClassRef = (Reference) state.popOperand();
            final Objekt javaClassObject = (Objekt) state.getObject(javaClassRef);
            //TODO
		} catch (OperandStackEmptyException | ClassCastException e) {
		    throwVerifyError(state);
            throw new InterruptException();
		}

		try {
			state.incPC(INVOKESPECIALSTATICVIRTUAL_OFFSET);
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
		}
        throw new InterruptException();
	}
}

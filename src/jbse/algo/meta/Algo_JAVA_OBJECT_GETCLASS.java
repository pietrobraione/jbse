package jbse.algo.meta;

import static jbse.algo.Util.ensureClassInstance;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.INVOKESPECIALSTATICVIRTUAL_OFFSET;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;

import jbse.algo.Algorithm;
import jbse.algo.ExecutionContext;
import jbse.algo.exc.InterruptException;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.exc.DecisionException;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;

public class Algo_JAVA_OBJECT_GETCLASS implements Algorithm {
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException, SymbolicValueNotAllowedException, 
	ClasspathException, DecisionException, InterruptException {
		try {			
			//gets the "this" object and the name of its class
            final Reference thisRef = (Reference) state.top();
            if (state.isNull(thisRef)) {
                throwNew(state, NULL_POINTER_EXCEPTION);
            }
			final Objekt thisObj = state.getObject(thisRef);
			if (thisObj == null) {
				throw new UnexpectedInternalException("symbolic reference on operand stack");
			}
			final String className = thisObj.getType();

			//gets the instance of the class of the "this" object
			ensureClassInstance(state, className, ctx.decisionProcedure);
			final Reference classRef = state.referenceToClassInstance(className);
			state.pop();
			state.push(classRef);
        } catch (ClassFileNotAccessibleException e) {
            throwNew(state, ILLEGAL_ACCESS_ERROR);
            return;
		} catch (OperandStackEmptyException | ClassCastException e) {
		    throwVerifyError(state);
		    return;
		} catch (BadClassFileException e) {
		    //this should never happen
		    throw new UnexpectedInternalException(e);
		}

		try {
			state.incPC(INVOKESPECIALSTATICVIRTUAL_OFFSET);
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
		}
	}
}

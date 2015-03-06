package jbse.algo.meta;

import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.INVOKESPECIALSTATICVIRTUAL_OFFSET;
import static jbse.bc.Signatures.ARRAY_STORE_EXCEPTION;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;

import jbse.algo.Algorithm;
import jbse.algo.ExecutionContext;
import jbse.algo.exc.InterruptException;
import jbse.mem.Array;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Primitive;
import jbse.val.Reference;

public class Algo_JAVA_SYSTEM_ARRAYCOPY implements Algorithm {
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException, InterruptException {
	    final Primitive srcPos, destPos, length;
        final Reference src, dest;
	    try {
	        length = (Primitive) state.popOperand();
	        destPos = (Primitive) state.popOperand();
	        dest = (Reference) state.popOperand();
	        srcPos = (Primitive) state.popOperand();
	        src = (Reference) state.popOperand();
        } catch (OperandStackEmptyException e) {
            throwVerifyError(state);
            throw new InterruptException();
        }
	    
	    if (state.isNull(src) || state.isNull(dest)) {
	        throwNew(state, NULL_POINTER_EXCEPTION);
            throw new InterruptException();
	    }

	    final Array srcArray, destArray;
	    try {
	        srcArray = (Array) state.getObject(src);
	        destArray = (Array) state.getObject(dest);
	    } catch (ClassCastException e) {
            throwNew(state, ARRAY_STORE_EXCEPTION);
            throw new InterruptException();
	    }
	    
	    //TODO
	    
        try {
			state.incPC(INVOKESPECIALSTATICVIRTUAL_OFFSET);
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
		}
	}
}

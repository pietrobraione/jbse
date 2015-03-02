package jbse.algo;

import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.NEWARRAY_OFFSET;

import jbse.common.Type;
import jbse.dec.exc.DecisionException;
import jbse.mem.Array;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Primitive;

final class Algo_NEWARRAY extends MultipleStateGenerator_XNEWARRAY implements Algorithm {
    
    @Override
	public void exec(State state, ExecutionContext ctx) 
	throws DecisionException, ThreadStackEmptyException {
    	//determines the array's type
        try {
        	final int memberTypeEncoded = state.getInstruction(1);
        	final char memberType = Array.checkAndReturnArrayPrimitive(memberTypeEncoded);
        	if (memberType == Type.ERROR) {
                throwVerifyError(state);
        		return;
        	}
        	this.arrayType = "" + Type.ARRAYOF + memberType;
        } catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
            return;
        }
		
        //pops the array's length from the operand stack
		final Primitive length;
		try {
		    length = (Primitive) state.popOperand();
	        //TODO length check
		} catch (OperandStackEmptyException | ClassCastException e) {
            throwVerifyError(state);
            return;
		}
		
		//generates the next states
    	this.state = state;
    	this.ctx = ctx;
    	this.pcOffset = NEWARRAY_OFFSET;
    	//see above for this.arrayType
    	this.dimensionsCounts = new Primitive[] { length };
    	generateStates();
	} 
}

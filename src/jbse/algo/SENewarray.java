package jbse.algo;

import static jbse.bc.Offsets.NEWARRAY_OFFSET;
import jbse.Type;
import jbse.Util;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.dec.DecisionException;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.OperandStackEmptyException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.Array;
import jbse.mem.Primitive;
import jbse.mem.State;

final class SENewarray extends MultipleStateGeneratorNewarray implements Algorithm {
	public void exec(State state, ExecutionContext ctx) 
	throws DecisionException, ThreadStackEmptyException, 
	OperandStackEmptyException, UnexpectedInternalException {
    	//determines the array's type
        {
        	int type;
        	try {
        		type = state.getInstruction(1);
        	} catch (InvalidProgramCounterException e) {
        		state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
        		return;
        	}
        	final char memberType = Array.checkAndReturnArrayPrimitive(type);
        	if (memberType == Type.ERROR) {
        		state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
        		return;
        	}
        	this.arrayType = "" + Type.ARRAYOF + memberType;
        }
		
        //pops the array's length from the operand stack
		final Primitive length = (Primitive) state.pop();
		//TODO length type check
		
		//generates the next states
    	this.state = state;
    	this.ctx = ctx;
    	this.pcOffset = NEWARRAY_OFFSET;
    	//see above for this.arrayType
    	this.dimensionsCounts = new Primitive[] { length };
    	this.generateStates();
	} 
}

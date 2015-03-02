package jbse.algo;

import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.XCONST_OFFSET;

import jbse.common.Type;
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;

class Algo_XCONST_Y implements Algorithm {
    char type;
    int value; //that's enough since all constants are small
    
    @Override
    public void exec(State state, ExecutionContext ctx) 
    throws ThreadStackEmptyException {
        if (this.type == Type.INT) {
        	state.pushOperand(state.getCalculator().valInt(value));
        } else if (this.type == Type.DOUBLE) {
        	state.pushOperand(state.getCalculator().valDouble((double) value));
        } else if (this.type == Type.FLOAT) {
        	state.pushOperand(state.getCalculator().valFloat((float) value));
        } else if (this.type == Type.LONG) {
        	state.pushOperand(state.getCalculator().valLong((long) value));
        } else {
            throw new UnexpectedInternalException("const bytecodes with type " + type + " do not exist");
        }

        try {
			state.incPC(XCONST_OFFSET);
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
		}
    } 
}
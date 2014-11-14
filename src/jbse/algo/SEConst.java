package jbse.algo;

import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.XCONST_OFFSET;

import jbse.common.Type;
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;

class SEConst implements Algorithm {
    char type;
    int val; //that's enough since all constants are small
    
    @Override
    public void exec(State state, ExecutionContext ctx) 
    throws ThreadStackEmptyException {
        if (type == Type.INT) {
        	state.push(state.getCalculator().valInt(val));
        } else if (type == Type.DOUBLE) {
        	state.push(state.getCalculator().valDouble((double) val));
        } else if (type == Type.FLOAT) {
        	state.push(state.getCalculator().valFloat((float) val));
        } else if (type == Type.LONG) {
        	state.push(state.getCalculator().valLong((long) val));
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
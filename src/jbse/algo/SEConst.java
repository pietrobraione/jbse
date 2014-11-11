package jbse.algo;

import static jbse.bc.Offsets.XCONST_OFFSET;
import jbse.Type;
import jbse.Util;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.State;


class SEConst implements Algorithm {
    char type;
    int val; //that's enough since all constants are small
    
    @Override
    public void exec(State state, ExecutionContext ctx) 
    throws ThreadStackEmptyException, UnexpectedInternalException {
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
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
		}
    } 
}
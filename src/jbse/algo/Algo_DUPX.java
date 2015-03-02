package jbse.algo;

import static jbse.algo.Util.throwVerifyError;

import jbse.common.Type;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Value;

final class Algo_DUPX implements Algorithm {
    /** {@code true} for dup, {@code false} for dup2. */
    boolean cat1;

    public Algo_DUPX() { }

    @Override
    public void exec(State state, ExecutionContext ctx) throws ThreadStackEmptyException {
        boolean error = false;

        try {
            final Value tmp1 = state.popOperand();
            if (this.cat1) {
                //dup
                if (Type.isCat_1(tmp1.getType())) {
                    //dup semantics
                    state.pushOperand(tmp1);
                    state.pushOperand(tmp1);
                } else {
                    //dup: incorrect operand
                    error = true;
                }
            } else {
                //dup2
                if (Type.isCat_1(tmp1.getType())) {
                    //dup2: we need a second operand 
                    final Value tmp2 = state.popOperand();
                    if (Type.isCat_1(tmp2.getType())) {
                        //dup2 form 1 semantics
                        state.pushOperand(tmp2);
                        state.pushOperand(tmp1);
                        state.pushOperand(tmp2);
                        state.pushOperand(tmp1);
                    } else {
                        //dup2: incorrect operand 2
                        error = true;
                    }
                } else {
                    //dup2 form 2 semantics 
                    state.pushOperand(tmp1);
                    state.pushOperand(tmp1);
                }
            }
		} catch (OperandStackEmptyException e) {
		    error = true;
		}

        //common updates
        if (error) {
            throwVerifyError(state);
            return;
        }

		try {
			state.incPC();
		} catch (InvalidProgramCounterException e) {
		    throwVerifyError(state);
		}
	}
}

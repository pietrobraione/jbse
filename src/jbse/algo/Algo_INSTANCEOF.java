package jbse.algo;

import static jbse.algo.Util.throwVerifyError;

import jbse.algo.exc.InterruptException;
import jbse.mem.State;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;

final class Algo_INSTANCEOF extends Algo_CASTINSTANCEOF {
    @Override
    protected void complete(State state, boolean isSubclass)
    throws ThreadStackEmptyException, InterruptException {
        try {
        //pops the checked reference and calculates the result
        final Reference tmpValue = (Reference) state.pop();
        if (!state.isNull(tmpValue) && isSubclass) { 
            state.push(state.getCalculator().valInt(1));
        } else { 
            state.push(state.getCalculator().valInt(0));
        }
        } catch (OperandStackEmptyException | ClassCastException e) {
            throwVerifyError(state);
            throw new InterruptException();
        }
    }
}

package jbse.algo;

import static jbse.algo.Util.throwVerifyError;

import jbse.algo.exc.InterruptException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.State;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;

final class Algo_INSTANCEOF extends Algo_CASTINSTANCEOF {
    @Override
    protected void complete(State state, boolean isSubclass)
    throws InterruptException {
        try {
            //pops the checked reference and calculates the result
            final Reference tmpValue = (Reference) state.popOperand();
            if (!state.isNull(tmpValue) && isSubclass) { //null is not an instance of anything
                state.pushOperand(state.getCalculator().valInt(1));
            } else { 
                state.pushOperand(state.getCalculator().valInt(0));
            }
        } catch (ClassCastException e) {
            throwVerifyError(state);
            throw InterruptException.getInstance();
        } catch (ThreadStackEmptyException | OperandStackEmptyException e) {
            //should never happen
            throw new UnexpectedInternalException(e);
        }
    }
}

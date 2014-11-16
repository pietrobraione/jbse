package jbse.algo;


import jbse.mem.State;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;

final class SEInstanceof extends SECastInstanceof {
    @Override
    protected boolean complete(State state, boolean isSubclass)
    throws ThreadStackEmptyException, OperandStackEmptyException {
        //pops the checked reference and calculates the result
        final Reference tmpValue = (Reference) state.pop();
        if (!state.isNull(tmpValue) && isSubclass) { 
            state.push(state.getCalculator().valInt(1));
        } else { 
            state.push(state.getCalculator().valInt(0));
        }
        return false;
    }
}

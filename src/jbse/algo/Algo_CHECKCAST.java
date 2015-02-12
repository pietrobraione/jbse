package jbse.algo;

import static jbse.algo.Util.throwNew;
import static jbse.bc.Signatures.CLASS_CAST_EXCEPTION;

import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;

final class Algo_CHECKCAST extends Algo_CASTINSTANCEOF {
    @Override
    protected boolean complete(State state, boolean isSubclass) 
    throws ThreadStackEmptyException {
        //if the check fails throws a ClassCastException
        if (!isSubclass) {
            throwNew(state, CLASS_CAST_EXCEPTION);
            return true;
        }
        return false;
    }
}
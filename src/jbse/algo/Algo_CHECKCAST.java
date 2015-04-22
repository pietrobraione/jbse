package jbse.algo;

import static jbse.algo.Util.throwNew;
import static jbse.bc.Signatures.CLASS_CAST_EXCEPTION;

import jbse.algo.exc.InterruptException;
import jbse.mem.State;

final class Algo_CHECKCAST extends Algo_CASTINSTANCEOF {
    @Override
    protected void complete(State state, boolean isSubclass) 
    throws InterruptException {
        //if the check fails throws a ClassCastException
        if (!isSubclass) {
            throwNew(state, CLASS_CAST_EXCEPTION);
            throw InterruptException.getInstance();
        }
    }
}
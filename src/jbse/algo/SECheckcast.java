package jbse.algo;

import static jbse.algo.Util.CLASS_CAST_EXCEPTION;
import static jbse.algo.Util.createAndThrow;

import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;

final class SECheckcast extends SECastInstanceof {
    @Override
    protected boolean complete(State state, boolean isSubclass) 
    throws ThreadStackEmptyException {
        //if the check fails throws a ClassCastException
        if (!isSubclass) {
            createAndThrow(state, CLASS_CAST_EXCEPTION);
            return true;
        }
        return false;
    }
}
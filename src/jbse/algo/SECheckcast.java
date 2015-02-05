package jbse.algo;

import static jbse.algo.Util.createAndThrowObject;
import static jbse.bc.Signatures.CLASS_CAST_EXCEPTION;

import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;

final class SECheckcast extends SECastInstanceof {
    @Override
    protected boolean complete(State state, boolean isSubclass) 
    throws ThreadStackEmptyException {
        //if the check fails throws a ClassCastException
        if (!isSubclass) {
            createAndThrowObject(state, CLASS_CAST_EXCEPTION);
            return true;
        }
        return false;
    }
}
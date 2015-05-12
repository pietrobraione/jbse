package jbse.algo;

import jbse.algo.exc.CannotManageStateException;
import jbse.common.exc.ClasspathException;
import jbse.dec.exc.DecisionException;
import jbse.mem.State;

@FunctionalInterface
public interface BytecodeCooker {
    void cook(State state) 
    throws DecisionException, ClasspathException, 
    CannotManageStateException, InterruptException;
}

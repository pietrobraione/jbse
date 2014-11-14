package jbse.algo;

import jbse.dec.exc.DecisionException;
import jbse.mem.State;
import jbse.val.Value;

/**
 * Strategy for writing back a value to the source 
 * container array during resolution of a symbolic
 * reference. 
 * 
 * @author Pietro Braione
 */
@FunctionalInterface
public interface WriteBackToSourceStrategy {
	void writeBack(State s, Value toWriteBack) throws DecisionException;
}

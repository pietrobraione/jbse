package jbse.algo;

import static jbse.algo.Util.ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION;
import static jbse.algo.Util.NULL_POINTER_EXCEPTION;
import static jbse.bc.Offsets.XALOADSTORE_OFFSET;

import java.util.Iterator;

import jbse.Util;
import jbse.dec.DecisionProcedureAlgorithms.Outcome;
import jbse.exc.algo.CannotManageStateException;
import jbse.exc.bc.ClassFileNotFoundException;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.dec.DecisionException;
import jbse.exc.dec.InvalidInputException;
import jbse.exc.mem.ContradictionException;
import jbse.exc.mem.InvalidOperandException;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.InvalidTypeException;
import jbse.exc.mem.OperandStackEmptyException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.Array;
import jbse.mem.Primitive;
import jbse.mem.Reference;
import jbse.mem.State;
import jbse.mem.Value;
import jbse.tree.DecisionAlternativeAstore;

/**
 * Command managing all the *astore (store into array) bytecodes 
 * ([a/c/b/d/f/i/l/s]astore). It decides over access index 
 * membership (inbound vs. outbound), which is a sheer numeric 
 * decision.
 * 
 * @author Pietro Braione
 *
 */
class SEAstore extends MultipleStateGenerator<DecisionAlternativeAstore> implements Algorithm {
	public SEAstore() {
		super(DecisionAlternativeAstore.class);
	}
	
    public void exec(final State state, final ExecutionContext ctx) 
    throws DecisionException, CannotManageStateException, 
    ThreadStackEmptyException, OperandStackEmptyException,  
    ContradictionException, UnexpectedInternalException {
        final Value value = state.pop();
        final Primitive index = (Primitive) state.pop();
        final Reference myObjectRef = (Reference) state.pop();
        if (state.isNull(myObjectRef)) {
        	//base-level throws NullPointerException 
	    	state.createThrowableAndThrowIt(NULL_POINTER_EXCEPTION);
	    	return;
        }

        //creates the Values that check whether the index
        //is in range or out of range w.r.t. the array
    	final Primitive inRange;
    	final Primitive outOfRange;
    	try {
    		final Array o = (Array) state.getObject(myObjectRef);
    		inRange = o.inRange(index);
    		outOfRange = o.outOfRange(index);
    	} catch (InvalidOperandException | InvalidTypeException e) {
    		//index is bad
    		state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
    		return;
    	}
        
		//generates the next states    	
    	this.ds = (results) -> {
    		final Outcome o = ctx.decisionProcedure.decideAstore(inRange, results);
    		return o;
		};
		
		this.srs = (State s, DecisionAlternativeAstore r) -> {
			if (r.isInRange()) {
				s.assume(ctx.decisionProcedure.simplify(inRange));
			} else {
				s.assume(ctx.decisionProcedure.simplify(outOfRange));
			}
		};
		
		this.sus = (State s, DecisionAlternativeAstore r) -> {
			if (r.isInRange()) {
				try {
					final Array arrayObj = (Array) s.getObject(myObjectRef);
					final Iterator<Array.AccessOutcomeIn> entries = arrayObj.set(index, value);
					ctx.decisionProcedure.completeArraySet(entries, index);
				} catch (InvalidOperandException | InvalidTypeException | InvalidInputException e) {
					//this should never happen
					throw new UnexpectedInternalException(e);
				}
				try {
					s.incPC(XALOADSTORE_OFFSET);
				} catch (InvalidProgramCounterException e) {
					s.createThrowableAndThrowIt(Util.VERIFY_ERROR);
				}
			} else {
				s.createThrowableAndThrowIt(ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION);
			}
		};

    	this.state = state;
    	this.ctx = ctx;
		try {
			generateStates();
		} catch (ClassFileNotFoundException | InvalidInputException | 
				InvalidTypeException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
    } 
}

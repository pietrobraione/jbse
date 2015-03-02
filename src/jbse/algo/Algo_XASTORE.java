package jbse.algo;

import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.XALOADSTORE_OFFSET;
import static jbse.bc.Signatures.ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;

import java.util.Iterator;

import jbse.algo.exc.CannotManageStateException;
import jbse.bc.exc.BadClassFileException;
import jbse.common.Type;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.DecisionProcedureAlgorithms.Outcome;
import jbse.dec.exc.DecisionException;
import jbse.dec.exc.InvalidInputException;
import jbse.mem.Array;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_XASTORE;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.Value;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * Command managing all the *astore (store into array) bytecodes 
 * ([a/c/b/d/f/i/l/s]astore). It decides over access index 
 * membership (inbound vs. outbound), which is a sheer numeric 
 * decision.
 * 
 * @author Pietro Braione
 *
 */
class Algo_XASTORE extends MultipleStateGenerator<DecisionAlternative_XASTORE> implements Algorithm {
	public Algo_XASTORE() {
		super(DecisionAlternative_XASTORE.class);
	}
	
	@Override
    public void exec(final State state, final ExecutionContext ctx) 
    throws DecisionException, CannotManageStateException, 
    ThreadStackEmptyException, ContradictionException {
        final Value value;
        final Primitive index;
        final Reference arrayRef;
        try {
            value = state.popOperand();
            index = (Primitive) state.popOperand();
            arrayRef = (Reference) state.popOperand();
        } catch (OperandStackEmptyException | ClassCastException e) {
            throwVerifyError(state);
            return;
        }
        
        //null check
        if (state.isNull(arrayRef)) {
            throwNew(state, NULL_POINTER_EXCEPTION);
	    	return;
        }

        //creates the Values that check whether the index
        //is in range or out of range w.r.t. the array;
        //moreover, converts the value in case of [b/c/s]astore
    	final Primitive inRange;
    	final Primitive outOfRange;
        final Value valueToStore;
    	try {
    	    final Array array = (Array) state.getObject(arrayRef);
    	    inRange = array.inRange(index);
    		outOfRange = array.outOfRange(index);
            final String arrayElemType = Type.getArrayMemberType(array.getType());
            if (Type.isPrimitive(arrayElemType) && !Type.isPrimitiveOpStack(arrayElemType.charAt(0))) {
                try {
                    valueToStore = ((Primitive) value).to(arrayElemType.charAt(0));
                } catch (InvalidTypeException e) {
                    //this should not happen
                    throw new UnexpectedInternalException(e);
                }
            } else {
                valueToStore = value;
            }
    	} catch (InvalidOperandException | InvalidTypeException | ClassCastException e) {
    		//index is bad or the reference does not point to an array
    	    throwVerifyError(state);
    		return;
    	}
        

        //generates the next states    	
    	this.ds = (results) -> {
    		final Outcome o = ctx.decisionProcedure.decide_XASTORE(inRange, results);
    		return o;
		};
		
		this.srs = (State s, DecisionAlternative_XASTORE r) -> {
			if (r.isInRange()) {
				s.assume(ctx.decisionProcedure.simplify(inRange));
			} else {
				s.assume(ctx.decisionProcedure.simplify(outOfRange));
			}
		};
		
		this.sus = (State s, DecisionAlternative_XASTORE r) -> {
			if (r.isInRange()) {
				try {
					final Array array = (Array) s.getObject(arrayRef);
					final Iterator<Array.AccessOutcomeIn> entries = array.set(index, valueToStore);
					ctx.decisionProcedure.completeArraySet(entries, index);
				} catch (InvalidOperandException | InvalidTypeException | 
				         InvalidInputException | ClassCastException e) {
					//this should never happen
					throw new UnexpectedInternalException(e);
				}
				try {
					s.incPC(XALOADSTORE_OFFSET);
				} catch (InvalidProgramCounterException e) {
				    throwVerifyError(s);
				}
			} else {
			    throwNew(s, ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION);
			}
		};

    	this.state = state;
    	this.ctx = ctx;
		try {
			generateStates();
		} catch (BadClassFileException | InvalidInputException | 
				InvalidTypeException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
    } 
}

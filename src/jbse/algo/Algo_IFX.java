package jbse.algo;

import static jbse.algo.Util.throwVerifyError;

import jbse.bc.exc.BadClassFileException;
import jbse.common.Type;
import jbse.common.Util;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.DecisionProcedureAlgorithms.Outcome;
import jbse.dec.exc.DecisionException;
import jbse.dec.exc.InvalidInputException;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_IFX;
import jbse.val.Operator;
import jbse.val.Primitive;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidOperatorException;
import jbse.val.exc.InvalidTypeException;

/**
 * Command managing all the "branch if integer comparison" bytecodes 
 * (if[eq/ge/gt/le/lt/ne], if_icmp[eq/ge/gt/le/lt/ne]). It decides over
 * the branch to be taken, a sheer numeric decision.
 * 
 * @author Pietro Braione
 *
 */
final class Algo_IFX extends MultipleStateGenerator<DecisionAlternative_IFX> implements Algorithm {
    boolean compareWithZero;
    Operator operator;
    
    public Algo_IFX() {
		super(DecisionAlternative_IFX.class);
	}
    
    @Override
    public void exec(State state, final ExecutionContext ctx) 
    throws DecisionException, ContradictionException, 
    ThreadStackEmptyException {
        //gets operands and calculates branch target
        final int branchOffset;
        try {
	        final byte tmp1 = state.getInstruction(1);
	        final byte tmp2 = state.getInstruction(2);
	        branchOffset = Util.byteCatShort(tmp1, tmp2);
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
			return;
		} //note that now the program counter points to the next instruction

        //takes operands from current frame's operand stack
        Primitive val1, val2;
        try {
            val1 = (Primitive) state.popOperand();
            if (this.compareWithZero) {
                val2 = state.getCalculator().valInt(0);
                //cast necessary because the Algo_XCMPY state space reduction  
                //trick spills nonint values to the operand stack.
                try {
                    if (Type.widens(val1.getType(), Type.INT)) {
                        val2 = val2.to(val1.getType());
                    } else {
                        val1 = val1.to(val2.getType());
                    }
                } catch (InvalidTypeException e) {
                    //this should never happen
                    throw new UnexpectedInternalException(e);
                }
            } else {
                //swaps the operands for comparison
                val2 = val1;
                val1 = (Primitive) state.popOperand();
            }
        } catch (OperandStackEmptyException | ClassCastException e) {
            throwVerifyError(state);
            return;
        }
    	
		final Primitive condition;
		try {
			condition = state.getCalculator().applyBinary(val1, this.operator, val2);
		} catch (InvalidOperandException | InvalidTypeException e) {
            throwVerifyError(state);
			return;
		} catch (InvalidOperatorException e) {
			throw new UnexpectedInternalException(e);
		}

    	this.ds = (result) -> {
    		final Outcome o = ctx.decisionProcedure.decide_IFX(condition, result);
    		return o;
		};
		
		this.srs = (State s, DecisionAlternative_IFX r) -> {
			final Primitive condTrue = (r.value() ? condition : condition.not());
			s.assume(ctx.decisionProcedure.simplify(condTrue));
		};
		
		this.sus = (State s, DecisionAlternative_IFX r) -> {
			try {
				if (r.value()) {
					s.incPC(branchOffset);
				} else {
					s.incPC(3);
				}
			} catch (InvalidProgramCounterException e) {
	            throwVerifyError(s);
			}
		};
		
		//generates all the states
    	this.state = state;
    	this.ctx = ctx;
		try {
			generateStates();
		} catch (InvalidInputException | InvalidTypeException | 
				BadClassFileException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
    }
}
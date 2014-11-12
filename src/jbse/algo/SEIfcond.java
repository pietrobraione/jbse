package jbse.algo;

import jbse.Type;
import jbse.Util;
import jbse.dec.DecisionProcedureAlgorithms.Outcome;
import jbse.exc.bc.ClassFileNotFoundException;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.dec.DecisionException;
import jbse.exc.dec.InvalidInputException;
import jbse.exc.mem.ContradictionException;
import jbse.exc.mem.InvalidOperandException;
import jbse.exc.mem.InvalidOperatorException;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.InvalidTypeException;
import jbse.exc.mem.OperandStackEmptyException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.Operator;
import jbse.mem.Primitive;
import jbse.mem.State;
import jbse.tree.DecisionAlternativeIf;

/**
 * Command managing all the "branch if integer comparison" bytecodes 
 * (if[eq/ge/gt/le/lt/ne], if_icmp[eq/ge/gt/le/lt/ne]). It decides over
 * the branch to be taken, a sheer numeric decision.
 * 
 * @author Pietro Braione
 *
 */
final class SEIfcond extends MultipleStateGenerator<DecisionAlternativeIf> implements Algorithm {
    boolean def;
    Operator operator;
    
    public SEIfcond() {
		super(DecisionAlternativeIf.class);
	}
    
    @Override
    public void exec(State state, final ExecutionContext ctx) 
    throws DecisionException, ContradictionException, 
    ThreadStackEmptyException, OperandStackEmptyException {
        //gets operands and calculates branch target
        final int branchOffset;
        try {
	        final byte tmp1 = state.getInstruction(1);
	        final byte tmp2 = state.getInstruction(2);
	        branchOffset = Util.byteCatShort(tmp1, tmp2);
		} catch (InvalidProgramCounterException e) {
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
			return;
		} //note that now the program counter points to the next instruction

        //takes operands from current frame's operand stack
        Primitive val1 = (Primitive) state.pop();
        Primitive val2;
        if (def) {
    		val2 = state.getCalculator().valInt(0);
    		//cast necessary because the SENcmp state space reduction  
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
            val1 = (Primitive) state.pop();
        }
        
    	
		final Primitive condition;
		try {
			condition = state.getCalculator().applyBinary(val1, operator, val2);
		} catch (InvalidOperandException | InvalidTypeException e) {
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
			return;
		} catch (InvalidOperatorException e) {
			throw new UnexpectedInternalException(e);
		}

    	this.ds = (result) -> {
    		final Outcome o = ctx.decisionProcedure.decideIf(condition, result);
    		return o;
		};
		
		this.srs = (State s, DecisionAlternativeIf r) -> {
			final Primitive condTrue = (r.value() ? condition : condition.not());
			s.assume(ctx.decisionProcedure.simplify(condTrue));
		};
		
		this.sus = (State s, DecisionAlternativeIf r) -> {
			try {
				if (r.value()) {
					s.incPC(branchOffset);
				} else {
					s.incPC(3);
				}
			} catch (InvalidProgramCounterException e) {
				s.createThrowableAndThrowIt(Util.VERIFY_ERROR);
			}
		};
		
		//generates all the states
    	this.state = state;
    	this.ctx = ctx;
		try {
			generateStates();
		} catch (InvalidInputException | InvalidTypeException | 
				ClassFileNotFoundException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
    }
}
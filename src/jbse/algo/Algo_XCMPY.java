package jbse.algo;

import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Opcodes.OP_IFEQ;
import static jbse.bc.Opcodes.OP_IFGE;
import static jbse.bc.Opcodes.OP_IFGT;
import static jbse.bc.Opcodes.OP_IFLE;
import static jbse.bc.Opcodes.OP_IFLT;
import static jbse.bc.Opcodes.OP_IFNE;

import jbse.bc.exc.BadClassFileException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.DecisionProcedureAlgorithms.Outcome;
import jbse.dec.exc.DecisionException;
import jbse.dec.exc.InvalidInputException;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_XCMPY;
import jbse.val.Calculator;
import jbse.val.Primitive;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidOperatorException;
import jbse.val.exc.InvalidTypeException;

/**
 * Command managing all the *cmp* commands ([d/f/l]cmp_[g/l]). 
 * It decides over a comparison between primitives (longs, 
 * doubles, floats), a sheer numeric decision.
 * 
 * @author Pietro Braione
 */
final class Algo_XCMPY extends MultipleStateGenerator<DecisionAlternative_XCMPY> implements Algorithm {
	public Algo_XCMPY() {
		super(DecisionAlternative_XCMPY.class);
	}
	
	@Override
	public void exec(State state, final ExecutionContext ctx) 
	throws DecisionException, ContradictionException, ThreadStackEmptyException {
		//TODO NaN cases for distinguishing [d/l]cmpg from [d/l]cmpl

		//takes operands from current frame's operand stack
        final Primitive val1, val2;
        try {
            val2 = (Primitive) state.popOperand();
            val1 = (Primitive) state.popOperand();
        } catch (OperandStackEmptyException | ClassCastException e) {
            throwVerifyError(state);
            return;
        }

		//since typically *cmp* is followed by an if[eq/ge/gt/le/lt/ne], 
		//which exploits only TWO of the THREE cases, we check whether
		//this is the case and avoid having a uselessly 50% bigger
		//state space
		final int nextBytecode;
		try {
			nextBytecode = state.getInstruction(1);
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
			return;
		}
		
		//executes
		if (nextBytecode == OP_IFEQ
			|| nextBytecode == OP_IFGE
			|| nextBytecode == OP_IFGT
			|| nextBytecode == OP_IFLE
			|| nextBytecode == OP_IFLT
			|| nextBytecode == OP_IFNE) {
			//fast (and common) case: pushes the difference between val1 and val2
			//since checking it is equivalent to checking the true outcome of the bytecode 
			try {
				state.pushOperand(val1.sub(val2));
			} catch (InvalidOperandException | InvalidTypeException e) {
	            throwVerifyError(state);
				return;
			}
			try {
				state.incPC();
			} catch (InvalidProgramCounterException e) {
	            throwVerifyError(state);
			}
		} else {
			final Calculator calc = state.getCalculator();
			//slow (and uncommon) case: pushes the true value
			this.ds = (result) -> {
				final Outcome o = ctx.decisionProcedure.decide_XCMPY(val1, val2, result);
				return o;
			};

			this.srs = (State s, DecisionAlternative_XCMPY r) -> {
				try {
					s.assume(ctx.decisionProcedure.simplify(calc.applyBinary(val1, r.toOperator(), val2)));
				} catch (InvalidOperatorException | InvalidOperandException
						| InvalidTypeException e) {
					//this should never happen after decideComparison call
					throw new UnexpectedInternalException(e);
				}
			};

			this.sus = (State s, DecisionAlternative_XCMPY r) -> {
				s.pushOperand(calc.valInt(r.value()));
				try {
					s.incPC();
				} catch (InvalidProgramCounterException e) {
		            throwVerifyError(state);
				}
			};

			//generates all the states
			this.state = state;
			this.ctx = ctx;
			try {
				generateStates();
			} catch (InvalidInputException e) {
				//bad val1 / val2
	            throwVerifyError(state);
			} catch (BadClassFileException | InvalidTypeException e) {
				//this should never happen
				throw new UnexpectedInternalException(e);
			}
		}
	}
}
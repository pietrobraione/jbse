package jbse.algo;

import static jbse.bc.Opcodes.OP_IFEQ;
import static jbse.bc.Opcodes.OP_IFGE;
import static jbse.bc.Opcodes.OP_IFGT;
import static jbse.bc.Opcodes.OP_IFLE;
import static jbse.bc.Opcodes.OP_IFLT;
import static jbse.bc.Opcodes.OP_IFNE;
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
import jbse.mem.Calculator;
import jbse.mem.Primitive;
import jbse.mem.State;
import jbse.tree.DecisionAlternativeComparison;

/**
 * Command managing all the *cmp* commands ([d/f/l]cmp_[g/l]). 
 * It decides over a comparison between primitives (longs, 
 * doubles, floats), a sheer numeric decision.
 * 
 * @author Pietro Braione
 */
class SENcmp extends MultipleStateGenerator<DecisionAlternativeComparison> implements Algorithm {
	public SENcmp() {
		super(DecisionAlternativeComparison.class);
	}
	
	public void exec(State state, final ExecutionContext ctx) 
	throws DecisionException, UnexpectedInternalException, ContradictionException, ThreadStackEmptyException, OperandStackEmptyException {
		//TODO NaN cases for distinguishing [d/l]cmpg from [d/l]cmpl

		//takes operands from current frame's operand stack
		final Primitive val2 = (Primitive) state.pop();
		final Primitive val1 = (Primitive) state.pop();

		//since typically *cmp* is followed by an if[eq/ge/gt/le/lt/ne], 
		//which exploits only TWO of the THREE cases, we check whether
		//this is the case and avoid having a uselessly 50% bigger
		//state space
		final int nextBytecode;
		try {
			nextBytecode = state.getInstruction(1);
		} catch (InvalidProgramCounterException e) {
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
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
				state.push(val1.sub(val2));
			} catch (InvalidOperandException | InvalidTypeException e) {
				state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
				return;
			}
			try {
				state.incPC();
			} catch (InvalidProgramCounterException e) {
				state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
			}
		} else {
			final Calculator calc = state.getCalculator();
			//slow (and uncommon) case: pushes the true value
			this.ds = (result) -> {
				final Outcome o = ctx.decisionProcedure.decideComparison(val1, val2, result);
				return o;
			};

			this.srs = (State s, DecisionAlternativeComparison r) -> {
				try {
					s.assume(ctx.decisionProcedure.simplify(calc.applyBinary(val1, r.toOperator(), val2)));
				} catch (InvalidOperatorException | InvalidOperandException
						| InvalidTypeException e) {
					//this should never happen after decideComparison call
					throw new UnexpectedInternalException(e);
				}
			};

			this.sus = (State s, DecisionAlternativeComparison r) -> {
				s.push(calc.valInt(r.value()));
				try {
					s.incPC();
				} catch (InvalidProgramCounterException e) {
					s.createThrowableAndThrowIt(Util.VERIFY_ERROR);
				}
			};

			//generates all the states
			this.state = state;
			this.ctx = ctx;
			try {
				generateStates();
			} catch (InvalidInputException e) {
				//bad val1 / val2
				this.state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
			} catch (ClassFileNotFoundException | InvalidTypeException e) {
				//this should never happen
				throw new UnexpectedInternalException(e);
			}
		}
	}
}
package jbse.algo;

import static jbse.algo.Util.throwVerifyError;

import jbse.bc.exc.ClassFileNotFoundException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.DecisionProcedureAlgorithms.Outcome;
import jbse.dec.exc.DecisionException;
import jbse.dec.exc.InvalidInputException;
import jbse.mem.Frame;
import jbse.mem.State;
import jbse.mem.SwitchTable;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_XSWITCH;
import jbse.val.Calculator;
import jbse.val.Expression;
import jbse.val.Primitive;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * Command for managing the *switch (tableswitch, lookupswitch). 
 * It decides over the branch to be taken, a sheer numeric decision.
 * 
 * @author Pietro Braione
 * @author unknown
 */
final class Algo_XSWITCH extends MultipleStateGenerator<DecisionAlternative_XSWITCH> implements Algorithm {
	boolean isTableSwitch;

	public Algo_XSWITCH() {
		super(DecisionAlternative_XSWITCH.class);
	}
	
	@Override
	public void exec(State state, final ExecutionContext ctx) 
	throws ThreadStackEmptyException, OperandStackEmptyException, 
	DecisionException, ContradictionException {
		final Calculator calc = state.getCalculator();
		
		//gets the switch table
		final SwitchTable tab;
		try {
			final Frame f = state.getCurrentFrame();
			tab = new SwitchTable(f, calc, isTableSwitch);
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
			return;
		}
		
		//pops the selector
		final Primitive selector = (Primitive) state.pop();
		
		this.ds = (result) -> {
			final Outcome o = ctx.decisionProcedure.decideSwitch(selector, tab, result);
			return o;
		};
		
		this.srs = (State s, DecisionAlternative_XSWITCH r) -> {
			//augments the path condition
			final Expression branchCondition;
			try {
				if (r.isDefault()) {
					branchCondition = (Expression) tab.getDefaultClause(selector);
				} else {  
					final int index = r.value();
					branchCondition = (Expression) selector.eq(calc.valInt(index));
				}
			} catch (InvalidOperandException | InvalidTypeException e) {
				//this should never happen after call to decideSwitch
				throw new UnexpectedInternalException(e);
			}
			s.assume(ctx.decisionProcedure.simplify(branchCondition));
		};
		
		this.sus = (State s, DecisionAlternative_XSWITCH r) -> {
			final int jumpOffset;
			if (r.isDefault()) {
				jumpOffset = tab.jumpOffsetDefault();
			} else {
				final int index = r.value();
				jumpOffset = tab.jumpOffset(index);
			}
			try {
				s.incPC(jumpOffset);
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
			//bad selector
            throwVerifyError(state);
		} catch (ClassFileNotFoundException | InvalidTypeException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
	}
}
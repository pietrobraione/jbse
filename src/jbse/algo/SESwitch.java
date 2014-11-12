package jbse.algo;

import jbse.Util;
import jbse.dec.DecisionProcedureAlgorithms.Outcome;
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
import jbse.mem.Calculator;
import jbse.mem.Expression;
import jbse.mem.Frame;
import jbse.mem.Primitive;
import jbse.mem.State;
import jbse.mem.SwitchTable;
import jbse.tree.DecisionAlternativeSwitch;

/**
 * Command for managing the *switch (tableswitch, lookupswitch). 
 * It decides over the branch to be taken, a sheer numeric decision.
 * 
 * @author Pietro Braione
 * @author unknown
 */
final class SESwitch extends MultipleStateGenerator<DecisionAlternativeSwitch> implements Algorithm {
	boolean isTableSwitch;

	public SESwitch() {
		super(DecisionAlternativeSwitch.class);
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
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
			return;
		}
		
		//pops the selector
		final Primitive selector = (Primitive) state.pop();
		
		this.ds = (result) -> {
			final Outcome o = ctx.decisionProcedure.decideSwitch(selector, tab, result);
			return o;
		};
		
		this.srs = (State s, DecisionAlternativeSwitch r) -> {
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
		
		this.sus = (State s, DecisionAlternativeSwitch r) -> {
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
				s.createThrowableAndThrowIt(Util.VERIFY_ERROR);
			}
		};
		
		//generates all the states		
    	this.state = state;
    	this.ctx = ctx;		
		try {
			generateStates();
		} catch (InvalidInputException e) {
			//bad selector
			this.state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
		} catch (ClassFileNotFoundException | InvalidTypeException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
	}
}
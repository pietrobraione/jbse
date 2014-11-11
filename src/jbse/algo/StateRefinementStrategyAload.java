package jbse.algo;

import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.dec.DecisionException;
import jbse.exc.mem.ContradictionException;
import jbse.exc.mem.InvalidTypeException;
import jbse.mem.State;
import jbse.tree.DecisionAlternativeAload;
import jbse.tree.DecisionAlternativeAloadOut;
import jbse.tree.DecisionAlternativeAloadRefAliases;
import jbse.tree.DecisionAlternativeAloadRefNull;
import jbse.tree.DecisionAlternativeAloadRefExpands;
import jbse.tree.DecisionAlternativeAloadResolved;
import jbse.tree.DecisionAlternativeAloadVisitor;

/**
 * Strategy for refining a state for the *aload bytecodes; reimplements {@link StateRefinementStrategy#refine} 
 * to redispatch towards abstract methods specializing refinement on the possible {@link DecisionAlternativeAload}s.
 * Class created to untangle a bit its only subclass.
 * 
 * @author Pietro Braione
 *
 */
abstract class StateRefinementStrategyAload implements StateRefinementStrategy<DecisionAlternativeAload> {
	abstract public void refineRefExpands(State s, DecisionAlternativeAloadRefExpands dac) 
	throws DecisionException, ContradictionException, InvalidTypeException, UnexpectedInternalException;

	abstract public void refineRefAliases(State s, DecisionAlternativeAloadRefAliases dai) 
	throws DecisionException, ContradictionException, UnexpectedInternalException;

	abstract public void refineRefNull(State s, DecisionAlternativeAloadRefNull dan) 
	throws DecisionException, ContradictionException, UnexpectedInternalException;

	abstract public void refineResolved(State s, DecisionAlternativeAloadResolved dav) 
	throws DecisionException, UnexpectedInternalException;

	abstract public void refineOut(State s, DecisionAlternativeAloadOut dao) 
	throws UnexpectedInternalException;

	@Override
	public final void refine(final State s, DecisionAlternativeAload r)
	throws DecisionException, ContradictionException, InvalidTypeException, UnexpectedInternalException {
		//a visitor redispatching to the methods which specialize this.refine
		final DecisionAlternativeAloadVisitor visitorRefine = 
		new DecisionAlternativeAloadVisitor() {
			@Override
			public void visitDecisionAlternativeAloadRefExpands(DecisionAlternativeAloadRefExpands dac)
			throws DecisionException, ContradictionException, InvalidTypeException, UnexpectedInternalException {
				StateRefinementStrategyAload.this.refineRefExpands(s, dac);
			}

			@Override
			public void visitDecisionAlternativeAloadRefAliases(DecisionAlternativeAloadRefAliases dai)
			throws DecisionException, ContradictionException, UnexpectedInternalException {
				StateRefinementStrategyAload.this.refineRefAliases(s, dai);
			}

			@Override
			public void visitDecisionAlternativeAloadRefNull(DecisionAlternativeAloadRefNull dan)
			throws DecisionException, ContradictionException, UnexpectedInternalException {
				StateRefinementStrategyAload.this.refineRefNull(s, dan);
			}

			@Override
			public void visitDecisionAlternativeAloadResolved(DecisionAlternativeAloadResolved dav)
			throws DecisionException, UnexpectedInternalException {
				StateRefinementStrategyAload.this.refineResolved(s, dav);
			}

			@Override
			public void visitDecisionAlternativeAloadOut(DecisionAlternativeAloadOut dao) 
			throws UnexpectedInternalException {
				StateRefinementStrategyAload.this.refineOut(s, dao);
			}
		};

		//redispatches and manages exceptions
		try {
			r.accept(visitorRefine);
		} catch (DecisionException | ContradictionException | InvalidTypeException | 
				UnexpectedInternalException | RuntimeException e) {
			throw e;
		} catch (Exception e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
	}
}

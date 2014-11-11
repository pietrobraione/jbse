package jbse.algo;

import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.dec.DecisionException;
import jbse.exc.mem.ContradictionException;
import jbse.exc.mem.InvalidTypeException;
import jbse.mem.State;
import jbse.tree.DecisionAlternativeLFLoad;
import jbse.tree.DecisionAlternativeLFLoadRefAliases;
import jbse.tree.DecisionAlternativeLFLoadRefNull;
import jbse.tree.DecisionAlternativeLFLoadRefExpands;
import jbse.tree.DecisionAlternativeLFLoadResolved;
import jbse.tree.DecisionAlternativeLFLoadVisitor;

/**
 * Strategy for refining a state for the *load* and get* bytecodes; reimplements {@link StateRefinementStrategy#refine} 
 * to redispatch towards abstract methods specializing refinement on the possible {@link DecisionAlternativeLFLoad}s.
 * Class created to untangle a bit its only subclass.
 * 
 * @author Pietro Braione
 *
 */
abstract class StateRefinementStrategyLoadLF implements StateRefinementStrategy<DecisionAlternativeLFLoad> {
	abstract public void refineRefExpands(State s, DecisionAlternativeLFLoadRefExpands drc)
	throws DecisionException, ContradictionException, InvalidTypeException, UnexpectedInternalException;

	abstract public void refineRefAliases(State s, DecisionAlternativeLFLoadRefAliases dro) 
	throws DecisionException, ContradictionException;

	abstract public void refineRefNull(State s, DecisionAlternativeLFLoadRefNull drn)
	throws DecisionException, ContradictionException;

	abstract public void refineResolved(State s, DecisionAlternativeLFLoadResolved drr)
	throws DecisionException, UnexpectedInternalException;

	@Override
	public final void refine(final State s, DecisionAlternativeLFLoad r)
	throws DecisionException, ContradictionException, InvalidTypeException, UnexpectedInternalException {
		//a visitor redispatching to the methods which specialize this.refine
		final DecisionAlternativeLFLoadVisitor visitorRefine = 
		new DecisionAlternativeLFLoadVisitor() {
			@Override
			public void visitDecisionAlternativeLFLoadRefExpands(DecisionAlternativeLFLoadRefExpands drc) 
			throws DecisionException, ContradictionException, InvalidTypeException, UnexpectedInternalException {
				StateRefinementStrategyLoadLF.this.refineRefExpands(s, drc);
			}

			@Override
			public void visitDecisionAlternativeLFLoadRefAliases(DecisionAlternativeLFLoadRefAliases dro) 
			throws DecisionException, ContradictionException {
				StateRefinementStrategyLoadLF.this.refineRefAliases(s, dro);
			}

			@Override
			public void visitDecisionAlternativeLFLoadRefNull(DecisionAlternativeLFLoadRefNull drn) 
			throws DecisionException, ContradictionException {
				StateRefinementStrategyLoadLF.this.refineRefNull(s, drn);
			}

			@Override
			public void visitDecisionAlternativeLFLoadResolved(DecisionAlternativeLFLoadResolved drr)
			throws DecisionException, UnexpectedInternalException {
				StateRefinementStrategyLoadLF.this.refineResolved(s, drr);
			}
		};

		//redispatches and manages exceptions
		try {
			r.accept(visitorRefine);
		} catch (DecisionException | ContradictionException | 
				InvalidTypeException | UnexpectedInternalException | RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new UnexpectedInternalException(e);
		}
	}
}

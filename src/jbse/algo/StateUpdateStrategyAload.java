package jbse.algo;

import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.dec.DecisionException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.mem.State;
import jbse.tree.DecisionAlternativeAload;
import jbse.tree.DecisionAlternativeAloadOut;
import jbse.tree.DecisionAlternativeAloadRef;
import jbse.tree.DecisionAlternativeAloadRefAliases;
import jbse.tree.DecisionAlternativeAloadRefNull;
import jbse.tree.DecisionAlternativeAloadRefExpands;
import jbse.tree.DecisionAlternativeAloadResolved;
import jbse.tree.DecisionAlternativeAloadVisitor;

/**
 * {@link StateUpdateStrategy} for the aload bytecode; reimplements {@link StateUpdateStrategy.update} 
 * to redispatch towards abstract methods specializing refinement on the possible {@link DecisionAlternativeAload}s.
 * Class created to untangle a bit its only subclass.
 * 
 * @author Pietro Braione
 *
 */
abstract class StateUpdateStrategyAload implements StateUpdateStrategy<DecisionAlternativeAload> {
	abstract public void updateReference(State s, DecisionAlternativeAloadRef dar) 
	throws DecisionException, ThreadStackEmptyException, UnexpectedInternalException;

	abstract public void updateResolved(State s, DecisionAlternativeAloadResolved dav) 
	throws DecisionException, ThreadStackEmptyException, UnexpectedInternalException;

	abstract public void updateOut(State s, DecisionAlternativeAloadOut dao) 
	throws ThreadStackEmptyException, UnexpectedInternalException;

	@Override
	public final void update(final State s, DecisionAlternativeAload r)
	throws DecisionException, ThreadStackEmptyException, UnexpectedInternalException {
		//a visitor redispatching to the methods which specialize this.update
		DecisionAlternativeAloadVisitor visitorUpdate = 
		new DecisionAlternativeAloadVisitor() {
			@Override
			public void visitDecisionAlternativeAloadRefExpands(DecisionAlternativeAloadRefExpands dac) 
			throws DecisionException, ThreadStackEmptyException, UnexpectedInternalException {
				StateUpdateStrategyAload.this.updateReference(s, dac);
			}

			@Override
			public void visitDecisionAlternativeAloadRefAliases(DecisionAlternativeAloadRefAliases dai) 
			throws DecisionException, ThreadStackEmptyException, UnexpectedInternalException {
				StateUpdateStrategyAload.this.updateReference(s, dai);
			}

			@Override
			public void visitDecisionAlternativeAloadRefNull(DecisionAlternativeAloadRefNull dan) 
			throws DecisionException, ThreadStackEmptyException, UnexpectedInternalException {
				StateUpdateStrategyAload.this.updateReference(s, dan);
			}

			@Override
			public void visitDecisionAlternativeAloadResolved(DecisionAlternativeAloadResolved dav) 
			throws DecisionException, ThreadStackEmptyException, UnexpectedInternalException {
				StateUpdateStrategyAload.this.updateResolved(s, dav);
			}

			@Override
			public void visitDecisionAlternativeAloadOut(DecisionAlternativeAloadOut dao) 
			throws ThreadStackEmptyException, UnexpectedInternalException {
				StateUpdateStrategyAload.this.updateOut(s, dao);
			}
		};

		try {
			r.accept(visitorUpdate);
		} catch (DecisionException | ThreadStackEmptyException | 
				UnexpectedInternalException | RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new UnexpectedInternalException(e);
		}
	}
}

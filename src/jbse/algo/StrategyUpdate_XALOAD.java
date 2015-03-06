package jbse.algo;

import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.exc.DecisionException;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_XALOAD;
import jbse.tree.DecisionAlternative_XALOAD_Out;
import jbse.tree.DecisionAlternative_XALOAD_Unresolved;
import jbse.tree.DecisionAlternative_XALOAD_Aliases;
import jbse.tree.DecisionAlternative_XALOAD_Null;
import jbse.tree.DecisionAlternative_XALOAD_Expands;
import jbse.tree.DecisionAlternative_XALOAD_Resolved;
import jbse.tree.VisitorDecisionAlternative_XALOAD;

/**
 * {@link StrategyUpdate} for the *aload (load from array) bytecodes 
 * ([a/b/c/d/f/i/l/s]aload). It reimplements {@link StrategyUpdate#update} 
 * to redispatch towards abstract methods specializing refinement on 
 * the possible {@link DecisionAlternative_XALOAD}s.
 * This class exists only to untangle a bit its only subclass.
 * 
 * @author Pietro Braione
 *
 */
abstract class StrategyUpdate_XALOAD implements StrategyUpdate<DecisionAlternative_XALOAD> {
	abstract public void updateReference(State s, DecisionAlternative_XALOAD_Unresolved dar) 
	throws DecisionException, ThreadStackEmptyException;

	abstract public void updateResolved(State s, DecisionAlternative_XALOAD_Resolved dav) 
	throws DecisionException, ThreadStackEmptyException;

	abstract public void updateOut(State s, DecisionAlternative_XALOAD_Out dao) 
	throws ThreadStackEmptyException;

	@Override
	public final void update(final State s, DecisionAlternative_XALOAD r)
	throws DecisionException, ThreadStackEmptyException {
		//a visitor redispatching to the methods which specialize this.update
		VisitorDecisionAlternative_XALOAD visitorUpdate = 
		new VisitorDecisionAlternative_XALOAD() {
			@Override
			public void visitDecisionAlternative_XALOAD_Expands(DecisionAlternative_XALOAD_Expands dac) 
			throws DecisionException, ThreadStackEmptyException {
				StrategyUpdate_XALOAD.this.updateReference(s, dac);
			}

			@Override
			public void visitDecisionAlternative_XALOAD_Aliases(DecisionAlternative_XALOAD_Aliases dai) 
			throws DecisionException, ThreadStackEmptyException {
				StrategyUpdate_XALOAD.this.updateReference(s, dai);
			}

			@Override
			public void visitDecisionAlternative_XALOAD_Null(DecisionAlternative_XALOAD_Null dan) 
			throws DecisionException, ThreadStackEmptyException {
				StrategyUpdate_XALOAD.this.updateReference(s, dan);
			}

			@Override
			public void visitDecisionAlternative_XALOAD_Resolved(DecisionAlternative_XALOAD_Resolved dav) 
			throws DecisionException, ThreadStackEmptyException {
				StrategyUpdate_XALOAD.this.updateResolved(s, dav);
			}

			@Override
			public void visitDecisionAlternative_XALOAD_Out(DecisionAlternative_XALOAD_Out dao) 
			throws ThreadStackEmptyException {
				StrategyUpdate_XALOAD.this.updateOut(s, dao);
			}
		};

		try {
			r.accept(visitorUpdate);
		} catch (DecisionException | ThreadStackEmptyException | 
				RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new UnexpectedInternalException(e);
		}
	}
}

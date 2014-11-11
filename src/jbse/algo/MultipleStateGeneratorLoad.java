package jbse.algo;

import jbse.Type;
import jbse.Util;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.dec.DecisionException;
import jbse.exc.mem.ContradictionException;
import jbse.exc.mem.InvalidOperandException;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.InvalidTypeException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.mem.Array;
import jbse.mem.Objekt;
import jbse.mem.Primitive;
import jbse.mem.ReferenceConcrete;
import jbse.mem.ReferenceSymbolic;
import jbse.mem.State;
import jbse.mem.Value;
import jbse.tree.DecisionAlternative;
import jbse.tree.DecisionAlternativeLoadRefAliases;
import jbse.tree.DecisionAlternativeLoadRefNull;
import jbse.tree.DecisionAlternativeLoadRefExpands;
import jbse.tree.DecisionAlternativeLoad;

/**
 * A {@link MultipleStateGenerator} for bytecodes which load a {@link Value} to the operand stack.
 * It defines method which manage refinement in the case the {@link Value} is a {@link ReferenceSymbolic}
 * ("lazy initialization"), and/or it comes from an {@link Array}.
 * 
 * @author Pietro Braione
 *
 * @param <R> the {@link DecisionAlternative}s used to encode the results 
 *            of splitting decisions. NOT assumed to be a {@link DecisionAlternativeLoad}
 *            so it can be used as a superclass also for array bytecodes {@link Algorithm}s, 
 *            which do not load any {@link Value} to the operand stack in the 
 *            out-of-range case. 
 */
abstract class MultipleStateGeneratorLoad<R extends DecisionAlternative> extends MultipleStateGenerator<R> {
	/** Must be set by subclasses to provide the pc offset for the bytecode. */
	protected int pcOffset;
	
	protected MultipleStateGeneratorLoad(Class<R> superclassDecisionAlternatives) {
		super(superclassDecisionAlternatives);
	}
	
	protected final void refineRefExpands(State s, DecisionAlternativeLoadRefExpands drc) 
	throws ContradictionException, InvalidTypeException, UnexpectedInternalException {
		final ReferenceSymbolic referenceToExpand = drc.getValueToLoad();
		final String classNameOfTargetObject = drc.getClassNameOfTargetObject();
		s.assumeExpands(referenceToExpand, classNameOfTargetObject);
		//in the case the fresh object is an array, we assume it 
		//to have nonnegative length
		if (Type.isArray(classNameOfTargetObject)) {
			final Primitive lengthPositive;
			try {
				final Array targetObject = (Array) s.getObject(referenceToExpand);
				lengthPositive = targetObject.getLength().ge(this.state.getCalculator().valInt(0));
			} catch (InvalidOperandException | InvalidTypeException e) {
				//this should never happen
				throw new UnexpectedInternalException(e);
			}
			s.assume(this.ctx.decisionProcedure.simplify(lengthPositive));
		}
	}

	protected final void refineRefAliases(State s, DecisionAlternativeLoadRefAliases dro)
	throws ContradictionException {
		final ReferenceSymbolic referenceToResolve = dro.getValueToLoad();
		final long aliasPosition = dro.getAliasPosition();
		final Objekt object = s.getObjectInitial(new ReferenceConcrete(aliasPosition));
		s.assumeAliases(referenceToResolve, aliasPosition, object);
	}
	
	protected final void refineRefNull(State s, DecisionAlternativeLoadRefNull drn)
	throws ContradictionException {
		final ReferenceSymbolic referenceToResolve = drn.getValueToLoad();
		s.assumeNull(referenceToResolve);
	}
	
	protected final void update(State s, DecisionAlternativeLoad r) 
	throws DecisionException, ThreadStackEmptyException, UnexpectedInternalException {
		final Value val = r.getValueToLoad();
		final Value valToPush = possiblyMaterialize(s, val);
		s.push(valToPush);
		
		//manages triggers and increments the program counter
		final boolean goOn;
		try {
			goOn = this.ctx.triggerManager.runTriggers(s, r, this.pcOffset);
		} catch (InvalidProgramCounterException e) {
			s.createThrowableAndThrowIt(Util.VERIFY_ERROR);
			return;
		}
		if (goOn) {
			//updates the program counter
			try {
				s.incPC(this.pcOffset);
			} catch (InvalidProgramCounterException e) {
				s.createThrowableAndThrowIt(Util.VERIFY_ERROR);
			}
		}
	}
	
	protected abstract Value possiblyMaterialize(State s, Value val) 
	throws DecisionException, UnexpectedInternalException;
}

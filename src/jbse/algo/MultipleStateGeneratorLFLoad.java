package jbse.algo;

import static jbse.algo.Util.throwVerifyError;

import jbse.bc.exc.ClassFileNotFoundException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.DecisionProcedureAlgorithms.Outcome;
import jbse.dec.exc.DecisionException;
import jbse.dec.exc.InvalidInputException;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternativeLFLoad;
import jbse.tree.DecisionAlternativeLFLoadRefAliases;
import jbse.tree.DecisionAlternativeLFLoadRefNull;
import jbse.tree.DecisionAlternativeLFLoadRefExpands;
import jbse.tree.DecisionAlternativeLFLoadResolved;
import jbse.val.ReferenceSymbolic;
import jbse.val.Value;
import jbse.val.exc.InvalidTypeException;

/**
 * Class for completing the semantics of the *load* and get* bytecodes 
 * (aload[_0/1/2/3], dload[_0/1/2/3], fload[_0/1/2/3], 
 * iload[_0/1/2/3], lload[_0/1/2/3], getfield, getstatic). It decides over the value loaded 
 * to the operand stack in the case (aload[_0/1/2/3], getfield, getstatic) 
 * this is an uninitialized symbolic reference ("lazy initialization").
 * 
 * @author Pietro Braione
 */
abstract class MultipleStateGeneratorLFLoad extends MultipleStateGeneratorLoad<DecisionAlternativeLFLoad> {
	public MultipleStateGeneratorLFLoad() {
		super(DecisionAlternativeLFLoad.class);
	}

	//must be set by subclasses
	protected Value valToLoad;
	
	//set by the decision strategies
	private boolean refNotExpanded;
	private String nonExpandedRefType;
	private String nonExpandedRefOrigin;
	
	@Override
	protected void generateStates() 
	throws DecisionException, ContradictionException, ThreadStackEmptyException {
		this.ds = (results) -> {
			final Outcome o = ctx.decisionProcedure.resolveLFLoad(state, valToLoad, results);
			MultipleStateGeneratorLFLoad.this.refNotExpanded = o.noReferenceExpansion();
			if (MultipleStateGeneratorLFLoad.this.refNotExpanded) {
				final ReferenceSymbolic refToLoad = (ReferenceSymbolic) valToLoad;
				nonExpandedRefType = refToLoad.getStaticType();
				nonExpandedRefOrigin = refToLoad.getOrigin();
			}
			return o;
		};
		
		this.srs = new StateRefinementStrategyLoadLF() {
			@Override
			public void refineRefExpands(State s, DecisionAlternativeLFLoadRefExpands drc) 
			throws DecisionException, ContradictionException, InvalidTypeException {
				MultipleStateGeneratorLFLoad.this.refineRefExpands(s, drc); //implemented in MultipleStateGeneratorLoad
			}

			@Override
			public void refineRefAliases(State s, DecisionAlternativeLFLoadRefAliases dro)
			throws DecisionException, ContradictionException {
				MultipleStateGeneratorLFLoad.this.refineRefAliases(s, dro); //implemented in MultipleStateGeneratorLoad
			}

			@Override
			public void refineRefNull(State s, DecisionAlternativeLFLoadRefNull drn)
			throws DecisionException, ContradictionException {
				MultipleStateGeneratorLFLoad.this.refineRefNull(s, drn); //implemented in MultipleStateGeneratorLoad
			}

			@Override
			public void refineResolved(State s, DecisionAlternativeLFLoadResolved drr) {
				//nothing to do, the value is concrete or has been already refined
			}
		};
		
		this.sus = (State s, DecisionAlternativeLFLoad r) -> {
			MultipleStateGeneratorLFLoad.this.update(s, r);
		};
		
		try {
			super.generateStates();
		} catch (ClassFileNotFoundException | InvalidInputException e) {
			//bad valToLoad (triggered by call to resolveLFLoad in this.ds)
            throwVerifyError(state);
		} catch (InvalidTypeException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
	}
	
	@Override
	protected final Value possiblyMaterialize(State s, Value val) {
		//nothing to do
		return val;
	}		

	
	//these override the default implementation in Algorithms
    public boolean someReferenceNotExpanded() { 
    	return this.refNotExpanded; 
    }

    public String nonExpandedReferencesTypes() { 
    	return this.nonExpandedRefType; 
    }
    
    public String nonExpandedReferencesOrigins() { 
    	return this.nonExpandedRefOrigin; 
    }
}

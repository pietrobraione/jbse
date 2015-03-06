package jbse.algo;

import static jbse.algo.Util.throwVerifyError;

import jbse.bc.exc.BadClassFileException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.DecisionProcedureAlgorithms.Outcome;
import jbse.dec.exc.DecisionException;
import jbse.dec.exc.InvalidInputException;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_XLOAD_GETX;
import jbse.tree.DecisionAlternative_XLOAD_GETX_Aliases;
import jbse.tree.DecisionAlternative_XLOAD_GETX_Null;
import jbse.tree.DecisionAlternative_XLOAD_GETX_Expands;
import jbse.tree.DecisionAlternative_XLOAD_GETX_Resolved;
import jbse.val.ReferenceSymbolic;
import jbse.val.Value;
import jbse.val.exc.InvalidTypeException;

/**
 * Class for completing the semantics of the *load* and get* bytecodes 
 * ([a/d/f/i/l]load[_0/1/2/3], get[field/static]). It decides over the value loaded 
 * to the operand stack in the case (aload[_0/1/2/3], get[field/static]) 
 * this is an uninitialized symbolic reference ("lazy initialization").
 * 
 * @author Pietro Braione
 */
abstract class MultipleStateGenerator_XLOAD_GETX extends MultipleStateGenerator_XYLOAD_GETX<DecisionAlternative_XLOAD_GETX> {
	public MultipleStateGenerator_XLOAD_GETX() {
		super(DecisionAlternative_XLOAD_GETX.class);
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
			final Outcome o = ctx.decisionProcedure.resolve_XLOAD_GETX(state, valToLoad, results);
			MultipleStateGenerator_XLOAD_GETX.this.refNotExpanded = o.noReferenceExpansion();
			if (MultipleStateGenerator_XLOAD_GETX.this.refNotExpanded) {
				final ReferenceSymbolic refToLoad = (ReferenceSymbolic) valToLoad;
				nonExpandedRefType = refToLoad.getStaticType();
				nonExpandedRefOrigin = refToLoad.getOrigin();
			}
			return o;
		};
		
		this.rs = new StrategyRefine_XLOAD_GETX() {
			@Override
			public void refineRefExpands(State s, DecisionAlternative_XLOAD_GETX_Expands drc) 
			throws ContradictionException, InvalidTypeException {
				MultipleStateGenerator_XLOAD_GETX.this.refineRefExpands(s, drc); //implemented in MultipleStateGeneratorLoad
			}

			@Override
			public void refineRefAliases(State s, DecisionAlternative_XLOAD_GETX_Aliases dro)
			throws ContradictionException {
				MultipleStateGenerator_XLOAD_GETX.this.refineRefAliases(s, dro); //implemented in MultipleStateGeneratorLoad
			}

			@Override
			public void refineRefNull(State s, DecisionAlternative_XLOAD_GETX_Null drn)
			throws ContradictionException {
				MultipleStateGenerator_XLOAD_GETX.this.refineRefNull(s, drn); //implemented in MultipleStateGeneratorLoad
			}

			@Override
			public void refineResolved(State s, DecisionAlternative_XLOAD_GETX_Resolved drr) {
				//nothing to do, the value is concrete or has been already refined
			}
		};
		
		this.us = (State s, DecisionAlternative_XLOAD_GETX r) -> {
			MultipleStateGenerator_XLOAD_GETX.this.update(s, r);
		};
		
		try {
			super.generateStates();
		} catch (BadClassFileException | InvalidInputException e) {
			//bad valToLoad (triggered by call to resolve_XLOAD_GETX in this.ds)
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

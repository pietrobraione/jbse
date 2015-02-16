package jbse.algo;

import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.XALOADSTORE_OFFSET;
import static jbse.bc.Signatures.ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;

import java.util.Collection;
import java.util.Iterator;

import jbse.algo.exc.CannotManageStateException;
import jbse.bc.exc.BadClassFileException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.DecisionProcedureAlgorithms.Outcome;
import jbse.dec.exc.DecisionException;
import jbse.dec.exc.InvalidInputException;
import jbse.mem.Array;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_XALOAD;
import jbse.tree.DecisionAlternative_XALOAD_Out;
import jbse.tree.DecisionAlternative_XALOAD_Ref;
import jbse.tree.DecisionAlternative_XALOAD_RefAliases;
import jbse.tree.DecisionAlternative_XALOAD_RefNull;
import jbse.tree.DecisionAlternative_XALOAD_RefExpands;
import jbse.tree.DecisionAlternative_XALOAD_Resolved;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.ReferenceArrayImmaterial;
import jbse.val.ReferenceConcrete;
import jbse.val.ReferenceSymbolic;
import jbse.val.Value;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * Algorithm managing all the *aload (load from array) bytecodes 
 * ([a/b/c/d/f/i/l/s]aload). 
 * It decides over access index membership (inbound vs. outbound), 
 * which is a sheer numeric decision, and in the case of 
 * the aaload bytecode, also over the value loaded from the array 
 * when this is a symbolic reference ("lazy initialization").
 *  
 * @author Pietro Braione
 * @author unknown
 */
final class Algo_XALOAD extends MultipleStateGenerator_XYLOAD_GETX<DecisionAlternative_XALOAD> implements Algorithm {
	private boolean someRefNotExpanded;
	private String nonExpandedRefTypes;
	private String nonExpandedRefOrigins;
	private Primitive index;
	private Reference myObjectRef;
	
	public Algo_XALOAD() {
		super(DecisionAlternative_XALOAD.class);
	}

	@Override
	public void exec(final State state, final ExecutionContext ctx) 
	throws DecisionException, CannotManageStateException, 
	ContradictionException, ThreadStackEmptyException {
	    try {
	        this.index = (Primitive) state.pop();
	        this.myObjectRef = (Reference) state.pop();
	    } catch (OperandStackEmptyException e) {
	        throwVerifyError(state);
	        return;
	    }
	    
		if (state.isNull(this.myObjectRef)) {
			//null object 
		    throwNew(state, NULL_POINTER_EXCEPTION);
			return;
		}

		//takes the value from the array
		final Array arrayObj = (Array) state.getObject(myObjectRef);
		final Collection<Array.AccessOutcome> entries;
		try {
			entries = arrayObj.get(this.index);
		} catch (InvalidOperandException | InvalidTypeException e) {
			//bad index
		    throwVerifyError(state);
			return;
		}

		//generates the next states
		this.state = state;
		this.ctx = ctx;
		this.pcOffset = XALOADSTORE_OFFSET;

		this.ds = (result) -> {
			boolean shouldRefine = false;
			boolean branchingDecision = false;
			boolean first = true; //just for formatting
			for (Array.AccessOutcome e : entries) {
				//puts in val the value of the current entry, or a fresh symbol, 
				//or null if the index is out of bound
				Value val;
				boolean fresh = false; 	//true iff val is a fresh symbol
				if (e instanceof Array.AccessOutcomeIn) {
					val = ((Array.AccessOutcomeIn) e).getValue();
					if (val == null) {
						val = state.createSymbol(arrayObj.getType().substring(1), arrayObj.getOrigin() + "[" + this.index + "]");
						fresh = true;
					}
				} else { //e instanceof AccessOutcomeOut
					val = null;
				}

				final Outcome o = ctx.decisionProcedure.resolve_XALOAD(state, e.getAccessCondition(), val, fresh, result);

				//if at least one reference has not been expanded, 
				//sets someRefNotExpanded to true and stores data
				//about the reference
				someRefNotExpanded = someRefNotExpanded || o.noReferenceExpansion();
				if (o.noReferenceExpansion()) {
					final ReferenceSymbolic refToLoad = (ReferenceSymbolic) val;
					nonExpandedRefTypes += (first ? "" : ", ") + refToLoad.getStaticType();
					nonExpandedRefOrigins += (first ? "" : ", ") + refToLoad.getOrigin();
					first = false;
				}
				
				//if at least one reference should be refined, then it should be refined
				shouldRefine = shouldRefine || o.shouldRefine();
				
				//if at least one decision is branching, then it is branching
				branchingDecision = branchingDecision || o.branchingDecision();
			}

			//also the size of the result matters to whether refine or not 
			shouldRefine = shouldRefine || (result.size() > 1);
			
			//for branchingDecision nothing to do: it will be false only if
			//the access is concrete and the value obtained is resolved 
			//(if a symbolic reference): in this case, result.size() must
			//be 1. Note that branchingDecision must be invariant
			//on the used decision procedure, so we cannot make it dependent
			//on result.size().
			return Outcome.val(shouldRefine, someRefNotExpanded, branchingDecision);
		};
		
		this.srs = new StrategyRefine_XALOAD() {
			@Override
			public void refineRefExpands(State s, DecisionAlternative_XALOAD_RefExpands dac) 
			throws DecisionException, ContradictionException, InvalidTypeException {
				//handles all the assumptions for reference resolution by expansion
				Algo_XALOAD.this.refineRefExpands(s, dac); //implemented in MultipleStateGeneratorLoad
				
				//assumes the array access expression (index in range)
				final Primitive accessExpression = dac.getArrayAccessExpression();
				s.assume(Algo_XALOAD.this.ctx.decisionProcedure.simplify(accessExpression));
				
				//updates the array with the resolved reference
				//TODO is it still necessary? Is it necessary to do it always?
				final ReferenceSymbolic referenceToExpand = dac.getValueToLoad();
				writeBackToSource(s, referenceToExpand);					
			}

			@Override
			public void refineRefAliases(State s, DecisionAlternative_XALOAD_RefAliases dai)
			throws DecisionException, ContradictionException {
				//handles all the assumptions for reference resolution by aliasing
				Algo_XALOAD.this.refineRefAliases(s, dai); //implemented in MultipleStateGeneratorLoad
				
				//assumes the array access expression (index in range)
				final Primitive accessExpression = dai.getArrayAccessExpression();
				s.assume(ctx.decisionProcedure.simplify(accessExpression));				

				//updates the array with the resolved reference
				//TODO is it still necessary? Is it necessary to do it always?
				final ReferenceSymbolic referenceToResolve = dai.getValueToLoad();
				writeBackToSource(s, referenceToResolve);
			}

			@Override
			public void refineRefNull(State s, DecisionAlternative_XALOAD_RefNull dan) 
			throws DecisionException, ContradictionException {
				Algo_XALOAD.this.refineRefNull(s, dan); //implemented in MultipleStateGeneratorLoad
				
				//further augments the path condition 
				final Primitive accessExpression = dan.getArrayAccessExpression();
				s.assume(ctx.decisionProcedure.simplify(accessExpression));
				
				//updates the array with the resolved reference
				//TODO is it still necessary? Is it necessary to do it always?
				final ReferenceSymbolic referenceToResolve = dan.getValueToLoad();
				writeBackToSource(s, referenceToResolve);
			}

			@Override
			public void refineResolved(State s, DecisionAlternative_XALOAD_Resolved dav)
			throws DecisionException {
				//augments the path condition
				s.assume(ctx.decisionProcedure.simplify(dav.getArrayAccessExpression()));
				
				//if the value is fresh, it writes it back in the array
				if (dav.isValueFresh()) {
					writeBackToSource(s, dav.getValueToLoad());
				}
			}
			
			@Override
			public void refineOut(State s, DecisionAlternative_XALOAD_Out dao) {
				//augments the path condition
				s.assume(ctx.decisionProcedure.simplify(dao.getArrayAccessExpression()));
			}
		};
		
		this.sus = new StrategyUpdate_XALOAD() {
			@Override
			public void updateResolved(State s, DecisionAlternative_XALOAD_Resolved dav) 
			throws DecisionException, ThreadStackEmptyException {
				Algo_XALOAD.this.update(s, dav); //implemented in MultipleStateGeneratorLoad
			}

			@Override
			public void updateReference(State s, DecisionAlternative_XALOAD_Ref dar) 
			throws DecisionException, ThreadStackEmptyException {
				Algo_XALOAD.this.update(s, dar); //implemented in MultipleStateGeneratorLoad
			}

			@Override
			public void updateOut(State s, DecisionAlternative_XALOAD_Out dao) 
			throws ThreadStackEmptyException {
				throwNew(s, ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION);
			}
		};
		
		try {
			generateStates();
		} catch (BadClassFileException e) {
			//the array element type is a reference to a nonexistent or ill-formed class
		    throwVerifyError(state); //TODO should we rather throw NO_CLASS_DEFINITION_FOUND_ERROR?
		} catch (InvalidInputException | InvalidTypeException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
	}
	
	@Override	
	protected Value possiblyMaterialize(State s, Value val) 
	throws DecisionException {
		//calculates the actual value to push by materializing 
		//a member array, if it is the case, and then pushes it
		//on the operand stack
		if (val instanceof ReferenceArrayImmaterial) { //TODO eliminate manual dispatch and WriteBackStrategy
			try {
				final ReferenceArrayImmaterial valRef = (ReferenceArrayImmaterial) val;
				final ReferenceConcrete valMaterialized = 
						s.createArray(valRef.next(), valRef.getLength(), valRef.getArrayType().substring(1));
				writeBackToSource(s, valMaterialized);
				s.push(valMaterialized);
				return valMaterialized;
			} catch (InvalidTypeException | ThreadStackEmptyException e) {
				//this should never happen
				throw new UnexpectedInternalException(e);
			}
		} else {
			return val;
		}
	}
	
	private void writeBackToSource(State s, Value val) 
	throws DecisionException {
		final Array a = (Array) s.getObject(this.myObjectRef);
		try {
			final Iterator<Array.AccessOutcomeIn> entries = a.set(this.index, val);
			this.ctx.decisionProcedure.completeArraySet(entries, this.index);
		} catch (InvalidInputException | InvalidOperandException | 
				InvalidTypeException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
	}

	@Override
	public boolean someReferenceNotExpanded() { return this.someRefNotExpanded; }

	@Override
	public String nonExpandedReferencesTypes() { return this.nonExpandedRefTypes; }

	@Override
	public String nonExpandedReferencesOrigins() { return this.nonExpandedRefOrigins; }
}

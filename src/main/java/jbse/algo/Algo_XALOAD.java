package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.storeInArray;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.XALOADSTORE_OFFSET;
import static jbse.bc.Signatures.ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;
import static jbse.common.Type.getArrayMemberType;

import java.util.Collection;
import java.util.function.Supplier;

import jbse.dec.DecisionProcedureAlgorithms.Outcome;
import jbse.dec.exc.DecisionException;
import jbse.mem.Array;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.tree.DecisionAlternative_XALOAD;
import jbse.tree.DecisionAlternative_XALOAD_Out;
import jbse.tree.DecisionAlternative_XALOAD_Unresolved;
import jbse.tree.DecisionAlternative_XALOAD_Aliases;
import jbse.tree.DecisionAlternative_XALOAD_Null;
import jbse.tree.DecisionAlternative_XALOAD_Expands;
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
 * {@link Algorithm} managing all the *aload (load from array) bytecodes 
 * ([a/b/c/d/f/i/l/s]aload). 
 * It decides over access index membership (inbound vs. outbound) 
 * which is a sheer numeric decision, and in the case of 
 * the aaload bytecode, also over the value loaded from the array 
 * when this is a symbolic reference ("lazy initialization").
 * Note that the inbound cases can be many, in principle one for 
 * each entry in the symbolic array.
 *  
 * @author Pietro Braione
 */
final class Algo_XALOAD extends Algo_XYLOAD_GETX<
BytecodeData_0, 
DecisionAlternative_XALOAD,
StrategyDecide<DecisionAlternative_XALOAD>, 
StrategyRefine_XALOAD,
StrategyUpdate_XALOAD> {

    private Reference myObjectRef; //set by cooker
    private Primitive index; //set by cooker
    private Array arrayObj; //set by cooker
    private Collection<Array.AccessOutcome> entries; //set by cooker

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }

    @Override
    protected Supplier<BytecodeData_0> bytecodeData() {
        return BytecodeData_0::get;
    }

    @Override
    protected BytecodeCooker bytecodeCooker() {
        return (state) -> { 
            try {
                this.myObjectRef = (Reference) data.operand(0);
                this.index = (Primitive) data.operand(1);
            } catch (ClassCastException e) {
                throwVerifyError(state);
                exitFromAlgorithm();
            }

            //null check
            if (state.isNull(this.myObjectRef)) {
                throwNew(state, NULL_POINTER_EXCEPTION);
                exitFromAlgorithm();
            }

            //reads the array and its entries
            try {
                this.arrayObj = (Array) state.getObject(this.myObjectRef);
                this.entries = this.arrayObj.get(this.index);
            } catch (InvalidOperandException | InvalidTypeException | 
                     ClassCastException e) {
                throwVerifyError(state);
                exitFromAlgorithm();
            }

        };
    }

    @Override
    protected Class<DecisionAlternative_XALOAD> classDecisionAlternative() {
        return DecisionAlternative_XALOAD.class;
    }

    @Override
    protected StrategyDecide<DecisionAlternative_XALOAD> decider() {
        return (state, result) -> { 
            boolean shouldRefine = false;
            boolean branchingDecision = false;
            boolean first = true; //just for formatting
            for (Array.AccessOutcome e : this.entries) {
                //puts in val the value of the current entry, or a fresh symbol, 
                //or null if the index is out of bound
                Value val;
                boolean fresh = false;  //true iff val is a fresh symbol
                if (e instanceof Array.AccessOutcomeIn) {
                    val = ((Array.AccessOutcomeIn) e).getValue();
                    if (val == null) {
                        val = state.createSymbol(getArrayMemberType(this.arrayObj.getType()), 
                                                 this.arrayObj.getOrigin().thenArrayMember(this.index));
                        fresh = true;
                    }
                } else { //e instanceof AccessOutcomeOut
                    val = null;
                }

                final Outcome o = this.ctx.decisionProcedure.resolve_XALOAD(state, e.getAccessCondition(), val, fresh, result);

                //if at least one reference has not been expanded, 
                //sets someRefNotExpanded to true and stores data
                //about the reference
                this.someRefNotExpanded = this.someRefNotExpanded || o.noReferenceExpansion();
                if (o.noReferenceExpansion()) {
                    final ReferenceSymbolic refToLoad = (ReferenceSymbolic) val;
                    this.nonExpandedRefTypes += (first ? "" : ", ") + refToLoad.getStaticType();
                    this.nonExpandedRefOrigins += (first ? "" : ", ") + refToLoad.getOrigin();
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
            return Outcome.val(shouldRefine, this.someRefNotExpanded, branchingDecision);
        };
    }

    private void writeBackToSource(State state, Value valueToStore) 
    throws DecisionException {
        storeInArray(state, this.ctx, this.myObjectRef, this.index, valueToStore);
    }

    @Override   
    protected Value possiblyMaterialize(State state, Value val) 
    throws DecisionException {
        //calculates the actual value to push by materializing 
        //a member array, if it is the case, and then pushes it
        //on the operand stack
        if (val instanceof ReferenceArrayImmaterial) { //TODO eliminate manual dispatch
            try {
                final ReferenceArrayImmaterial valRef = (ReferenceArrayImmaterial) val;
                final ReferenceConcrete valMaterialized = 
                    state.createArray(valRef.getMember(), valRef.getLength(), valRef.getArrayType());
                writeBackToSource(state, valMaterialized);
                return valMaterialized;
            } catch (InvalidTypeException e) {
                //this should never happen
                failExecution(e);
                return null; //to keep the compiler happy
            }
        } else {
            return val;
        }
    }

    @Override
    protected StrategyRefine_XALOAD refiner() {
        return new StrategyRefine_XALOAD() {
            @Override
            public void refineRefExpands(State state, DecisionAlternative_XALOAD_Expands altExpands) 
            throws DecisionException, ContradictionException, InvalidTypeException {
                //handles all the assumptions for reference resolution by expansion
                Algo_XALOAD.this.refineRefExpands(state, altExpands); //implemented in MultipleStateGenerator_XYLOAD_GETX

                //assumes the array access expression (index in range)
                final Primitive accessExpression = altExpands.getArrayAccessExpression();
                state.assume(Algo_XALOAD.this.ctx.decisionProcedure.simplify(accessExpression));

                //updates the array with the resolved reference
                final ReferenceSymbolic referenceToExpand = altExpands.getValueToLoad();
                writeBackToSource(state, referenceToExpand);                    
            }

            @Override
            public void refineRefAliases(State state, DecisionAlternative_XALOAD_Aliases altAliases)
            throws DecisionException, ContradictionException {
                //handles all the assumptions for reference resolution by aliasing
                Algo_XALOAD.this.refineRefAliases(state, altAliases); //implemented in MultipleStateGenerator_XYLOAD_GETX

                //assumes the array access expression (index in range)
                final Primitive accessExpression = altAliases.getArrayAccessExpression();
                state.assume(Algo_XALOAD.this.ctx.decisionProcedure.simplify(accessExpression));             

                //updates the array with the resolved reference
                final ReferenceSymbolic referenceToResolve = altAliases.getValueToLoad();
                writeBackToSource(state, referenceToResolve);
            }

            @Override
            public void refineRefNull(State state, DecisionAlternative_XALOAD_Null altNull) 
            throws DecisionException, ContradictionException {
                Algo_XALOAD.this.refineRefNull(state, altNull); //implemented in MultipleStateGenerator_XYLOAD_GETX

                //further augments the path condition 
                final Primitive accessExpression = altNull.getArrayAccessExpression();
                state.assume(Algo_XALOAD.this.ctx.decisionProcedure.simplify(accessExpression));

                //updates the array with the resolved reference
                final ReferenceSymbolic referenceToResolve = altNull.getValueToLoad();
                writeBackToSource(state, referenceToResolve);
            }

            @Override
            public void refineResolved(State state, DecisionAlternative_XALOAD_Resolved altResolved)
            throws DecisionException {
                //augments the path condition
                state.assume(Algo_XALOAD.this.ctx.decisionProcedure.simplify(altResolved.getArrayAccessExpression()));

                //if the value is fresh, it writes it back in the array
                if (altResolved.isValueFresh()) {
                    writeBackToSource(state, altResolved.getValueToLoad());
                }
            }

            @Override
            public void refineOut(State state, DecisionAlternative_XALOAD_Out altOut) {
                //augments the path condition
                state.assume(Algo_XALOAD.this.ctx.decisionProcedure.simplify(altOut.getArrayAccessExpression()));
            }
        };
    }

    protected StrategyUpdate_XALOAD updater() {
        return new StrategyUpdate_XALOAD() {
            @Override
            public void updateResolved(State s, DecisionAlternative_XALOAD_Resolved dav) 
            throws DecisionException, InterruptException {
                Algo_XALOAD.this.update(s, dav); //implemented in Algo_XYLOAD_GETX
            }

            @Override
            public void updateReference(State s, DecisionAlternative_XALOAD_Unresolved dar) 
            throws DecisionException, InterruptException {
                Algo_XALOAD.this.update(s, dar); //implemented in Algo_XYLOAD_GETX
            }

            @Override
            public void updateOut(State s, DecisionAlternative_XALOAD_Out dao) 
            throws InterruptException {
                throwNew(s, ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION);
                exitFromAlgorithm();
            }
        };
    }

    @Override
    protected Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }

    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> XALOADSTORE_OFFSET;
    }
}

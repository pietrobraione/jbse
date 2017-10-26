package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.storeInArray;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.INVOKESPECIALSTATICVIRTUAL_OFFSET;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.common.Type.getArrayMemberType;
import static jbse.common.Type.INT;
import static jbse.common.Type.isPrimitive;
import static jbse.common.Type.isPrimitiveOpStack;

import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA;
import jbse.algo.BytecodeCooker;
import jbse.algo.InterruptException;
import jbse.algo.StrategyDecide;
import jbse.algo.StrategyRefine;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.MissingTriggerParameterException;
import jbse.algo.meta.exc.UndefinedResultException;
import jbse.dec.DecisionProcedureAlgorithms.Outcome;
import jbse.dec.exc.DecisionException;
import jbse.mem.Array;
import jbse.mem.State;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_XALOAD;
import jbse.tree.DecisionAlternative_XALOAD_Out;
import jbse.tree.DecisionAlternative_XALOAD_Resolved;
import jbse.val.Expression;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.ReferenceArrayImmaterial;
import jbse.val.ReferenceConcrete;
import jbse.val.Simplex;
import jbse.val.Value;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * Meta-level implementation of {@link sun.misc.Unsafe#getIntVolatile(Object, long)} in 
 * the case the object to read into is an array.
 * 
 * @author Pietro Braione
 */
//TODO heavily copied from Algo_XALOAD and Algo_XYLOAD_GETX: Refactor and merge 
public final class Algo_SUN_UNSAFE_GETINTVOLATILE_Array extends Algo_INVOKEMETA<
DecisionAlternative_XALOAD,
StrategyDecide<DecisionAlternative_XALOAD>, 
StrategyRefine<DecisionAlternative_XALOAD>, 
StrategyUpdate<DecisionAlternative_XALOAD>> {

    private Reference myObjectRef; //set by cooker
    private Primitive index; //set by cooker

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 3;
    }

    @Override
    protected BytecodeCooker bytecodeCooker() {
        return (state) -> {
            try {
                this.myObjectRef = (Reference) this.data.operand(1);
                this.index = (Simplex) this.data.operand(2);
            } catch (ClassCastException e) {
                //this should never happen now
                failExecution(e);
            }

            Array array = null;
            try {
                array = (Array) state.getObject(this.myObjectRef);
                final String arrayType = array.getType();
                if (!getArrayMemberType(arrayType).equals("" + INT)) {
                    throw new UndefinedResultException("The object parameter to sun.misc.Unsafe.getIntVolatile was an array whose member type is not int");
                }
            } catch (ClassCastException e) {
                //this should never happen now
                failExecution(e);
            }
        };
    } 

    @Override
    protected Class<DecisionAlternative_XALOAD> classDecisionAlternative() {
        return DecisionAlternative_XALOAD.class;
    }

    @Override
    protected StrategyDecide<DecisionAlternative_XALOAD> decider() {
        //copied from Algo_XALOAD
        return (state, result) -> { 
            boolean shouldRefine = false;
            boolean branchingDecision = false;
            final LinkedList<Reference> refToArraysToProcess = new LinkedList<>();
            final LinkedList<Expression> accessConditions = new LinkedList<>();
            final LinkedList<Primitive> offsets = new LinkedList<>();
            refToArraysToProcess.add(this.myObjectRef);
            accessConditions.add(null);
            offsets.add(state.getCalculator().valInt(0));
            while (!refToArraysToProcess.isEmpty()) {
                final Reference refToArrayToProcess = refToArraysToProcess.remove();
                final Primitive arrayAccessCondition = accessConditions.remove();
                final Primitive arrayOffset = offsets.remove();
                Array arrayToProcess = null; //to keep the compiler happy
                try {
                    arrayToProcess = (Array) state.getObject(refToArrayToProcess);
                } catch (ClassCastException exc) {
                    //this should never happen
                    failExecution(exc);
                }
                if (arrayToProcess == null) {
                    //this should never happen
                    failExecution("an initial array that backs another array is null");
                }
                Collection<Array.AccessOutcome> entries = null; //to keep the compiler happy
                try {
                    entries = arrayToProcess.get(this.index.add(arrayOffset));
                } catch (InvalidOperandException | InvalidTypeException e) {
                    //this should never happen
                    failExecution(e);
                }
                for (Array.AccessOutcome e : entries) {
                    if (e instanceof Array.AccessOutcomeInInitialArray) {
                        final Array.AccessOutcomeInInitialArray eCast = (Array.AccessOutcomeInInitialArray) e;
                        refToArraysToProcess.add(eCast.getInitialArray());
                        accessConditions.add(e.getAccessCondition());
                        offsets.add(eCast.getOffset());
                    } else { 
                        //puts in val the value of the current entry, or a fresh symbol, 
                        //or null if the index is out of bounds
                        Value val;
                        boolean fresh = false;  //true iff val is a fresh symbol
                        if (e instanceof Array.AccessOutcomeInValue) {
                            val = ((Array.AccessOutcomeInValue) e).getValue();
                            if (val == null) {
                                try {
                                    val = state.createSymbol(getArrayMemberType(arrayToProcess.getType()), 
                                                             arrayToProcess.getOrigin().thenArrayMember(this.index.add(arrayOffset)));
                                } catch (InvalidOperandException | InvalidTypeException exc) {
                                    //this should never happen
                                    failExecution(exc);
                                }
                                fresh = true;
                            }
                        } else { //e instanceof Array.AccessOutcomeOut
                            val = null;
                        }

                        Outcome o = null; //to keep the compiler happy
                        try {
                            final Expression accessCondition = (arrayAccessCondition == null ? e.getAccessCondition() : (Expression) arrayAccessCondition.and(e.getAccessCondition()));
                            o = this.ctx.decisionProcedure.resolve_XALOAD(state, accessCondition, val, fresh, refToArrayToProcess, result);
                        } catch (InvalidOperandException | InvalidTypeException exc) {
                            //this should never happen
                            failExecution(exc);
                        }

                        //here reference expansion check was omitted because
                        //we are getting integers, not references

                        //if at least one read requires refinement, then it should be refined
                        shouldRefine = shouldRefine || o.shouldRefine();

                        //if at least one decision is branching, then it is branching
                        branchingDecision = branchingDecision || o.branchingDecision();
                    }
                }
            }

            //also the size of the result matters to whether refine or not 
            shouldRefine = shouldRefine || (result.size() > 1);

            //for branchingDecision nothing to do: it will be false only if
            //the access is concrete and the value obtained is resolved 
            //(if a symbolic reference): in this case, result.size() must
            //be 1. Note that branchingDecision must be invariant
            //on the used decision procedure, so we cannot make it dependent
            //on result.size().
            return Outcome.val(shouldRefine, /*omitted this.someRefNotExpanded,*/ branchingDecision);
        };
    }

    private void writeBackToSource(State state, Value valueToStore) 
    throws DecisionException {
        storeInArray(state, this.ctx, this.myObjectRef, this.index, valueToStore);
    }

    protected Value possiblyMaterialize(State state, Value val) 
    throws DecisionException, InterruptException {
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
            } catch (HeapMemoryExhaustedException e) {
                throwNew(state, OUT_OF_MEMORY_ERROR);
                exitFromAlgorithm();
                return null; //to keep the compiler happy
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
    protected StrategyRefine_SUN_UNSAFE_GETINTVOLATILE_Array refiner() {
        return new StrategyRefine_SUN_UNSAFE_GETINTVOLATILE_Array() {
            @Override
            public void refineResolved(State state, DecisionAlternative_XALOAD_Resolved altResolved)
            throws DecisionException {
                //augments the path condition
                state.assume(Algo_SUN_UNSAFE_GETINTVOLATILE_Array.this.ctx.decisionProcedure.simplify(altResolved.getArrayAccessExpression()));

                //if the value is fresh, it writes it back in the array
                if (altResolved.isValueFresh()) {
                    writeBackToSource(state, altResolved.getValueToLoad());
                }
            }

            @Override
            public void refineOut(State state, DecisionAlternative_XALOAD_Out altOut) {
                //augments the path condition
                state.assume(Algo_SUN_UNSAFE_GETINTVOLATILE_Array.this.ctx.decisionProcedure.simplify(altOut.getArrayAccessExpression()));
            }
        };
    }

    protected StrategyUpdate_SUN_UNSAFE_GETINTVOLATILE_Array updater() {
        return new StrategyUpdate_SUN_UNSAFE_GETINTVOLATILE_Array() {
            @Override
            public void updateResolved(State state, DecisionAlternative_XALOAD_Resolved altResolved) 
            throws DecisionException, InterruptException, MissingTriggerParameterException {
                //possibly materializes the value
                final Value val = altResolved.getValueToLoad();
                final Value valMaterialized = possiblyMaterialize(state, val);
                final char valMaterializedType = valMaterialized.getType();

                //pushes the value
                try {
                    final Value valToPush;
                    if (isPrimitive(valMaterializedType) && !isPrimitiveOpStack(valMaterializedType)) {
                        valToPush = ((Primitive) valMaterialized).widen(INT);
                    } else {
                        valToPush = valMaterialized;
                    }
                    state.pushOperand(valToPush);
                } catch (ClassCastException | InvalidTypeException | 
                         ThreadStackEmptyException e) {
                    //this should not happen
                    failExecution(e);
                }

                //manages triggers
                try {
                    final boolean someTriggerFrameLoaded = 
                    Algo_SUN_UNSAFE_GETINTVOLATILE_Array.this.ctx.triggerManager.loadTriggerFrames(state, altResolved, Algo_SUN_UNSAFE_GETINTVOLATILE_Array.this.programCounterUpdate.get());
                    if (someTriggerFrameLoaded) {
                        exitFromAlgorithm();
                    }
                } catch (InvalidProgramCounterException e) {
                    throwVerifyError(state);
                    exitFromAlgorithm();
                } catch (ThreadStackEmptyException e) {
                    //this should not happen
                    failExecution(e);
                }
            }

            @Override
            public void updateOut(State s, DecisionAlternative_XALOAD_Out dao) 
            throws UndefinedResultException {
                throw new UndefinedResultException("The offset parameter to sun.misc.Unsafe.getIntVolatile was not a correct index for the object (array) parameter");
            }
        };
    }

    @Override
    protected Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }

    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> INVOKESPECIALSTATICVIRTUAL_OFFSET;
    }
}

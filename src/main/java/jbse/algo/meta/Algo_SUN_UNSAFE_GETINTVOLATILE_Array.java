package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.storeInArray;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.INVOKESPECIALSTATICVIRTUAL_OFFSET;
import static jbse.bc.Signatures.CLASS_NOT_FOUND_EXCEPTION;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.bc.Signatures.UNSUPPORTED_CLASS_VERSION_ERROR;
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
import jbse.algo.exc.NotYetImplementedException;
import jbse.algo.meta.exc.UndefinedResultException;
import jbse.bc.ClassFile;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.dec.DecisionProcedureAlgorithms.Outcome;
import jbse.dec.exc.DecisionException;
import jbse.mem.Array;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
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
 * Meta-level implementation of {@link sun.misc.Unsafe#getIntVolatile(Object, long)} 
 * in the case the object to read into is an array.
 * 
 * @author Pietro Braione
 */
//TODO heavily copied from Algo_XALOAD and Algo_XYLOAD_GETX: Refactor and merge 
//TODO refactor together with Algo_SUN_UNSAFE_GETOBJECTVOLATILE_Array
@SuppressWarnings("restriction")
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
                this.index = ((Simplex) this.data.operand(2)).narrow(INT);
            } catch (ClassCastException | InvalidTypeException e) {
                //this should never happen now
                failExecution(e);
            }

            Array array = null;
            try {
                array = (Array) state.getObject(this.myObjectRef);
                final ClassFile arrayType = array.getType();
                if (!arrayType.getMemberClass().getClassName().equals("int")) {
                    throw new UndefinedResultException("The Object o parameter to sun.misc.Unsafe.getIntVolatile was an array whose member type is not int");
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
        //TODO copied from Algo_XALOAD, unify with it and with Algo_SUN_UNSAFE_GETOBJECTVOLATILE_Array
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
                Primitive indexPlusOffset = null;  //to keep the compiler happy
                Collection<Array.AccessOutcome> entries = null; //to keep the compiler happy
                try {
                	indexPlusOffset = this.index.add(arrayOffset);
                    entries = arrayToProcess.get(indexPlusOffset);
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
                                    val = (Value) state.createSymbolMemberArray(arrayToProcess.getType().getMemberClass().getClassName(), 
                                                             arrayToProcess.getOrigin(), this.index.add(arrayOffset));
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
                            final Expression accessCondition = (arrayAccessCondition == null ? 
                            		                            e.getAccessCondition() : 
                            		                            (Expression) arrayAccessCondition.and(e.getAccessCondition()));
                            o = this.ctx.decisionProcedure.resolve_XALOAD(state, accessCondition, arrayToProcess.getIndex(), indexPlusOffset, val, fresh, refToArrayToProcess, result);
                        //TODO the next catch blocks should disappear, see comments on removing exceptions in jbse.dec.DecisionProcedureAlgorithms.doResolveReference
                        } catch (ClassFileNotFoundException exc) {
                            throwNew(state, CLASS_NOT_FOUND_EXCEPTION);
                            exitFromAlgorithm();
                        } catch (BadClassFileVersionException exc) {
                            throwNew(state, UNSUPPORTED_CLASS_VERSION_ERROR);
                            exitFromAlgorithm();
                        } catch (WrongClassNameException exc) {
                            throwNew(state, NO_CLASS_DEFINITION_FOUND_ERROR); //without wrapping a ClassNotFoundException
                            exitFromAlgorithm();
                        } catch (IncompatibleClassFileException exc) {
                            throwNew(state, INCOMPATIBLE_CLASS_CHANGE_ERROR);
                            exitFromAlgorithm();
                        } catch (ClassFileNotAccessibleException exc) {
                            throwNew(state, ILLEGAL_ACCESS_ERROR);
                            exitFromAlgorithm();
                        } catch (ClassFileIllFormedException exc) {
                            throwVerifyError(state);
                            exitFromAlgorithm();
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
    throws DecisionException, InterruptException, ClasspathException, FrozenStateException {
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
    protected StrategyRefine_SUN_UNSAFE_GETX_Array refiner() {
        return new StrategyRefine_SUN_UNSAFE_GETX_Array() {
            @Override
            public void refineResolved(State state, DecisionAlternative_XALOAD_Resolved altResolved)
            throws DecisionException, InvalidInputException {
                //augments the path condition
            	final Expression accessExpression = altResolved.getArrayAccessExpressionSimplified();
            	if (accessExpression != null) {
            		state.assume(Algo_SUN_UNSAFE_GETINTVOLATILE_Array.this.ctx.decisionProcedure.simplify(accessExpression));
            	}

                //if the value is fresh, it writes it back in the array
                if (altResolved.isValueFresh()) {
                    writeBackToSource(state, altResolved.getValueToLoad());
                }
            }

            @Override
            public void refineOut(State state, DecisionAlternative_XALOAD_Out altOut) 
            throws InvalidInputException {
                //augments the path condition
                try {
                	final Expression accessExpression = altOut.getArrayAccessExpressionSimplified();
                	if (accessExpression != null) {
                		state.assume(Algo_SUN_UNSAFE_GETINTVOLATILE_Array.this.ctx.decisionProcedure.simplify(accessExpression));
                	}
				} catch (DecisionException e) { //TODO propagate exception (...and replace with a better exception)
					//this should never happen
					failExecution(e);
				}
            }
        };
    }

    protected StrategyUpdate_SUN_UNSAFE_GETX_Array updater() {
        return new StrategyUpdate_SUN_UNSAFE_GETX_Array() {
            @Override
            public void updateResolved(State state, DecisionAlternative_XALOAD_Resolved altResolved) 
            throws DecisionException, InterruptException, MissingTriggerParameterException, 
            ClasspathException, NotYetImplementedException, FrozenStateException {
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

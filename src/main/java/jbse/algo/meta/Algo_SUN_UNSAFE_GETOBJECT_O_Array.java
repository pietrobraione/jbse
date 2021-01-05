package jbse.algo.meta;

import static jbse.algo.Util.getFromArray;
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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
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
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.dec.DecisionProcedureAlgorithms.ArrayAccessInfo;
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
import jbse.val.Calculator;
import jbse.val.Expression;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.ReferenceArrayImmaterial;
import jbse.val.ReferenceConcrete;
import jbse.val.ReferenceSymbolic;
import jbse.val.Simplex;
import jbse.val.SymbolicMemberArray;
import jbse.val.Value;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * Meta-level implementation of {@link sun.misc.Unsafe#getObject(Object, long)} and
 * {@link sun.misc.Unsafe#getObjectVolatile(Object, long)} in the case the object 
 * to read into is an array.
 * 
 * @author Pietro Braione
 */
//TODO heavily copied from Algo_XALOAD and Algo_XYLOAD_GETX: Refactor and merge 
//TODO refactor together with Algo_SUN_UNSAFE_GETINTVOLATILE_Array
public final class Algo_SUN_UNSAFE_GETOBJECT_O_Array extends Algo_INVOKEMETA<
DecisionAlternative_XALOAD,
StrategyDecide<DecisionAlternative_XALOAD>, 
StrategyRefine<DecisionAlternative_XALOAD>, 
StrategyUpdate<DecisionAlternative_XALOAD>> {

    private Reference myObjectRef; //set by cooker
    private Simplex index; //set by cooker

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 3;
    }

    @Override
    protected BytecodeCooker bytecodeCooker() {
        return (state) -> {
            try {
                this.myObjectRef = (Reference) this.data.operand(1);
                this.index = (Simplex) this.ctx.getCalculator().push((Simplex) this.data.operand(2)).narrow(INT).pop();
            } catch (ClassCastException | InvalidTypeException e) {
                //this should never happen now
                failExecution(e);
            }

            Array array = null;
            try {
                array = (Array) state.getObject(this.myObjectRef);
                final ClassFile arrayMemberType = array.getType().getMemberClass();
                if (!arrayMemberType.isReference() && !arrayMemberType.isArray()) {
                    throw new UndefinedResultException("The object parameter to sun.misc.Unsafe.getObject[Volatile] was an array whose member type is not a reference type.");
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
        //TODO unify with Algo_SUN_UNSAFE_GETINTVOLATILE_Array
        return (state, result) -> { 
            //builds the ArrayAccessInfos by reading the array
            final List<ArrayAccessInfo> arrayAccessInfos = getFromArray(state, this.ctx.getCalculator(), this.myObjectRef, this.index);
                        
            //invokes the decision procedure
            Outcome o = null; //to keep the compiler happy
            final ArrayList<ReferenceSymbolic> nonExpandedRefs = new ArrayList<>(); //dummy
            try {
                o = this.ctx.decisionProcedure.resolve_XALOAD(arrayAccessInfos, result, nonExpandedRefs);
            //TODO the next catch blocks should disappear, see comments on removing exceptions in jbse.dec.DecisionProcedureAlgorithms.doResolveReference
            } catch (ClassFileNotFoundException exc) {
                throwNew(state, this.ctx.getCalculator(), CLASS_NOT_FOUND_EXCEPTION);
                exitFromAlgorithm();
            } catch (BadClassFileVersionException exc) {
                throwNew(state, this.ctx.getCalculator(), UNSUPPORTED_CLASS_VERSION_ERROR);
                exitFromAlgorithm();
            } catch (WrongClassNameException exc) {
                throwNew(state, this.ctx.getCalculator(), NO_CLASS_DEFINITION_FOUND_ERROR); //without wrapping a ClassNotFoundException
                exitFromAlgorithm();
            } catch (IncompatibleClassFileException exc) {
                throwNew(state, this.ctx.getCalculator(), INCOMPATIBLE_CLASS_CHANGE_ERROR);
                exitFromAlgorithm();
            } catch (ClassFileNotAccessibleException exc) {
                throwNew(state, this.ctx.getCalculator(), ILLEGAL_ACCESS_ERROR);
                exitFromAlgorithm();
            } catch (HeapMemoryExhaustedException exc) {
                throwNew(state, this.ctx.getCalculator(), OUT_OF_MEMORY_ERROR);
                exitFromAlgorithm();
            } catch (ClassFileIllFormedException exc) {
                throwVerifyError(state, this.ctx.getCalculator());
                exitFromAlgorithm();
            } catch (RenameUnsupportedException e) {
            	//this should never happen
            	failExecution(e);
            }
            
            //no-reference-expansion code omitted because
            //we are getting integers, not references
            
            return o;
        };
    }

    private void writeBackToSource(State state, Primitive index, Value valueToStore) 
    throws DecisionException {
        storeInArray(state, this.ctx, this.myObjectRef, index, valueToStore);
    }

    protected Value possiblyMaterialize(State state, Value val) 
    throws DecisionException, InterruptException, ClasspathException, InvalidInputException {
        //calculates the actual value to push by materializing 
        //a member array, if it is the case, and then pushes it
        //on the operand stack
        if (val instanceof ReferenceArrayImmaterial) { //TODO eliminate manual dispatch
            try {
                final ReferenceArrayImmaterial valRef = (ReferenceArrayImmaterial) val;
                final ReferenceConcrete valMaterialized = 
                    state.createArray(this.ctx.getCalculator(), valRef.getMember(), valRef.getLength(), valRef.getArrayType());
                writeBackToSource(state, this.index, valMaterialized);
                return valMaterialized;
            } catch (HeapMemoryExhaustedException e) {
                throwNew(state, this.ctx.getCalculator(), OUT_OF_MEMORY_ERROR);
                exitFromAlgorithm();
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
            		state.assume(Algo_SUN_UNSAFE_GETOBJECT_O_Array.this.ctx.getCalculator().simplify(Algo_SUN_UNSAFE_GETOBJECT_O_Array.this.ctx.decisionProcedure.simplify(accessExpression)));
            	}

                //if the value is fresh, it writes it back in the array
                if (altResolved.isValueFresh()) {
                    final Value valueToLoad = altResolved.getValueToLoad();
                	final Primitive index = ((SymbolicMemberArray) valueToLoad).getIndex();
                    writeBackToSource(state, index, valueToLoad);
                }
            }

            @Override
            public void refineOut(State state, DecisionAlternative_XALOAD_Out altOut) 
            throws InvalidInputException {
                //augments the path condition
                try {
                	final Expression accessExpression = altOut.getArrayAccessExpressionSimplified();
                	if (accessExpression != null) {
                		state.assume(Algo_SUN_UNSAFE_GETOBJECT_O_Array.this.ctx.getCalculator().simplify(Algo_SUN_UNSAFE_GETOBJECT_O_Array.this.ctx.decisionProcedure.simplify(accessExpression)));
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
            ClasspathException, NotYetImplementedException, NoSuchElementException, InvalidOperandException, InvalidInputException {
            	final Calculator calc = Algo_SUN_UNSAFE_GETOBJECT_O_Array.this.ctx.getCalculator();
            	
                //possibly materializes the value
                final Value val = altResolved.getValueToLoad();
                final Value valMaterialized = possiblyMaterialize(state, val);
                final char valMaterializedType = valMaterialized.getType();

                //pushes the value
                try {
                    final Value valToPush;
                    //in the next if only the else branch is taken, but we keep it to highlight the fact that it can be refactored together with other Algo_SUN_UNSAFE_GETX_Array 
                    if (isPrimitive(valMaterializedType) && !isPrimitiveOpStack(valMaterializedType)) {
                        valToPush = calc.push((Primitive) valMaterialized).widen(INT).pop();
                    } else {
                        valToPush = valMaterialized;
                    }
                    state.pushOperand(valToPush);
                } catch (ClassCastException | InvalidTypeException | 
                         ThreadStackEmptyException e) { //TODO propagate exceptions
                    //this should not happen
                    failExecution(e);
                }

                //manages triggers
                try {
                    final boolean someTriggerFrameLoaded = 
                        Algo_SUN_UNSAFE_GETOBJECT_O_Array.this.ctx.triggerManager.loadTriggerFrames(state, calc, altResolved, Algo_SUN_UNSAFE_GETOBJECT_O_Array.this.programCounterUpdate.get());
                    if (someTriggerFrameLoaded) {
                        exitFromAlgorithm();
                    }
                } catch (InvalidProgramCounterException e) { //TODO propagate exceptions?
                    throwVerifyError(state, calc);
                    exitFromAlgorithm();
                } catch (ThreadStackEmptyException e) {
                    //this should not happen
                    failExecution(e);
                }
            }

            @Override
            public void updateOut(State s, DecisionAlternative_XALOAD_Out dao) 
            throws UndefinedResultException {
                throw new UndefinedResultException("The offset parameter to sun.misc.Unsafe.getObject[Volatile] was not a correct index for the object (array) parameter");
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

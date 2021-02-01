package jbse.algo;

import static jbse.algo.Util.getFromArray;
import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.storeInArray;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.XALOADSTORE_OFFSET;
import static jbse.bc.Signatures.ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.bc.Signatures.UNSUPPORTED_CLASS_VERSION_ERROR;

import java.util.List;
import java.util.function.Supplier;

import jbse.algo.exc.MissingTriggerParameterException;
import jbse.algo.exc.NotYetImplementedException;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.Type;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.dec.DecisionProcedureAlgorithms.ArrayAccessInfo;
import jbse.dec.DecisionProcedureAlgorithms.Outcome;
import jbse.dec.exc.DecisionException;
import jbse.mem.Array;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_XALOAD;
import jbse.tree.DecisionAlternative_XALOAD_Out;
import jbse.tree.DecisionAlternative_XALOAD_Unresolved;
import jbse.tree.DecisionAlternative_XALOAD_Aliases;
import jbse.tree.DecisionAlternative_XALOAD_Null;
import jbse.tree.DecisionAlternative_XALOAD_Expands;
import jbse.tree.DecisionAlternative_XALOAD_Resolved;
import jbse.val.Expression;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.ReferenceArrayImmaterial;
import jbse.val.ReferenceConcrete;
import jbse.val.ReferenceSymbolic;
import jbse.val.SymbolicMemberArray;
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
                this.myObjectRef = (Reference) this.data.operand(0);
                this.index = (Primitive) this.data.operand(1);
            } catch (ClassCastException e) {
                throwVerifyError(state, this.ctx.getCalculator());
                exitFromAlgorithm();
            }

            //null check
            if (state.isNull(this.myObjectRef)) {
                throwNew(state, this.ctx.getCalculator(), NULL_POINTER_EXCEPTION);
                exitFromAlgorithm();
            }

            //index check
            if (this.index == null || this.index.getType() != Type.INT) {
                throwVerifyError(state, this.ctx.getCalculator());
                exitFromAlgorithm();
            }

            //object check
            if (!(state.getObject(this.myObjectRef) instanceof Array)) {
                throwVerifyError(state, this.ctx.getCalculator());
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
            //reads the array
            final List<ArrayAccessInfo> arrayAccessInfos = getFromArray(state, this.ctx.getCalculator(), this.myObjectRef, this.index);
            
            //invokes the decision procedure
            Outcome o = null; //to keep the compiler happy
            try {
                o = this.ctx.decisionProcedure.resolve_XALOAD(arrayAccessInfos, result, this.partiallyResolvedReferences);
            //TODO the next catch blocks should disappear, see comments on removing exceptions in jbse.dec.DecisionProcedureAlgorithms.doResolveReference
            } catch (ClassFileNotFoundException exc) {
                //TODO this exception should wrap a ClassNotFoundException
                throwNew(state, this.ctx.getCalculator(), NO_CLASS_DEFINITION_FOUND_ERROR);
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
            
            //stores info about the non expanded references
            this.someReferencePartiallyResolved = o.partialReferenceResolution();
            
            return o;
        };
    }

    private void writeBackToSource(State state, Reference refToSource, Primitive index, Value valueToStore) 
    throws DecisionException {
        storeInArray(state, this.ctx, refToSource, index, valueToStore);
    }

    @Override   
    protected Value possiblyMaterialize(State state, Value val) 
    throws DecisionException, InterruptException, ClasspathException, FrozenStateException {
        //calculates the actual value to push by materializing 
        //a member array, if it is the case, and then pushes it
        //on the operand stack
        if (val instanceof ReferenceArrayImmaterial) { //TODO eliminate manual dispatch
            try {
                final ReferenceArrayImmaterial valRef = (ReferenceArrayImmaterial) val;
                final ReferenceConcrete valMaterialized = 
                    state.createArray(this.ctx.getCalculator(), valRef.getMember(), valRef.getLength(), valRef.getArrayType());
                writeBackToSource(state, this.myObjectRef, this.index, valMaterialized); //TODO is the parameter this.myObjectRef correct?????
                return valMaterialized;
            } catch (HeapMemoryExhaustedException e) {
                throwNew(state, this.ctx.getCalculator(), OUT_OF_MEMORY_ERROR);
                exitFromAlgorithm();
                return null; //to keep the compiler happy
            } catch (InvalidInputException e) {
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
            throws DecisionException, ContradictionException, InvalidTypeException, InvalidInputException, 
            InterruptException, SymbolicValueNotAllowedException, ClasspathException {
                //handles all the assumptions for reference resolution by expansion
                Algo_XALOAD.this.refineRefExpands(state, altExpands); //implemented in Algo_XYLOAD_GETX

                //assumes the array access expression (index in range)
                final Expression accessExpression = altExpands.getArrayAccessExpressionSimplified();
            	if (accessExpression != null) {
            		state.assume(Algo_XALOAD.this.ctx.getCalculator().simplify(Algo_XALOAD.this.ctx.decisionProcedure.simplify(accessExpression)));
            	}

                //if the value is fresh, writes it back in the array
                if (altExpands.isValueFresh()) { //pleonastic: all unresolved symbolic references from an array are fresh
                    final Reference source = altExpands.getArrayReference();
                    final ReferenceSymbolic referenceToExpand = altExpands.getValueToLoad();
                	final Primitive index = ((SymbolicMemberArray) referenceToExpand).getIndex();
                    writeBackToSource(state, source, index, referenceToExpand);
                }
            }

            @Override
            public void refineRefAliases(State state, DecisionAlternative_XALOAD_Aliases altAliases)
            throws DecisionException, ContradictionException, InvalidInputException, ClasspathException, 
            InterruptException {
                //handles all the assumptions for reference resolution by aliasing
                Algo_XALOAD.this.refineRefAliases(state, altAliases); //implemented in Algo_XYLOAD_GETX

                //assumes the array access expression (index in range)
                final Expression accessExpression = altAliases.getArrayAccessExpressionSimplified();
            	if (accessExpression != null) {
            		state.assume(Algo_XALOAD.this.ctx.getCalculator().simplify(Algo_XALOAD.this.ctx.decisionProcedure.simplify(accessExpression)));
            	}

                //if the value is fresh, writes it back in the array
                if (altAliases.isValueFresh()) { //pleonastic: all unresolved symbolic references from an array are fresh
                    final Reference source = altAliases.getArrayReference();
                    final ReferenceSymbolic referenceToResolve = altAliases.getValueToLoad();
                	final Primitive index = ((SymbolicMemberArray) referenceToResolve).getIndex();
                    writeBackToSource(state, source, index, referenceToResolve);
                }
            }

            @Override
            public void refineRefNull(State state, DecisionAlternative_XALOAD_Null altNull) 
            throws DecisionException, ContradictionException, InvalidInputException {
                Algo_XALOAD.this.refineRefNull(state, altNull); //implemented in Algo_XYLOAD_GETX

                //further augments the path condition 
                final Expression accessExpression = altNull.getArrayAccessExpressionSimplified();
            	if (accessExpression != null) {
            		state.assume(Algo_XALOAD.this.ctx.getCalculator().simplify(Algo_XALOAD.this.ctx.decisionProcedure.simplify(accessExpression)));
            	}

                //if the value is fresh, writes it back in the array
                if (altNull.isValueFresh()) { //pleonastic: all unresolved symbolic references from an array are fresh
                    final Reference source = altNull.getArrayReference();
                    final ReferenceSymbolic referenceToResolve = altNull.getValueToLoad();
                	final Primitive index = ((SymbolicMemberArray) referenceToResolve).getIndex();
                    writeBackToSource(state, source, index, referenceToResolve);
                }
            }

            @Override
            public void refineResolved(State state, DecisionAlternative_XALOAD_Resolved altResolved)
            throws DecisionException, InvalidInputException {
                //augments the path condition
            	final Expression accessExpression = altResolved.getArrayAccessExpressionSimplified();
            	if (accessExpression != null) {
            		state.assume(Algo_XALOAD.this.ctx.getCalculator().simplify(Algo_XALOAD.this.ctx.decisionProcedure.simplify(accessExpression)));
            	}

                //if the value is fresh, writes it back in the array
                if (altResolved.isValueFresh()) {
                    final Reference source = altResolved.getArrayReference();
                    final Value valueToLoad = altResolved.getValueToLoad();
                	final Primitive index = ((SymbolicMemberArray) valueToLoad).getIndex();
                    writeBackToSource(state, source, index, valueToLoad);
                }
            }

            @Override
            public void refineOut(State state, DecisionAlternative_XALOAD_Out altOut) 
            throws InvalidInputException {
                //augments the path condition
                try {
                	final Expression accessExpression = altOut.getArrayAccessExpressionSimplified();
                	if (accessExpression != null) {
                		state.assume(Algo_XALOAD.this.ctx.getCalculator().simplify(Algo_XALOAD.this.ctx.decisionProcedure.simplify(accessExpression)));
                	}
				} catch (DecisionException e) { //TODO propagate this exception (...and replace with a better exception)
					//this should never happen
					failExecution(e);
				}
            }
        };
    }

    protected StrategyUpdate_XALOAD updater() {
        return new StrategyUpdate_XALOAD() {
            @Override
            public void updateResolved(State s, DecisionAlternative_XALOAD_Resolved dar) 
            throws DecisionException, InterruptException, MissingTriggerParameterException, 
            ClasspathException, NotYetImplementedException, ThreadStackEmptyException, 
            InvalidOperandException, InvalidInputException {
                Algo_XALOAD.this.update(s, dar); //implemented in Algo_XYLOAD_GETX
            }

            @Override
            public void updateUnresolved(State s, DecisionAlternative_XALOAD_Unresolved dau) 
            throws DecisionException, InterruptException, MissingTriggerParameterException, 
            ClasspathException, NotYetImplementedException, ThreadStackEmptyException, 
            InvalidOperandException, InvalidInputException {
                Algo_XALOAD.this.update(s, dau); //implemented in Algo_XYLOAD_GETX
            }

            @Override
            public void updateOut(State s, DecisionAlternative_XALOAD_Out dao) 
            throws InterruptException, ClasspathException {
                throwNew(s, Algo_XALOAD.this.ctx.getCalculator(), ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION);
                exitFromAlgorithm();
            }
        };
    }

    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> XALOADSTORE_OFFSET;
    }
}

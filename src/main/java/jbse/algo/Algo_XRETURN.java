package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.invokeClassLoaderLoadClass;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.bc.Signatures.UNSUPPORTED_CLASS_VERSION_ERROR;
import static jbse.common.Type.className;
import static jbse.common.Type.INT;
import static jbse.common.Type.NULLREF;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.isPrimitive;
import static jbse.common.Type.isPrimitiveOpStack;
import static jbse.common.Type.isReference;
import static jbse.common.Type.splitReturnValueDescriptor;

import java.util.function.Supplier;

import jbse.bc.ClassFile;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.PleaseLoadClassException;
import jbse.bc.exc.WrongClassNameException;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.mem.State.Phase;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.Value;
import jbse.val.exc.InvalidTypeException;

/**
 * {@link Algorithm} for all the "return from method" bytecodes
 * ([i/l/f/d/a]return).
 * 
 * @author Pietro Braione
 *
 */
final class Algo_XRETURN extends Algorithm<
BytecodeData_0, 
DecisionAlternative_NONE,
StrategyDecide<DecisionAlternative_NONE>,
StrategyRefine<DecisionAlternative_NONE>,
StrategyUpdate<DecisionAlternative_NONE>> {
    
    private final char returnType; //set by constructor
    private Value valueToReturn; //set by cooker
    private int pcReturn; //set by updater

    /**
     * Constructor.
     * 
     * @param returnType the type of the value that
     *        must be returned.
     */
    public Algo_XRETURN(char returnType) {
        this.returnType = returnType;
    }

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected Supplier<BytecodeData_0> bytecodeData() {
        return BytecodeData_0::get;
    }

    @Override
    protected BytecodeCooker bytecodeCooker() {
        return (state) -> {
            this.valueToReturn = this.data.operand(0);
            final char valueType = this.valueToReturn.getType();
            if ((valueType != this.returnType) && !(valueType == NULLREF && this.returnType == REFERENCE)) {
                throwVerifyError(state, this.ctx.getCalculator());
                exitFromAlgorithm();
            }
            //TODO this code is duplicated in Algo_PUTX: refactor! 
        	final Calculator calc = this.ctx.getCalculator();
            try {            	
                //checks/converts the type of the value to be returned
                final ClassFile currentClass = state.getCurrentClass();
                final String currentMethodDescriptor = state.getCurrentMethodSignature().getDescriptor();
                if (currentMethodDescriptor == null) {
                	//this happens with no-wrap snippet frames: just accept
                	//the return value (i.e., do nothing)
                } else {
                	final String destinationType = splitReturnValueDescriptor(currentMethodDescriptor);
                	if (isPrimitive(destinationType)) {
                		final char destinationTypePrimitive = destinationType.charAt(0);
                		if (isPrimitiveOpStack(destinationTypePrimitive)) {
                			if (valueType != destinationTypePrimitive) {
                				throwVerifyError(state, this.ctx.getCalculator());
                				exitFromAlgorithm();
                			}
                		} else if (valueType == INT) {
                			//TODO the JVMS v8 does *not* say that in this case the value should be narrowed to the destination type: Rather, it should just be *reinterpreted*. Unfortunately JBSE cannot do that so it uses narrowing instead, and this is a bug. However in standard bytecode a value is narrowed before being reinterpreted, so it should not be an issue in the most typical case. 
                			try {
                				this.valueToReturn = calc.push((Primitive) this.valueToReturn).narrow(destinationTypePrimitive).pop();
                			} catch (InvalidTypeException e) {
                				//this should never happen
                				failExecution(e);
                			}
                		} else {
                			throwVerifyError(state, this.ctx.getCalculator());
                			exitFromAlgorithm();
                		}
                	} else if (isReference(valueType)) {
                		final Reference refToReturn = (Reference) this.valueToReturn;
                		if (!state.isNull(refToReturn)) {
                			//TODO the JVMS v8, *return instruction, does not explicitly say how and when the return descriptor type is resolved  
                			final ClassFile destinationTypeClass = state.getClassHierarchy().resolveClass(currentClass, className(destinationType), state.bypassStandardLoading());
                			final ClassFile valueObjectType = state.getObject(refToReturn).getType();
                			if (!state.getClassHierarchy().isAssignmentCompatible(valueObjectType, destinationTypeClass)) {
                				throwVerifyError(state, this.ctx.getCalculator());
                				exitFromAlgorithm();
                			}
                		}
                	} else if (valueType == NULLREF) {
                		//nothing to do
                	} else { //destination has reference type, value has primitive type
                		throwVerifyError(state, this.ctx.getCalculator());
                		exitFromAlgorithm();
                	}
                }
            } catch (PleaseLoadClassException e) {
                invokeClassLoaderLoadClass(state, this.ctx.getCalculator(), e);
                exitFromAlgorithm();
            } catch (ClassFileNotFoundException e) {
                //TODO this exception should wrap a ClassNotFoundException
                throwNew(state, this.ctx.getCalculator(), NO_CLASS_DEFINITION_FOUND_ERROR);
                exitFromAlgorithm();
            } catch (BadClassFileVersionException e) {
                throwNew(state, this.ctx.getCalculator(), UNSUPPORTED_CLASS_VERSION_ERROR);
                exitFromAlgorithm();
            } catch (WrongClassNameException e) {
                throwNew(state, this.ctx.getCalculator(), NO_CLASS_DEFINITION_FOUND_ERROR); //without wrapping a ClassNotFoundException
                exitFromAlgorithm();
            } catch (IncompatibleClassFileException e) {
                throwNew(state, this.ctx.getCalculator(), INCOMPATIBLE_CLASS_CHANGE_ERROR);
                exitFromAlgorithm();
            } catch (ClassFileNotAccessibleException e) {
                throwNew(state, this.ctx.getCalculator(), ILLEGAL_ACCESS_ERROR);
                exitFromAlgorithm();
            } catch (ClassFileIllFormedException e) {
                throwVerifyError(state, this.ctx.getCalculator());
                exitFromAlgorithm();
            }
            
            //since the value to return goes on the operand stack, it 
            //must be widened to int if it is a boolean, byte, char or short
            if (isPrimitive(this.valueToReturn.getType()) && !isPrimitiveOpStack(this.valueToReturn.getType())) {
                try {
                    this.valueToReturn = calc.push((Primitive) this.valueToReturn).widen(INT).pop();
                } catch (InvalidTypeException e) {
                    //this should never happen
                    failExecution(e);
                }
            }
        };        
    }

    @Override
    protected Class<DecisionAlternative_NONE> classDecisionAlternative() {
        return DecisionAlternative_NONE.class;
    }

    @Override
    protected StrategyDecide<DecisionAlternative_NONE> decider() {
        return (state, result) -> {
            result.add(DecisionAlternative_NONE.instance());
            return DecisionProcedureAlgorithms.Outcome.FF;
        };
    }

    @Override
    protected StrategyRefine<DecisionAlternative_NONE> refiner() {
        return (state, alt) -> { };
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.popCurrentFrame();
            if (state.getStackSize() == 0) {
            	if (state.phase() == Phase.POST_INITIAL) {
            		state.setStuckReturn(this.valueToReturn);
            	}
            } else {
                state.pushOperand(this.valueToReturn);
                this.pcReturn = state.getReturnPC();
            }
        };
    }

    @Override
    protected Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> false;
    }

    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> this.pcReturn;
    }
}

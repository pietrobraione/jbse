package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.invokeClassLoaderLoadClass;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.ClassLoaders.CLASSLOADER_BOOT;
import static jbse.bc.Offsets.GETX_PUTX_OFFSET;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.JAVA_OBJECT;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.bc.Signatures.NO_SUCH_FIELD_ERROR;
import static jbse.bc.Signatures.UNSUPPORTED_CLASS_VERSION_ERROR;
import static jbse.common.Type.className;
import static jbse.common.Type.INT;
import static jbse.common.Type.isPrimitive;
import static jbse.common.Type.isPrimitiveOpStack;
import static jbse.common.Type.isReference;
import static jbse.common.Type.NULLREF;

import java.util.ListIterator;
import java.util.function.Supplier;

import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.FieldNotAccessibleException;
import jbse.bc.exc.FieldNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.PleaseLoadClassException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.exc.ClasspathException;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.dec.exc.DecisionException;
import jbse.mem.Frame;
import jbse.mem.HeapObjekt;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.Value;
import jbse.val.exc.InvalidTypeException;

//TODO extract common superclass with Algo_GETX and eliminate duplicate code
/**
 * Abstract {@link Algorithm} managing all the put* bytecodes
 * (putfield, putstatic).
 * 
 * @author Pietro Braione
 */
abstract class Algo_PUTX extends Algorithm<
BytecodeData_1FI,
DecisionAlternative_NONE, 
StrategyDecide<DecisionAlternative_NONE>, 
StrategyRefine<DecisionAlternative_NONE>, 
StrategyUpdate<DecisionAlternative_NONE>> {
    private final boolean isStatic; //set by subclass via constructor
    protected ClassFile fieldClassResolved; //set by cook
    protected Value valueToPut;     //set by subclass
    
    public Algo_PUTX(boolean isStatic) {
        this.isStatic = isStatic;
    }

    @Override
    protected final Supplier<BytecodeData_1FI> bytecodeData() {
        return BytecodeData_1FI::get;
    }

    @Override
    protected final BytecodeCooker bytecodeCooker() {
        return (state) -> {
            try {
                //gets the value to put
                this.valueToPut = valueToPut();
                
                //performs field resolution
                final ClassFile currentClass = state.getCurrentClass();    
                this.fieldClassResolved = state.getClassHierarchy().resolveField(currentClass, this.data.signature(), state.bypassStandardLoading());

                //checks that, if the field is final, then it is declared in the current class and the
                //current method executes in the context of an initialization method
                if (this.fieldClassResolved.isFieldFinal(this.data.signature())) {
                    final String initializationMethodName = (this.isStatic ? "<clinit>" : "<init>");
                    boolean notInInitializationContext = true;
                    final ListIterator<Frame> iterator = state.getStack().listIterator(state.getStack().size());
                    while (iterator.hasPrevious()) {
                        final Frame f = iterator.previous();
                        if (!this.fieldClassResolved.equals(f.getMethodClass())) {
                            break;
                        }
                        if (initializationMethodName.equals(f.getMethodSignature().getName())) {
                            notInInitializationContext = false;
                            break;
                        }
                    }
                    if (notInInitializationContext) {
                        throwNew(state, this.ctx.getCalculator(), ILLEGAL_ACCESS_ERROR);
                        exitFromAlgorithm();
                    }
                }

                //TODO this code is duplicated in Algo_XRETURN: refactor! 
                //checks/converts the type of the value to be put
                final String destinationType = this.data.signature().getDescriptor();
                final char valueType = this.valueToPut.getType();
                if (isPrimitive(destinationType)) {
                    final char fieldTypePrimitive = destinationType.charAt(0);
                    if (isPrimitiveOpStack(fieldTypePrimitive)) {
                        if (valueType != fieldTypePrimitive) {
                            throwVerifyError(state, this.ctx.getCalculator());
                            exitFromAlgorithm();
                        }
                    } else if (valueType == INT) {
                    	//TODO the JVMS v8 does *not* say that in this case the value should be narrowed to the destination type: Rather, it should just be *reinterpreted*. Unfortunately JBSE cannot do that so it uses narrowing instead, and this is a bug. However in standard bytecode a value is narrowed before being reinterpreted, so it should not be an issue in the most typical case. 
                        try {
                            this.valueToPut = this.ctx.getCalculator().push((Primitive) this.valueToPut).narrow(fieldTypePrimitive).pop();
                        } catch (InvalidTypeException e) {
                            //this should never happen
                            failExecution(e);
                        }
                    } else {
                        throwVerifyError(state, this.ctx.getCalculator());
                        exitFromAlgorithm();
                    }
                } else if (isReference(valueType)) {
                    final Reference refToPut = (Reference) this.valueToPut;
                    if (!state.isNull(refToPut)) {
                        //TODO the JVMS v8, putfield instruction, does not explicitly say how and when the field descriptor type is resolved  
                        final ClassFile destinationTypeClass = state.getClassHierarchy().resolveClass(currentClass, className(destinationType), state.bypassStandardLoading());
                        final HeapObjekt valueObject = state.getObject(refToPut);
                        final ClassFile valueObjectType; 
                        if (valueObject == null) { 
                        	//it is a KlassPseudoReference originated by Unsafe.staticFieldBase
                        	valueObjectType = state.getClassHierarchy().clone().getClassFileClassArray(CLASSLOADER_BOOT, JAVA_OBJECT); //surely loaded
                        } else {
                        	valueObjectType = valueObject.getType();
                        }
                        if (!state.getClassHierarchy().isAssignmentCompatible(valueObjectType, destinationTypeClass)) {
                            throwVerifyError(state, this.ctx.getCalculator());
                            exitFromAlgorithm();
                        }
                    }
                } else if (valueType == NULLREF) {
                    //nothing to do
                } else { //field has reference type, value has primitive type
                    throwVerifyError(state, this.ctx.getCalculator());
                    exitFromAlgorithm();
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
            } catch (FieldNotFoundException e) {
                throwNew(state, this.ctx.getCalculator(), NO_SUCH_FIELD_ERROR);
                exitFromAlgorithm();
            } catch (ClassFileNotAccessibleException | FieldNotAccessibleException e) {
                throwNew(state, this.ctx.getCalculator(), ILLEGAL_ACCESS_ERROR);
                exitFromAlgorithm();
            } catch (ClassFileIllFormedException e) {
                throwVerifyError(state, this.ctx.getCalculator());
                exitFromAlgorithm();
            }

            //does bytecode-specific checks
            try {
                checkMore(state);
            } catch (FieldNotFoundException e) {
                //this should never happen
                failExecution(e);
            }
        };
    }

    @Override
    protected final StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            final Signature fieldSignatureResolved = new Signature(this.fieldClassResolved.getClassName(), this.data.signature().getDescriptor(), this.data.signature().getName());
            destination(state).setFieldValue(fieldSignatureResolved, this.valueToPut);
        };
    }

    @Override
    protected final Class<DecisionAlternative_NONE> classDecisionAlternative() {
        return DecisionAlternative_NONE.class;
    }

    @Override
    protected final StrategyDecide<DecisionAlternative_NONE> decider() {
        return (state, result) -> {
            result.add(DecisionAlternative_NONE.instance());
            return DecisionProcedureAlgorithms.Outcome.FF;
        };
    }

    @Override
    protected final StrategyRefine<DecisionAlternative_NONE> refiner() {
        return (state, alt) -> { };
    }

    /**
     * Returns the value to be put.
     * 
     * @return a {@link Value}.
     */
    protected abstract Value valueToPut();

    /**
     * Checks whether the destination of this put (a static or nonstatic
     * field) is correct for the bytecode (bytecode-specific checks).
     * 
     * @param state the current {@link State}.
     * @throws FieldNotFoundException if the field does not exist.
     * @throws DecisionException if the decision procedure fails.
     * @throws ClasspathException if a standard class is not found.
     * @throws InterruptException if the {@link Algorithm} must be interrupted.
     * @throws ThreadStackEmptyException if the stack is empty.
     * @throws ContradictionException  if some initialization assumption is
     *         contradicted.
     * @throws FrozenStateException if {@code state} is frozen.
     */
    protected abstract void checkMore(State state)
    throws FieldNotFoundException, DecisionException, 
    ClasspathException, InterruptException, ThreadStackEmptyException, 
    ContradictionException, FrozenStateException;
    
    /**
     * Returns the destination puts the value to its destination. 
     * 
     * @param state a {@link State}.
     * @return the {@link Objekt} containing the field where the
     *         value must be put.
     * @throws InterruptException if the {@link Algorithm} must be interrupted.
     * @throws ClasspathException if it needs to throw a {@code java.lang.VerifyException}
     *         but it is not found, or ill-formed, or cannot access its superclasses/superinterfaces.
     * @throws FrozenStateException if {@code state} is frozen.
     */
    protected abstract Objekt destination(State state)
    throws InterruptException, ClasspathException, FrozenStateException;

    @Override
    protected final Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }

    @Override
    protected final Supplier<Integer> programCounterUpdate() {
        return () -> GETX_PUTX_OFFSET;
    }
}

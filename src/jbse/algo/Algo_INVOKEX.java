package jbse.algo;

import static jbse.algo.Util.continueWith;
import static jbse.algo.Util.ensureClassCreatedAndInitialized;
import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.lookupMethodImpl;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.INVOKEDYNAMICINTERFACE_OFFSET;
import static jbse.bc.Offsets.INVOKESPECIALSTATICVIRTUAL_OFFSET;
import static jbse.bc.Signatures.ABSTRACT_METHOD_ERROR;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.bc.Signatures.NO_SUCH_METHOD_ERROR;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;
import static jbse.common.Type.splitParametersDescriptors;

import java.util.function.Supplier;

import jbse.algo.exc.CannotInvokeNativeException;
import jbse.algo.exc.MetaUnsupportedException;
import jbse.bc.ClassFile;
import jbse.bc.ClassHierarchy;
import jbse.bc.Signature;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.MethodAbstractException;
import jbse.bc.exc.MethodCodeNotFoundException;
import jbse.bc.exc.MethodNotAccessibleException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.bc.exc.NullMethodReceiverException;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.InvalidSlotException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Reference;

final class Algo_INVOKEX extends Algorithm<
BytecodeData_1ME,
DecisionAlternative_NONE,
StrategyDecide<DecisionAlternative_NONE>, 
StrategyRefine<DecisionAlternative_NONE>, 
StrategyUpdate<DecisionAlternative_NONE>> {
    
    private final boolean isInterface; //set by the constructor
    private final boolean isSpecial; //set by the constructor
    private final boolean isStatic; //set by the constructor
    
    public Algo_INVOKEX(boolean isInterface, boolean isSpecial, boolean isStatic) {
        this.isInterface = isInterface;
        this.isSpecial = isSpecial;
        this.isStatic = isStatic;
    }
    
    private int nParams; //set by cooker
    private int pcOffsetReturn; //set by cooker
    private boolean isNative; //set by cooker
    private Signature methodSignatureResolved; //set by cooker
    private Signature methodSignatureImpl; //set by cooker
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> this.nParams;
    }
    
    @Override
    protected Supplier<BytecodeData_1ME> bytecodeData() {
        return () -> BytecodeData_1ME.withInterfaceMethod(this.isInterface).get();
    }
    
    @Override
    protected BytecodeCooker bytecodeCooker() {
        return (state) -> {
            final ClassHierarchy hier = state.getClassHierarchy();
            
            //calculates the number of parameters
            final String[] paramsDescriptors = splitParametersDescriptors(this.data.signature().getDescriptor());
            this.nParams = (this.isStatic ? paramsDescriptors.length : paramsDescriptors.length + 1);
            
            //sets the program counter offset for the return point
            this.pcOffsetReturn = (this.isInterface ? 
                                  INVOKEDYNAMICINTERFACE_OFFSET : 
                                  INVOKESPECIALSTATICVIRTUAL_OFFSET);
            
            //performs method resolution
            try {
                final String currentClassName = state.getCurrentMethodSignature().getClassName();
                this.methodSignatureResolved = hier.resolveMethod(currentClassName, this.data.signature(), this.isInterface);
            } catch (ClassFileNotFoundException e) {
                throwNew(state, NO_CLASS_DEFINITION_FOUND_ERROR);
                exitFromAlgorithm();
            } catch (IncompatibleClassFileException e) {
                throwNew(state, INCOMPATIBLE_CLASS_CHANGE_ERROR);
                exitFromAlgorithm();
            } catch (MethodAbstractException e) {
                throwNew(state, ABSTRACT_METHOD_ERROR);
                exitFromAlgorithm();
            } catch (MethodNotFoundException e) {
                throwNew(state, NO_SUCH_METHOD_ERROR);
                exitFromAlgorithm();
            } catch (MethodNotAccessibleException e) {
                throwNew(state, ILLEGAL_ACCESS_ERROR);
                exitFromAlgorithm();
            } catch (BadClassFileException e) {
                throwVerifyError(state);
                exitFromAlgorithm();
            } catch (ThreadStackEmptyException e) {
                //this should never happen
                failExecution(e);
            }
            
            //checks the resolved method; note that more checks 
            //are done later, by the last call to state.pushFrame
            //TODO this check is ok for invoke[interface/static/virtual], which checks should we do for invokespecial, if any?
            try {
                final ClassFile classFileResolved = hier.getClassFile(this.methodSignatureResolved.getClassName());
                if (classFileResolved.isMethodStatic(this.methodSignatureResolved) != this.isStatic) {
                    throwNew(state, INCOMPATIBLE_CLASS_CHANGE_ERROR);
                    exitFromAlgorithm();
                }
            } catch (BadClassFileException | MethodNotFoundException e) {
                //this should never happen after resolution
                failExecution(e);
            }
            
            //looks for the method implementation
            ClassFile classMethodImpl = null; //to keep the compiler happy
            try {
                final boolean isVirtualInterface = !this.isStatic && !this.isSpecial;
                final String receiverClassName;
                if (isVirtualInterface) {
                    final Reference thisRef = state.peekReceiverArg(this.methodSignatureResolved);
                    if (state.isNull(thisRef)) {
                        throwNew(state, NULL_POINTER_EXCEPTION);
                        exitFromAlgorithm();
                    }
                    receiverClassName = state.getObject(thisRef).getType();
                } else {
                    receiverClassName = null;
                }
                classMethodImpl = lookupMethodImpl(state, this.methodSignatureResolved, this.isStatic, this.isSpecial, receiverClassName);
                this.isNative = classMethodImpl.isMethodNative(this.methodSignatureResolved);
            } catch (MethodNotFoundException e) {
                //it is still possible that the method
                //has a meta-level implementation
                classMethodImpl = null;
                this.isNative = false;
            } catch (IncompatibleClassFileException e) {
                //TODO is it ok?
                throwNew(state, INCOMPATIBLE_CLASS_CHANGE_ERROR);
                exitFromAlgorithm();
            } catch (NullPointerException | BadClassFileException e) {
                throwVerifyError(state);
                exitFromAlgorithm();
            } catch (ThreadStackEmptyException e) {
                //this should never happen
                failExecution(e);
            }
            
            //builds a signature for the method implementation;
            //falls back to the signature of the resolved method
            //if there is no base-level implementation
            this.methodSignatureImpl = (classMethodImpl == null ? this.methodSignatureResolved : 
                new Signature(classMethodImpl.getClassName(), 
                              this.methodSignatureResolved.getDescriptor(), 
                              this.methodSignatureResolved.getName()));
            
            //looks for a meta-level implementation, and in case 
            //delegates the responsibility to the dispatcherMeta
            try {
                if (this.ctx.dispatcherMeta.isMeta(hier, this.methodSignatureImpl)) {
                    final Algorithm<?, ?, ?, ?, ?> algo = 
                        this.ctx.dispatcherMeta.select(this.methodSignatureImpl);
                    continueWith(algo);
                }
            } catch (BadClassFileException | MethodNotFoundException |
                     MetaUnsupportedException e) {
                //this should never happen after resolution 
                failExecution(e);
            }
            
            //if the method has no implementation, raises AbstractMethodError
            try {
                if (classMethodImpl == null || classMethodImpl.isMethodAbstract(this.methodSignatureImpl)) {
                    throwNew(state, ABSTRACT_METHOD_ERROR);
                    exitFromAlgorithm();
                }
            } catch (MethodNotFoundException e) {
                //this should never happen after resolution 
                failExecution(e);
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
            //possibly creates and initializes the class of the resolved method
            //TODO should we do it in the invoke[interface/special/virtual] cases? If so, isn't the same doing on methodSignatureImpl?
            if (this.isStatic) { 
                try {
                    ensureClassCreatedAndInitialized(state, this.methodSignatureResolved.getClassName(), this.ctx.decisionProcedure);
                } catch (BadClassFileException e) {
                    //this should never happen after resolution 
                    failExecution(e);
                }
            }
            
            //if the method is native, delegates the responsibility 
            //to the native invoker
            if (this.isNative) {
                try {
                    this.ctx.nativeInvoker.doInvokeNative(state, this.methodSignatureResolved, this.data.operands(), this.pcOffsetReturn);
                } catch (CannotInvokeNativeException e) {
                    failExecution(e);
                }
                exitFromAlgorithm();
            }

            //pushes the frame
            try {
                state.pushFrame(this.methodSignatureImpl, false, this.pcOffsetReturn, this.data.operands());
            } catch (InvalidProgramCounterException | InvalidSlotException e) {
                //TODO is it ok?
                throwVerifyError(state);
            } catch (NullMethodReceiverException | BadClassFileException | 
                     MethodNotFoundException | MethodCodeNotFoundException e) {
                //this should never happen
                failExecution(e);
            }
        };
    }
    
    @Override
    protected Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }
    
    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> 0; //nothing to add to the program counter of the pushed frame
    }
 }

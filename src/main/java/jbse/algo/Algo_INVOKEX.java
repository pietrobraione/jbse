package jbse.algo;

import static jbse.algo.Util.continueWith;
import static jbse.algo.Util.ensureClassInitialized;
import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.invokeClassLoaderLoadClass;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.offsetInvoke;
import static jbse.bc.Opcodes.OP_INVOKEHANDLE;
import static jbse.bc.Signatures.ABSTRACT_METHOD_ERROR;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.bc.Signatures.NO_SUCH_METHOD_ERROR;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.bc.Signatures.UNSUPPORTED_CLASS_VERSION_ERROR;

import java.util.function.Supplier;

import jbse.algo.exc.NotYetImplementedException;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.MethodAbstractException;
import jbse.bc.exc.MethodNotAccessibleException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.bc.exc.PleaseLoadClassException;
import jbse.bc.exc.WrongClassNameException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.tree.DecisionAlternative_NONE;

/**
 * Algorithm for the invoke* bytecodes
 * (invoke[interface/special/static/virtual]).
 *  
 * @author Pietro Braione
 */
final class Algo_INVOKEX extends Algo_INVOKEX_Abstract {
    private final Algo_INVOKEX_Completion algo_INVOKEX_Completion;

    public Algo_INVOKEX(boolean isInterface, boolean isSpecial, boolean isStatic) {
        super(isInterface, isSpecial, isStatic);
        this.algo_INVOKEX_Completion = new Algo_INVOKEX_Completion(isInterface, isSpecial, isStatic);
    }

    @Override
    protected final BytecodeCooker bytecodeCooker() {
        return (state) -> {
            //performs method resolution
            try {
                resolveMethod(state);
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
            } catch (MethodNotFoundException e) {
                throwNew(state, this.ctx.getCalculator(), NO_SUCH_METHOD_ERROR);
                exitFromAlgorithm();
            } catch (ClassFileNotAccessibleException | MethodNotAccessibleException e) {
                throwNew(state, this.ctx.getCalculator(), ILLEGAL_ACCESS_ERROR);
                exitFromAlgorithm();
            } catch (ClassFileIllFormedException e) {
                //TODO is it ok?
                throwVerifyError(state, this.ctx.getCalculator());
                exitFromAlgorithm();
            }

            //checks the resolved method; note that more checks 
            //are done later when the frame is pushed
            check(state);

            //creates and initializes the class of the resolved method in the invokestatic case
            if (this.isStatic && this.methodResolvedClass != null) { 
                try {
                    ensureClassInitialized(state, this.ctx, this.methodResolvedClass);
                } catch (HeapMemoryExhaustedException e) {
                    throwNew(state, this.ctx.getCalculator(), OUT_OF_MEMORY_ERROR);
                    exitFromAlgorithm();
                }
            }
            
            //looks for the method implementation with standard lookup
            try {
                findImpl(state);
            } catch (IncompatibleClassFileException e) {
                throwNew(state, this.ctx.getCalculator(), INCOMPATIBLE_CLASS_CHANGE_ERROR);
                exitFromAlgorithm();
            } catch (MethodNotAccessibleException e) {
                throwNew(state, this.ctx.getCalculator(), ILLEGAL_ACCESS_ERROR);
                exitFromAlgorithm();
            } catch (MethodAbstractException e) {
                throwNew(state, this.ctx.getCalculator(), ABSTRACT_METHOD_ERROR);
                exitFromAlgorithm();
            }

            //looks for a base-level or meta-level overriding implementation, 
            //and in case considers it instead
            findOverridingImpl(state);

            //if no implementation exists, reacts accordingly
            if (this.methodImplClass == null) {
                if (this.methodImplSignature == null) {
                    //standard lookup failed
                    throwNew(state, this.ctx.getCalculator(), NO_SUCH_METHOD_ERROR);
                    exitFromAlgorithm();
                } else {
                    //a classless method has not an implementation
                    throw new NotYetImplementedException("The classless method " + this.methodImplSignature.toString() + " has no implementation.");
                }
            }
            
            //otherwise, concludes the execution of the bytecode algorithm
            if (this.isMethodImplSignaturePolymorphic) {
                state.getCurrentFrame().patchCode(OP_INVOKEHANDLE);
                exitFromAlgorithm();
            } else {
                this.algo_INVOKEX_Completion.setImplementation(this.methodImplClass, this.methodImplSignature);
                this.algo_INVOKEX_Completion.setProgramCounterOffset(returnPcOffset());                
                continueWith(this.algo_INVOKEX_Completion);
            }
        };
    }
    
    /**
     * Override to change the default policy for calculating the PC
     * offset after returning from the invoked method.
     * 
     * @return an {@code int}, the PC offset.
     */
    protected int returnPcOffset() {
        return offsetInvoke(this.isInterface);
    }

    @Override
    protected final Class<DecisionAlternative_NONE> classDecisionAlternative() {
        return null; //never used
    }

    @Override
    protected final StrategyDecide<DecisionAlternative_NONE> decider() {
        return null; //never used
    }

    @Override
    protected final StrategyRefine<DecisionAlternative_NONE> refiner() {
        return null; //never used
    }

    @Override
    protected final StrategyUpdate<DecisionAlternative_NONE> updater() {
        return null; //never used
    }

    @Override
    protected final Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return null; //never used
    }

    @Override
    protected final Supplier<Integer> programCounterUpdate() {
        return null; //never used
    }
}

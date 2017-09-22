package jbse.algo;

import static jbse.algo.Util.continueWith;
import static jbse.algo.Util.ensureClassCreatedAndInitialized;
import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.ABSTRACT_METHOD_ERROR;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.bc.Signatures.NO_SUCH_METHOD_ERROR;

import java.util.function.Supplier;

import jbse.algo.exc.MetaUnsupportedException;
import jbse.bc.ClassHierarchy;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.MethodAbstractException;
import jbse.bc.exc.MethodNotAccessibleException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.dec.exc.InvalidInputException;
import jbse.tree.DecisionAlternative_NONE;

/**
 * Algorithm for the invoke* bytecodes
 * (invoke[interface/special/static/virtual]).
 * Should be followed by the execution of 
 * {@link Algo_INVOKEX_Completion}.
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
    protected BytecodeCooker bytecodeCooker() {
        return (state) -> {
            //performs method resolution
            try {
                resolveMethod(state);
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
            }

            //checks the resolved method; note that more checks 
            //are done later when the frame is pushed
            check(state);

            //possibly creates and initializes the class of the resolved method
            //TODO should we do it in the invoke[interface/special/virtual] cases? If so, isn't the same doing on methodSignatureImpl?
            if (this.isStatic) { 
                try {
                    ensureClassCreatedAndInitialized(state, this.methodSignatureResolved.getClassName(), this.ctx);
                } catch (InvalidInputException | BadClassFileException e) {
                    //this should never happen after resolution 
                    failExecution(e);
                }
            }

            //looks for the method implementation and determines
            //whether it is native
            try {
                findImplAndCalcNative(state);
            } catch (IncompatibleClassFileException e) {
                //TODO is it ok?
                throwNew(state, INCOMPATIBLE_CLASS_CHANGE_ERROR);
                exitFromAlgorithm();
            } catch (NullPointerException | BadClassFileException e) {
                throwVerifyError(state);
                exitFromAlgorithm();
            }

            //looks for a meta-level implementation, and in case 
            //delegates the responsibility to the dispatcherMeta
            final ClassHierarchy hier = state.getClassHierarchy();
            try {
                if (this.ctx.dispatcherMeta.isMeta(hier, this.methodSignatureImpl)) {
                    final Algo_INVOKEMETA<?, ?, ?, ?> algo = 
                        this.ctx.dispatcherMeta.select(this.methodSignatureImpl);
                    algo.setFeatures(this.isInterface, this.isSpecial, this.isStatic);
                    continueWith(algo);
                }
            } catch (BadClassFileException | MethodNotFoundException |
                     MetaUnsupportedException e) {
                //this should never happen after resolution 
                failExecution(e);
            }

            //otherwise, concludes the execution of the bytecode algorithm
            continueWith(this.algo_INVOKEX_Completion);
        };
    }

    @Override
    protected Class<DecisionAlternative_NONE> classDecisionAlternative() {
        return null; //never used
    }

    @Override
    protected StrategyDecide<DecisionAlternative_NONE> decider() {
        return null; //never used
    }

    @Override
    protected StrategyRefine<DecisionAlternative_NONE> refiner() {
        return null; //never used
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return null; //never used
    }

    @Override
    protected Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return null; //never used
    }

    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return null; //never used
    }
}

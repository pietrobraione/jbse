package jbse.algo;

import static jbse.algo.Util.continueWith;

import java.util.function.Supplier;

import jbse.algo.BytecodeData_1ZME.Kind;
import jbse.bc.Signature;
import jbse.mem.State;
import jbse.tree.DecisionAlternative;

/**
 * Abstract {@link Algorithm} implementing the effect of 
 * a method call.
 * 
 * @author Pietro Braione
 *
 */
public abstract class Algo_INVOKEMETA<
R extends DecisionAlternative, 
DE extends StrategyDecide<R>, 
RE extends StrategyRefine<R>, 
UP extends StrategyUpdate<R>> 
extends Algorithm<BytecodeData_1ZME, R, DE, RE, UP> {

    protected boolean isInterface; //set by setter (called by Algo_INVOKEX_Abstract)
    protected boolean isSpecial; //set by setter (called by Algo_INVOKEX_Abstract)
    protected boolean isStatic; //set by setter (called by Algo_INVOKEX_Abstract)

    public final void setFeatures(boolean isInterface, boolean isSpecial, boolean isStatic) {
        this.isInterface = isInterface;
        this.isSpecial = isSpecial;
        this.isStatic = isStatic;
    }

    @Override
    protected final Supplier<BytecodeData_1ZME> bytecodeData() {
        return () -> BytecodeData_1ZME.withInterfaceMethod(Kind.kind(this.isInterface, this.isSpecial, this.isStatic)).get();
    }

    /**
     * Cleanly interrupts the execution and schedules 
     * the base-level implementation of the method
     * for execution. 
     */
    protected final void continueWithBaseLevelImpl(State state) 
    throws InterruptException {
        final Algo_INVOKEX_Completion continuation = 
            new Algo_INVOKEX_Completion(this.isInterface, this.isSpecial, this.isStatic);
        continuation.shouldFindImplementation();
        continueWith(continuation);
    }
    
    protected final void continueWithAnotherMethod(State state, Signature methodSignature, boolean isInterface, boolean isSpecial, boolean isStatic)
    throws InterruptException {
        final Algo_INVOKEX_NoData<?> continuation =
        new Algo_INVOKEX_NoData<BytecodeData>(isInterface, isSpecial, isStatic) {
            @Override
            protected Supplier<BytecodeData> bytecodeData() {
                return () -> new BytecodeData() {
                    @Override
                    protected void readImmediates(State state) {
                        setMethodSignature(methodSignature);
                    }
                };
            }
        };
        continueWith(continuation);
    }
}

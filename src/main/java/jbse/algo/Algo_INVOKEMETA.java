package jbse.algo;

import static jbse.algo.Util.continueWith;

import java.util.function.Supplier;

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

    protected boolean isInterface; //set by setter (called by Algo_INVOKEX)
    protected boolean isSpecial; //set by setter (called by Algo_INVOKEX)
    protected boolean isStatic; //set by setter (called by Algo_INVOKEX)

    public final void setFeatures(boolean isInterface, boolean isSpecial, boolean isStatic) {
        this.isInterface = isInterface;
        this.isSpecial = isSpecial;
        this.isStatic = isStatic;
    }

    @Override
    protected final Supplier<BytecodeData_1ZME> bytecodeData() {
        return () -> BytecodeData_1ZME.withInterfaceMethod(this.isInterface).get();
    }

    /**
     * Cleanly interrupts the execution and schedules 
     * the base-level implementation of the method
     * for execution. 
     */
    protected final void continueWithBaseLevelImpl() 
    throws InterruptException {
        final Algo_INVOKEX_Completion continuation = 
        new Algo_INVOKEX_Completion(this.isInterface, this.isSpecial, this.isStatic);
        continueWith(continuation);
    }    
}

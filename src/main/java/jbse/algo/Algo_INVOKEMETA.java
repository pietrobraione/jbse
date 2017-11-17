package jbse.algo;

import static jbse.algo.Util.continueWith;
import static jbse.bc.Offsets.invokeDefaultOffset;

import java.util.function.Supplier;

import jbse.algo.BytecodeData_1KME.Kind;
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
extends Algorithm<BytecodeData_1KME, R, DE, RE, UP> {

    protected boolean isInterface; //set by setter (called by Algo_INVOKEX_Abstract)
    protected boolean isSpecial; //set by setter (called by Algo_INVOKEX_Abstract)
    protected boolean isStatic; //set by setter (called by Algo_INVOKEX_Abstract)

    public final void setFeatures(boolean isInterface, boolean isSpecial, boolean isStatic) {
        this.isInterface = isInterface;
        this.isSpecial = isSpecial;
        this.isStatic = isStatic;
    }

    @Override
    protected final Supplier<BytecodeData_1KME> bytecodeData() {
        return () -> BytecodeData_1KME.withMethod(Kind.kind(this.isInterface, this.isSpecial, this.isStatic)).get();
    }

    /**
     * Cleanly interrupts the execution and schedules 
     * the base-level implementation of the method
     * for execution. 
     */
    protected final void continueWithBaseLevelImpl(State state) 
    throws InterruptException {
        final Algo_INVOKEX_CompletionNonSignaturePolymorphic<BytecodeData_1KME> continuation = 
            new Algo_INVOKEX_CompletionNonSignaturePolymorphic<BytecodeData_1KME>(this.isInterface, this.isSpecial, this.isStatic, bytecodeData());
        continuation.setPcOffset(invokeDefaultOffset(this.isInterface));
        continuation.shouldFindImplementation();
        continueWith(continuation);
    }    
}

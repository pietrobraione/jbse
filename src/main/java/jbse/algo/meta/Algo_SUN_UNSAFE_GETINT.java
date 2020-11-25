package jbse.algo.meta;

import static jbse.common.Util.unsafe;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.mem.State;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Simplex;

/**
 * Meta-level implementation of {@link sun.misc.Unsafe#getInt(long)}.
 */
public final class Algo_SUN_UNSAFE_GETINT extends Algo_INVOKEMETA_Nonbranching {
    private int value; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }
    
    @Override
    protected void cookMore(State state) throws SymbolicValueNotAllowedException {
        if (!(this.data.operand(1) instanceof Simplex)) {
            throw new SymbolicValueNotAllowedException("sun.misc.Unsafe.getInt cannot be invoked with a symbolic argument");
        }
        final long memoryAddress = ((Long) ((Simplex) this.data.operand(1)).getActualValue()).longValue();
        this.value = unsafe().getInt(memoryAddress);
    }
    
    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.pushOperand(this.ctx.getCalculator().valInt(this.value));
        };
    }
}

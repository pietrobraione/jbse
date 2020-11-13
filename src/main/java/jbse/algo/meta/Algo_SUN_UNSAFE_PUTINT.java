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
 * Meta-level implementation of {@link sun.misc.Unsafe#putInt(long, int)}.
 */
public final class Algo_SUN_UNSAFE_PUTINT extends Algo_INVOKEMETA_Nonbranching {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 3;
    }
    
    @Override
    protected void cookMore(State state) throws SymbolicValueNotAllowedException {
        if (!(this.data.operand(1) instanceof Simplex) || !(this.data.operand(2) instanceof Simplex)) {
            throw new SymbolicValueNotAllowedException("sun.misc.Unsafe.putInt cannot be invoked with a symbolic argument");
        }
        final long memoryAddress = ((Long) ((Simplex) this.data.operand(1)).getActualValue()).longValue();
        final int value = ((Integer) ((Simplex) this.data.operand(2)).getActualValue()).intValue();
        unsafe().putInt(memoryAddress, value);
    }
    
    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            //nothing to do
        };
    }
}

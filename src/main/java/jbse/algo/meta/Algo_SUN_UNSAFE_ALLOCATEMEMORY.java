package jbse.algo.meta;

import static jbse.algo.Util.unsafe;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.mem.State;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Simplex;

/**
 * Meta-level implementation of {@link sun.misc.Unsafe#allocateMemory(long)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_SUN_UNSAFE_ALLOCATEMEMORY extends Algo_INVOKEMETA_Nonbranching {
    private long memoryAddress;
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }
    
    @Override
    protected void cookMore(State state) throws SymbolicValueNotAllowedException {
        if (!(this.data.operand(1) instanceof Simplex)) {
            throw new SymbolicValueNotAllowedException("sun.misc.Unsafe.allocateMemory cannot be invoked with a symbolic argument");
        }
        final long bytes = ((Long) ((Simplex) this.data.operand(1)).getActualValue()).longValue();
        this.memoryAddress = unsafe().allocateMemory(bytes);
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.pushOperand(state.getCalculator().valLong(this.memoryAddress));
        };
    }
}

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
 * Meta-level implementation of {@link sun.misc.Unsafe#allocateMemory(long)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_SUN_UNSAFE_ALLOCATEMEMORY extends Algo_INVOKEMETA_Nonbranching {
    private long bytes; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }
    
    @Override
    protected void cookMore(State state) throws SymbolicValueNotAllowedException {
        if (!(this.data.operand(1) instanceof Simplex)) {
            throw new SymbolicValueNotAllowedException("Method sun.misc.Unsafe.allocateMemory cannot be invoked with a symbolic long size argument");
        }
        this.bytes = ((Long) ((Simplex) this.data.operand(1)).getActualValue()).longValue();
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            final long memoryAddress = unsafe().allocateMemory(this.bytes);
            state.addMemoryBlock(memoryAddress, this.bytes);
            state.pushOperand(this.ctx.getCalculator().valLong(memoryAddress));
        };
    }
}

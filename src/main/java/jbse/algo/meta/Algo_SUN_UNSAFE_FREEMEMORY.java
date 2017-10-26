package jbse.algo.meta;

import static jbse.algo.Util.unsafe;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Simplex;

/**
 * Meta-level implementation of {@link sun.misc.Unsafe#freeMemory(long)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_SUN_UNSAFE_FREEMEMORY extends Algo_INVOKEMETA_Nonbranching {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }
    
    @Override
    protected void cookMore(State state) throws SymbolicValueNotAllowedException {
        if (!(this.data.operand(1) instanceof Simplex)) {
            throw new SymbolicValueNotAllowedException("sun.misc.Unsafe.freeMemory cannot be invoked with a symbolic argument");
        }
        final long memoryAddress = ((Long) ((Simplex) this.data.operand(1)).getActualValue()).longValue();
        unsafe().freeMemory(memoryAddress);
    }

    @Override
    protected void update(State state) throws ThreadStackEmptyException {
        //nothing to do
    }
}

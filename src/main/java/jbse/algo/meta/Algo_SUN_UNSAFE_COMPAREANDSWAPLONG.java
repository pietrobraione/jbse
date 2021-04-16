package jbse.algo.meta;

/**
 * Meta-level implementation of {@link sun.misc.Unsafe#compareAndSwapLong(Object, long, long, long)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_SUN_UNSAFE_COMPAREANDSWAPLONG extends Algo_SUN_UNSAFE_COMPAREANDSWAPNUMERIC {
    public Algo_SUN_UNSAFE_COMPAREANDSWAPLONG() {
        super("Long");
    }
}

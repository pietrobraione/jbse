package jbse.algo.meta;

/**
 * Meta-level implementation of {@link sun.misc.Unsafe#compareAndSwapInt(Object, long, int, int)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_SUN_UNSAFE_COMPAREANDSWAPINT extends Algo_SUN_UNSAFE_COMPAREANDSWAPNUMERIC {
    public Algo_SUN_UNSAFE_COMPAREANDSWAPINT() {
        super("Int");
    }
}

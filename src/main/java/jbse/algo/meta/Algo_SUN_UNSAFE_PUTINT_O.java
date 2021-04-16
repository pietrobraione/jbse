package jbse.algo.meta;

/**
 * Meta-level implementation of {@link sun.misc.Unsafe#putInt(Object, long, int)} and 
 * {@link sun.misc.Unsafe#putIntVolatile(Object, long, int)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_SUN_UNSAFE_PUTINT_O extends Algo_SUN_UNSAFE_PUTX_O {
	public Algo_SUN_UNSAFE_PUTINT_O() {
		super(new Algo_SUN_UNSAFE_PUTINT_O_Array(), "putInt");
	}
}

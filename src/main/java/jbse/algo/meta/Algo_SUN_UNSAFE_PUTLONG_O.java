package jbse.algo.meta;

/**
 * Meta-level implementation of {@link sun.misc.Unsafe#putLong(Object, long, long)} and 
 * {@link sun.misc.Unsafe#putLongVolatile(Object, long, long)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_SUN_UNSAFE_PUTLONG_O extends Algo_SUN_UNSAFE_PUTX_O {
	public Algo_SUN_UNSAFE_PUTLONG_O() {
		super(new Algo_SUN_UNSAFE_PUTLONG_O_Array(), "putLong");
	}
}

package jbse.algo.meta;

/**
 * Meta-level implementation of {@link sun.misc.Unsafe#putObject(Object, long, Object)}, 
 * {@link sun.misc.Unsafe#putObjectVolatile(Object, long, Object)} and 
 * {@link sun.misc.Unsafe#putOrderedObject(Object, long, Object)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_SUN_UNSAFE_PUTOBJECT_O extends Algo_SUN_UNSAFE_PUTX_O {
	public Algo_SUN_UNSAFE_PUTOBJECT_O() {
		super(new Algo_SUN_UNSAFE_PUTOBJECT_O_Array(), "put[Ordered]Object");
	}
}

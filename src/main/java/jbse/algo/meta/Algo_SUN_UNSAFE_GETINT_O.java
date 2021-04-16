package jbse.algo.meta;

import static jbse.common.Type.INT;

/**
 * Meta-level implementation of {@link sun.misc.Unsafe#getInt(Object, long)} and 
 * {@link sun.misc.Unsafe#getIntVolatile(Object, long)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_SUN_UNSAFE_GETINT_O extends Algo_SUN_UNSAFE_GETX_O {
	public Algo_SUN_UNSAFE_GETINT_O() {
		super(new Algo_SUN_UNSAFE_GETINT_O_Array(), "getInt", "n int");
	}
	
	@Override
	protected boolean valueTypeCorrect(char valueType) {
		return valueType == INT;
	}
}

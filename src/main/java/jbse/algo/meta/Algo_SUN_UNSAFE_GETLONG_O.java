package jbse.algo.meta;

import static jbse.common.Type.LONG;

/**
 * Meta-level implementation of {@link sun.misc.Unsafe#getLong(Object, long)}
 * and {@link sun.misc.Unsafe#getLongVolatile(Object, long)}.
 */
public final class Algo_SUN_UNSAFE_GETLONG_O extends Algo_SUN_UNSAFE_GETX_O {
	public Algo_SUN_UNSAFE_GETLONG_O() {
		super(new Algo_SUN_UNSAFE_GETLONG_O_Array(), "getLong", " long");
	}
	
	@Override
	protected boolean valueTypeCorrect(char valueType) {
		return valueType == LONG;
	}
}


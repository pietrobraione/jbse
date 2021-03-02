package jbse.algo.meta;

import static jbse.common.Type.NULLREF;
import static jbse.common.Type.REFERENCE;

/**
 * Meta-level implementation of {@link sun.misc.Unsafe#getObject(Object, long)} and
 * {@link sun.misc.Unsafe#getObjectVolatile(Object, long)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_SUN_UNSAFE_GETOBJECT_O extends Algo_SUN_UNSAFE_GETX_O {
	public Algo_SUN_UNSAFE_GETOBJECT_O() {
		super(new Algo_SUN_UNSAFE_GETOBJECT_O_Array(), "getObject", " reference or null");
	}
	
	@Override
	protected boolean valueTypeCorrect(char valueType) {
		return valueType == REFERENCE || valueType == NULLREF;
	}
}

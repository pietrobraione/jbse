package jbse.algo.meta;

import jbse.bc.ClassFile;

/**
 * Meta-level implementation of {@link sun.misc.Unsafe#getLong(Object, long)}
 * and {@link sun.misc.Unsafe#getLongVolatile(Object, long)} 
 * in the case the object to read into is an array.
 */
public final class Algo_SUN_UNSAFE_GETLONG_O_Array extends Algo_SUN_UNSAFE_GETX_O_Array {
	public Algo_SUN_UNSAFE_GETLONG_O_Array() {
		super("getLong", "long");
	}
	
	@Override
	protected boolean arrayMemberTypeCorrect(ClassFile arrayMemberType) {
		return "long".equals(arrayMemberType.getClassName());
	}
}


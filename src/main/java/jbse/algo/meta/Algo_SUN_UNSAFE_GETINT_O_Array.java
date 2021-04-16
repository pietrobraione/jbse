package jbse.algo.meta;

import jbse.bc.ClassFile;

/**
 * Meta-level implementation of {@link sun.misc.Unsafe#getInt(Object, long)} and 
 * {@link sun.misc.Unsafe#getIntVolatile(Object, long)} in the case the object 
 * to read into is an array.
 * 
 * @author Pietro Braione
 */
public final class Algo_SUN_UNSAFE_GETINT_O_Array extends Algo_SUN_UNSAFE_GETX_O_Array {
	public Algo_SUN_UNSAFE_GETINT_O_Array() {
		super("getInt", "int");
	}
	
	@Override
	protected boolean arrayMemberTypeCorrect(ClassFile arrayMemberType) {
		return "int".equals(arrayMemberType.getClassName());
	}
}

package jbse.algo.meta;

import jbse.algo.meta.exc.UndefinedResultException;
import jbse.bc.ClassFile;
import jbse.mem.State;
import jbse.val.Value;

/**
 * Meta-level implementation of {@link sun.misc.Unsafe#putInt(Object, long, int)} and 
 * {@link sun.misc.Unsafe#putIntVolatile(Object, long, int)} 
 * in the case the object to write into is an array.
 * 
 * @author Pietro Braione
 */
public final class Algo_SUN_UNSAFE_PUTINT_O_Array extends Algo_SUN_UNSAFE_PUTX_O_Array {
	public Algo_SUN_UNSAFE_PUTINT_O_Array() {
		super("putInt");
	}
	
	@Override
	protected void arrayMemberTypeCheck(ClassFile arrayMemberType, State state, Value valueToStore) 
	throws UndefinedResultException {
		if (!"int".equals(arrayMemberType.getClassName())) {
			throw new UndefinedResultException("The Object o parameter to sun.misc.Unsafe.putInt[Volatile] was an array whose member type is not int");
		}
	}
}

package jbse.algo.meta;

import jbse.algo.meta.exc.UndefinedResultException;
import jbse.bc.ClassFile;
import jbse.mem.State;
import jbse.val.Value;

/**
 * Meta-level implementation of {@link sun.misc.Unsafe#putLong(Object, long, long)} and 
 * {@link sun.misc.Unsafe#putLongVolatile(Object, long, long)} 
 * in the case the object to write into is an array.
 * 
 * @author Pietro Braione
 */
public final class Algo_SUN_UNSAFE_PUTLONG_O_Array extends Algo_SUN_UNSAFE_PUTX_O_Array {
	public Algo_SUN_UNSAFE_PUTLONG_O_Array() {
		super("putLong");
	}
	
	@Override
	protected void arrayMemberTypeCheck(ClassFile arrayMemberType, State state, Value valueToStore) 
	throws UndefinedResultException {
		if (!"long".equals(arrayMemberType.getClassName())) {
			throw new UndefinedResultException("The Object o parameter to sun.misc.Unsafe.putLong[Volatile] was an array whose member type is not long");
		}
	}
}

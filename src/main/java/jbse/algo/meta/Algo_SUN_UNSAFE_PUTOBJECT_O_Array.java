package jbse.algo.meta;

import jbse.algo.meta.exc.UndefinedResultException;
import jbse.bc.ClassFile;
import jbse.bc.ClassHierarchy;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.val.Reference;
import jbse.val.Value;

/**
 * Meta-level implementation of {@link sun.misc.Unsafe#putObject(Object, long, Object)}, 
 * {@link sun.misc.Unsafe#putObjectVolatile(Object, long, Object)} and 
 * {@link sun.misc.Unsafe#putOrderedObject(Object, long, Object)}
 * in the case the object to write to is an array.
 * 
 * @author Pietro Braione
 */
public final class Algo_SUN_UNSAFE_PUTOBJECT_O_Array extends Algo_SUN_UNSAFE_PUTX_O_Array {
	public Algo_SUN_UNSAFE_PUTOBJECT_O_Array() {
		super("put[Ordered]Object");
	}
	
	@Override
	protected void arrayMemberTypeCheck(ClassFile arrayMemberType, State state, Value valueToStore) 
	throws UndefinedResultException, FrozenStateException {
        if (arrayMemberType.isReference() || arrayMemberType.isArray()) {
            final Reference valueToStoreRef = (Reference) valueToStore;
            final Objekt o = state.getObject(valueToStoreRef);
            final ClassHierarchy hier = state.getClassHierarchy();
            if (!state.isNull(valueToStoreRef) &&
                !hier.isAssignmentCompatible(o.getType(), arrayMemberType)) {
                throw new UndefinedResultException("The Object x parameter to sun.misc.Unsafe.put[Ordered]Object[Volatile] was not assignment-compatible with the Object o (array) parameter");
            }
        } else {
            throw new UndefinedResultException("The Object o parameter to sun.misc.Unsafe.put[Ordered]Object[Volatile] was an array whose member type is not a reference");
        }
	}
}

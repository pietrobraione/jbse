package jbse.mem;

import java.util.ArrayList;

import static jbse.common.Type.isPrimitive;
import static jbse.common.Type.isReference;

import jbse.val.Null;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.ReferenceArrayImmaterial;
import jbse.val.ReferenceConcrete;
import jbse.val.ReferenceSymbolic;
import jbse.val.Value;
import jbse.val.exc.InvalidTypeException;

/**
 * Some utility functions and constants.
 * 
 * @author Pietro Braione
 */
public class Util {
	/** The conventional heap position for the root object. */
	public static final long POS_ROOT = 0;

	/** The conventional heap position for null. */
	public static final long POS_NULL = -99;

	/** The conventional heap position for an unknown position. */
	public static final long POS_UNKNOWN = -999;
	
	/**
	 * Checks whether a {@link Value} is a symbolic {@link Reference}.
	 * 
	 * @param v a {@link Value}. It must not be {@code null}.
	 * @return {@code true} iff {@code v} is a symbolic {@link Reference}.
	 */
	public static boolean isSymbolicReference(Value v) {
		return 
		(isReference(v.getType()) && 
        ((Reference) v).isSymbolic());
	}
	
	/**
	 * Checks whether a {@link Value} is a resolved symbolic 
	 * {@link Reference}.
	 * 
	 * @param s a {@link State}. It must not be {@code null}.
	 * @param v a {@link Value}. It must not be {@code null}.
	 * @return {@code true} iff {@code v} is a symbolic 
	 * {@link Reference} resolved in {@code s}.
	 */
	public static boolean isResolvedSymbolicReference(State s, Value v) {
		return 
		isSymbolicReference(v) &&
        s.resolved((ReferenceSymbolic) v);
	}
	
	/**
	 * Checks whether a {@link Reference} is null. 
	 * 
	 * @param s a {@link State}. It must not be {@code null}.
	 * @param r a {@link Reference}. It must not be {@code null}.
	 * @return {@code true} iff {@code r} is a symbolic 
	 * {@link Reference} resolved to null in {@code s}, or the 
	 * {@link Null} concrete reference.
	 */
	public static boolean isNull(State s, Reference r) {
		return (r.isSymbolic() && s.getResolution((ReferenceSymbolic) r) == POS_NULL) ||
			   (!r.isSymbolic() && ((ReferenceConcrete) r).isNull());
	}
	
	/**
	 * Checks whether two {@link Reference}s are alias. 
	 * 
	 * @param s a {@link State}. It must not be {@code null}.
	 * @param r1 a {@link Reference}. It must not be {@code null}.
	 * @param r2 a {@link Reference}. It must not be {@code null}.
	 * @return {@code true} if {@code r1} and {@code r2} denote 
	 *         the same heap position or both denote {@code null}, 
	 *         {@code false} if they do not, 
	 *         or if at least one is an unresolved symbolic reference.
	 */
	public static boolean areAlias(State s, Reference r1, Reference r2) {
		final long r1Pos = heapPosition(s, r1);
		final long r2Pos = heapPosition(s, r2);
		if (r1Pos == POS_UNKNOWN || r2Pos == POS_UNKNOWN) {
			return false;
		} else {
			return (r1Pos == r2Pos);
		}
	}
	
	public static long heapPosition(State s, Reference r) {
		if (isResolved(s, r)) {
	        return (r.isSymbolic() ? s.getResolution((ReferenceSymbolic) r) : ((ReferenceConcrete) r).getHeapPosition());
		}
        return POS_UNKNOWN;
	}
	
	/**
	 * Checks whether a {@link Value} is resolved. 
	 * 
	 * @param s a {@link State}. It must not be {@code null}.
	 * @param v a {@link Value}. It must not be {@code null}.
	 * @return {@code true} iff {@code v} is resolved, i.e., 
	 * either is a {@link Primitive} (symbolic or not), or a 
	 * concrete {@link Reference}, or a symbolic 
	 * {@link Reference} resolved in {@code s}.
	 */
	public static boolean isResolved(State s, Value v) {
		return
		isPrimitive(v.getType()) ||
		v instanceof ReferenceConcrete ||
        v instanceof ReferenceArrayImmaterial ||
		isResolvedSymbolicReference(s, v);
	}
	
	/**
	 * Casts an array of {@link Value}s into an array of 
	 * {@link Primitive}s.
	 * 
	 * @param args an array of {@link Value}s.
	 * @return the array of the elements in {@code args} cast to
	 *         {@link Primitive}, in same order.
	 */
	private static final Primitive[] ARRAY_OF_PRIMITIVES = new Primitive[] { }; //foo
	public static Primitive[] toPrimitive(Value[] args) throws InvalidTypeException {
		final ArrayList<Primitive> retVal = new ArrayList<Primitive>();
		for (Value arg : args) {
			if (arg instanceof Primitive) {
				retVal.add((Primitive) arg);
			} else {
				throw new InvalidTypeException("not all the arguments are primitive");
			}
		}
		return retVal.toArray(ARRAY_OF_PRIMITIVES);
	}
	
	/**
	 * Do not instantiate it!
	 */
	private Util() { }
}

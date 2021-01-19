package jbse.mem;

import static jbse.common.Type.isPrimitive;
import static jbse.common.Type.isReference;

import java.util.List;

import jbse.val.KlassPseudoReference;
import jbse.val.Null;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.ReferenceArrayImmaterial;
import jbse.val.ReferenceConcrete;
import jbse.val.ReferenceSymbolic;
import jbse.val.Value;

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
	 * Checks whether a {@link Value} is a resolved symbolic 
	 * {@link Reference}.
	 * 
	 * @param s a {@link State}. It must not be {@code null}.
	 * @param v a {@link Value}. It must not be {@code null}.
	 * @return {@code true} iff {@code v} is a symbolic 
	 * {@link Reference} resolved in {@code s}.
	 */
	public static boolean isResolvedSymbolicReference(List<Clause> l, Value v) {
		if (!isSymbolicReference(v)) {
			return false;
		}
        for (Clause c : l) {
        	if (c instanceof ClauseAssumeReferenceSymbolic) {
        		final ClauseAssumeReferenceSymbolic cr = (ClauseAssumeReferenceSymbolic) c;
        		if (cr.getReference().equals(v)) {
        			return true;
        		}
        	}
        }
        return false;
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
	 * Checks whether two {@link Reference}s are surely alias. 
	 * 
	 * @param s a {@link State}. It must not be {@code null}.
	 * @param r1 a {@link Reference}. It must not be {@code null}.
	 * @param r2 a {@link Reference}. It must not be {@code null}.
	 * @return {@code true} if {@code r1} and {@code r2} surely denote 
	 *         the same heap position (i.e. either {@code r1 == r2}
	 *         or they are both resolved to the same heap position), 
	 *         {@code false} otherwise.
	 */
	public static boolean areAlias(State s, Reference r1, Reference r2) {
		final long r1Pos = heapPosition(s, r1);
		final long r2Pos = heapPosition(s, r2);
		if (r1Pos == POS_UNKNOWN || r2Pos == POS_UNKNOWN) {
			return (r1 == r2);
		} else {
			return (r1Pos == r2Pos);
		}
	}
	
	/**
	 * Checks whether two {@link Reference}s are surely not alias. 
	 * 
	 * @param s a {@link State}. It must not be {@code null}.
	 * @param r1 a {@link Reference}. It must not be {@code null}.
	 * @param r2 a {@link Reference}. It must not be {@code null}.
	 * @return {@code true} if {@code r1} and {@code r2} surely denote 
	 *         different heap position (i.e. they are both resolved 
	 *         to different heap position), {@code false} otherwise.
	 */
	public static boolean areNotAlias(State s, Reference r1, Reference r2) {
		final long r1Pos = heapPosition(s, r1);
		final long r2Pos = heapPosition(s, r2);
		if (r1Pos == POS_UNKNOWN || r2Pos == POS_UNKNOWN) {
			return false;
		} else {
			return (r1Pos != r2Pos);
		}
	}
	
	/**
	 * Returns the position in the heap a {@link Reference} points to
	 * 
	 * @param s a {@link State}.
	 * @param r a {@link Reference}.
	 * @return a {@code long}, the position in the heap of {@code s} to
	 *         which {@code r} points, or {@link #POS_UNKNOWN} if
	 *         {@code r} is not resolved.
	 */
	public static long heapPosition(State s, Reference r) {
		if (isResolved(s.getPathCondition(), r)) {
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
        v instanceof KlassPseudoReference ||
		isResolvedSymbolicReference(s, v);
	}
	
	/**
	 * Checks whether a {@link Value} is resolved. 
	 * 
	 * @param l a {@link List}{@code <}{@link Clause}{@code >}. 
	 *        It must not be {@code null}.
	 * @param v a {@link Value}. It must not be {@code null}.
	 * @return {@code true} iff {@code v} is resolved, i.e., 
	 * either is a {@link Primitive} (symbolic or not), or a 
	 * concrete {@link Reference}, or a symbolic 
	 * {@link Reference} resolved by some {@link Clause} in {@code l}.
	 */
	public static boolean isResolved(List<Clause> l, Value v) {
		return
		isPrimitive(v.getType()) ||
		v instanceof ReferenceConcrete ||
        v instanceof ReferenceArrayImmaterial ||
        v instanceof KlassPseudoReference ||
		isResolvedSymbolicReference(l, v);
	}
	
	/**
	 * Do not instantiate it!
	 */
	private Util() { }
}

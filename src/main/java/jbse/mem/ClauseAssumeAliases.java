package jbse.mem;

import jbse.common.exc.InvalidInputException;
import jbse.val.ReferenceSymbolic;

/**
 * A path condition {@link Clause}, an assumption 
 * that some {@link ReferenceSymbolic} is resolved by
 * aliasing.
 * 
 * @author Pietro Braione
 *
 */
public final class ClauseAssumeAliases extends ClauseAssumeReferenceSymbolic {
	private final long heapPosition;
	private final HeapObjekt object;
	
	/**
	 * Constructor.
	 * 
	 * @param referenceSymbolic a {@link ReferenceSymbolic}. It must not be {@code null}.
	 * @param heapPosition a {@code long}, the heap position. It must be 
	 *        the position of an object assumed by a previous expansion. 
	 * @param object the {@link HeapObjekt} at position {@code heapPosition}, 
	 *        as it was at the beginning of symbolic execution (equivalently, 
	 *        as it was when it was assumed).
	 * @throws InvalidInputException if {@code referenceSymbolic == null || object == null}. 
	 */
	public ClauseAssumeAliases(ReferenceSymbolic referenceSymbolic, long heapPosition, HeapObjekt object) throws InvalidInputException { 
		super(referenceSymbolic);
		this.heapPosition = heapPosition;
		this.object = object;
	}
	
	/**
	 * Returns the heap position.
	 * 
	 * @return a {@code long}.
	 */
	public long getHeapPosition() {
		return this.heapPosition;
	}
	
	/**
	 * Returns the aliased {@link HeapObjekt}.
	 * 
	 * @return a {@link HeapObjekt}.
	 */
	public HeapObjekt getObjekt() {
		return this.object;
	}
	
	@Override
	public void accept(ClauseVisitor v) throws Exception {
		v.visitClauseAssumeAliases(this);
	}

	@Override
	public String toString() {
		final ReferenceSymbolic r = this.getReference();
		return r.toString() + " == " + "Object[" + this.heapPosition + "] (aliases " + this.object.getOrigin().toString() + ")";
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 61 * result + (int) this.heapPosition;
		result = 907 * result + this.object.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		return (this.heapPosition == ((ClauseAssumeAliases) obj).heapPosition);
	}

	@Override
	public ClauseAssumeAliases clone() {
		return (ClauseAssumeAliases) super.clone();
	}
}

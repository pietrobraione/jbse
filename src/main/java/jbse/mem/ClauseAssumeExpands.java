package jbse.mem;

import jbse.common.exc.InvalidInputException;
import jbse.val.ReferenceSymbolic;

/**
 * A path condition {@link Clause}, an assumption 
 * that some {@link ReferenceSymbolic} is resolved by
 * expansion.
 * 
 * @author Pietro Braione
 *
 */
public final class ClauseAssumeExpands extends ClauseAssumeReferenceSymbolic {
	private final long heapPosition;
	private final HeapObjekt object;
	
	/**
	 * Constructor.
	 * 
	 * @param referenceSymbolic a {@link ReferenceSymbolic}. 
	 *        It must not be {@code null}.
	 * @param heapPosition a {@code long}, a heap position.
	 * @param object the {@link HeapObjekt} at position {@code heapPosition}, 
	 *        as it was at the beginning of symbolic execution (equivalently, 
	 *        as it was when it was assumed).
	 * @throws InvalidInputException if {@code referenceSymbolic == null} or 
	 *         {@code heapPosition < }{@link Util#POS_ROOT} or {@code object == null}. 
	 */
	public ClauseAssumeExpands(ReferenceSymbolic referenceSymbolic, long heapPosition, HeapObjekt object) throws InvalidInputException { 
		super(referenceSymbolic);
		if (heapPosition < Util.POS_ROOT) {
			throw new InvalidInputException("Tried to build a ClauseAssumeExpands with invalid heapPosition parameter.");
		}
		if (object == null) {
			throw new InvalidInputException("Tried to build a ClauseAssumeExpands with null object parameter.");
		}
		this.heapPosition = heapPosition;
		this.object = object.clone(); //safety copy
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
	 * Returns the expansion {@link HeapObjekt}.
	 * 
	 * @return a {@link HeapObjekt}.
	 */
	public HeapObjekt getObjekt() { 
		return this.object.clone(); //preserves the safety copy 
	}
	
	@Override
	public void accept(ClauseVisitor v) throws Exception {
		v.visitClauseAssumeExpands(this);
	}

	@Override
	public String toString() {
		final ReferenceSymbolic r = this.getReference();
		return r.toString() + " == " + "Object[" + this.heapPosition + "] (fresh " + this.object.getType().getClassName() + ")";
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 11 * result + this.object.getType().hashCode();
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
		final ClauseAssumeExpands other = (ClauseAssumeExpands) obj;
		if (!this.object.getType().equals(other.object.getType())) {
			return false;
		}
		return true;
	}

	@Override
	public ClauseAssumeExpands clone() {
		return (ClauseAssumeExpands) super.clone();
	}
}

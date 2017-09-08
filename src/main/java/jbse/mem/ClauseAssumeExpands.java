package jbse.mem;

import jbse.val.ReferenceSymbolic;

/**
 * A path condition {@link Clause}, an assumption 
 * that some {@link ReferenceSymbolic} is resolved by
 * expansion.
 * 
 * @author Pietro Braione
 *
 */
public class ClauseAssumeExpands extends ClauseAssumeReferenceSymbolic {
	private final long heapPosition;
	private final Objekt object;
	
	/**
	 * Constructor.
	 * 
	 * @param r a {@link ReferenceSymbolic}. It must be resolved to a fresh object.
	 * @param heapPosition an {@code int}, the heap position. It must be 
	 *        the position of a fresh (not yet aliased) expansion object. 
	 * @param object the expansion {@link Objekt}.
	 */
	public ClauseAssumeExpands(ReferenceSymbolic r, long heapPosition, Objekt object) { 
		super(r);
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

	public Objekt getObjekt() { 
		return this.object.clone(); //preserves the safety copy 
	}
	
	@Override
	public void accept(ClauseVisitor v) throws Exception {
		v.visitClauseAssumeExpands(this);
	}

	@Override
	public String toString() {
		final ReferenceSymbolic r = this.getReference();
		return r.toString() + " == " + "Object[" + this.heapPosition + "] (fresh)";
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 11 * result + this.object.hashCode();
		result = 23 * result + (int) this.heapPosition;
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
		if (object == null) {
			if (other.object != null) {
				return false;
			}
		} else if (!object.equals(other.object)) {
			return false;
		}
		return true;
	}

	@Override
	public ClauseAssumeExpands clone() {
		return (ClauseAssumeExpands) super.clone();
	}
}

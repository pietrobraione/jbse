package jbse.mem;

import jbse.val.ReferenceSymbolic;

/**
 * A path condition {@link Clause}, an assumption 
 * on a {@link ReferenceSymbolic}.
 * 
 * @author Pietro Braione
 *
 */
public abstract class ClauseAssumeReferenceSymbolic implements Clause {
	private final ReferenceSymbolic r;

	/**
	 * Constructor.
	 * 
	 * @param r a {@link ReferenceSymbolic}.
	 */
	protected ClauseAssumeReferenceSymbolic(ReferenceSymbolic r) { this.r = r; }

	/**
	 * Returns the reference that is the subject of this clause.
	 * 
	 * @return A {@link ReferenceSymbolic}.
	 */
	public ReferenceSymbolic getReference() { return this.r; }

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 53 * result + ((r == null) ? 0 : r.hashCode());
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
		ClauseAssumeReferenceSymbolic other = (ClauseAssumeReferenceSymbolic) obj;
		if (r == null) {
			if (other.r != null) {
				return false;
			}
		} else if (!r.equals(other.r)) {
			return false;
		}
		return true;
	}
	
	
	@Override
	public ClauseAssumeReferenceSymbolic clone() {
		final ClauseAssumeReferenceSymbolic o;
		try {
			o = (ClauseAssumeReferenceSymbolic) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e);
		}
		return o;
	}
}

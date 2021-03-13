package jbse.mem;

import jbse.common.exc.InvalidInputException;
import jbse.val.ReferenceSymbolic;

/**
 * A path condition {@link Clause}, an assumption 
 * on a {@link ReferenceSymbolic}.
 * 
 * @author Pietro Braione
 *
 */
public abstract class ClauseAssumeReferenceSymbolic implements Clause {
	private final ReferenceSymbolic referenceSymbolic;

	/**
	 * Constructor.
	 * 
	 * @param referenceSymbolic a {@link ReferenceSymbolic}. It must not be {@code null}.
	 * @throws InvalidInputException if {@code referenceSymbolic == null}.
	 */
	protected ClauseAssumeReferenceSymbolic(ReferenceSymbolic referenceSymbolic) throws InvalidInputException { 
		if (referenceSymbolic == null) {
			throw new InvalidInputException("Tried to build a " + getClass().getName() + " with null referenceSymbolic.");
		}
	    this.referenceSymbolic = referenceSymbolic; 
	}

	/**
	 * Returns the reference that is the subject of this clause.
	 * 
	 * @return A {@link ReferenceSymbolic}.
	 */
	public ReferenceSymbolic getReference() { 
	    return this.referenceSymbolic; 
	}

	@Override
	public int hashCode() {
		int result = 103;
		result = 53 * result + ((this.referenceSymbolic == null) ? 0 : this.referenceSymbolic.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ClauseAssumeReferenceSymbolic other = (ClauseAssumeReferenceSymbolic) obj;
		if (this.referenceSymbolic == null) {
			if (other.referenceSymbolic != null) {
				return false;
			}
		} else if (!this.referenceSymbolic.equals(other.referenceSymbolic)) {
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

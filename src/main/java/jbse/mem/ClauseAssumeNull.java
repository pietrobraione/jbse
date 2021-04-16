package jbse.mem;

import jbse.common.exc.InvalidInputException;
import jbse.val.ReferenceSymbolic;

/**
 * A path condition {@link Clause}, an assumption 
 * that some {@link ReferenceSymbolic} is resolved to
 * null.
 * 
 * @author Pietro Braione
 *
 */
public class ClauseAssumeNull extends ClauseAssumeReferenceSymbolic {
	/**
	 * Constructor.
	 * 
	 * @param referenceSymbolic a {@link ReferenceSymbolic}. It must not be {@code null}.
	 * @throws InvalidInputException if {@code referenceSymbolic == null}.
	 */
	public ClauseAssumeNull(ReferenceSymbolic referenceSymbolic) throws InvalidInputException { 
		super(referenceSymbolic); 
	}
	
	@Override
	public void accept(ClauseVisitor v) throws Exception {
		v.visitClauseAssumeNull(this);
	}

	@Override
	public String toString() {
		final ReferenceSymbolic r = this.getReference();
		return r.toString() + " == null";
	}

	@Override
	public int hashCode() {
		final int prime = 113;
		int result = super.hashCode();
		result = prime * result;
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
		return true;
	}

	@Override
	public ClauseAssumeNull clone() {
		return (ClauseAssumeNull) super.clone();
	}
}

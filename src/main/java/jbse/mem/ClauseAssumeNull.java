package jbse.mem;

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
	 * @param r a {@link ReferenceSymbolic}. It must be resolved to null.
	 */
	public ClauseAssumeNull(ReferenceSymbolic r) { super(r); }
	
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

package jbse.mem;

import jbse.val.Expression;
import jbse.val.Primitive;

/**
 * A path condition {@link Clause}, an assumption 
 * on primitive values. 
 * 
 * @author Pietro Braione
 *
 */
public class ClauseAssume implements Clause {
	private final Primitive p;
	
	/**
	 * Constructor.
	 * 
	 * @param p a {@link Primitive}. It must not be {@code null}
	 * and must have boolean type.
	 */
	public ClauseAssume(Primitive p) { this.p = p; }
	
	/**
	 * Gets the assumption.
	 * 
	 * @return An {@link Expression}.
	 */
	public Primitive getCondition() { return this.p; }
	
	@Override
	public void accept(ClauseVisitor v) throws Exception {
		v.visitClauseAssume(this);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((p == null) ? 0 : p.hashCode());
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
		ClauseAssume other = (ClauseAssume) obj;
		if (p == null) {
			if (other.p != null) {
				return false;
			}
		} else if (!p.equals(other.p)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return p.toString();
	}
	
	@Override
	public ClauseAssume clone() {
		final ClauseAssume o;
		try {
			o = (ClauseAssume) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e);
		}		
		return o;
	}
}

package jbse.mem;

import jbse.common.Type;
import jbse.common.exc.InvalidInputException;
import jbse.val.Expression;
import jbse.val.Primitive;
import jbse.val.Simplex;

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
	 * @param p a {@link Primitive}. It must not be {@code null},
	 * it must be an instance of either {@link Simplex} or {@link Expression}, 
	 * and must have boolean type.
	 * @throws NullPointerException if {@code p == null}.
	 * @throws InvalidInputException if {@code p} has not boolean type, or
	 * is not an instance of either {@link Simplex} or {@link Expression}.
	 */
	public ClauseAssume(Primitive p) throws InvalidInputException {
		if (p == null) {
			throw new NullPointerException("Tried to build a ClauseAssume with null Primitive value.");
		}
		if (p.getType() != Type.BOOLEAN || (! (p instanceof Simplex) && ! (p instanceof Expression))) {
			throw new InvalidInputException("Tried to build a ClauseAssume with Primitive value " + p.toString() + ".");
		}
		this.p = p; 
	}
	
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
		result = prime * result + ((this.p == null) ? 0 : this.p.hashCode());
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
		final ClauseAssume other = (ClauseAssume) obj;
		if (this.p == null) {
			if (other.p != null) {
				return false;
			}
		} else if (!this.p.equals(other.p)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return this.p.toString();
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

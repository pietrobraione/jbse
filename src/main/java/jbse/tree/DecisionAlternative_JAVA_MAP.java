package jbse.tree;

import jbse.common.exc.InvalidInputException;
import jbse.val.Primitive;

/**
 * {@link DecisionAlternative} for the refinement of 
 * JAVA_MAP (hashCode).
 */
public final class DecisionAlternative_JAVA_MAP implements DecisionAlternative {
	private final Primitive predicate;
	private final int branchNumber;
	private final String toString;
	private int hashCode;
	
	public DecisionAlternative_JAVA_MAP(Primitive predicate, int branchNumber) throws InvalidInputException {
		if (predicate == null) {
			throw new InvalidInputException("Tried to create a DecisionAlternative_JAVA_MAP with null predicate.");
		}
		this.predicate = predicate;
		this.branchNumber = branchNumber;
		this.toString = "JAVA_MAP:" + this.predicate.toString();
        final int prime = 49;
        int tmpHashCode = 1;
        tmpHashCode = prime * tmpHashCode + this.predicate.hashCode();
        this.hashCode = tmpHashCode;
	}

	@Override
	public String getIdentifier() {
		return this.toString;
	}

	@Override
	public int getBranchNumber() {
		return this.branchNumber;
	}

	@Override
	public boolean trivial() {
		return false;
	}

	@Override
	public boolean concrete() {
		return false;
	}

	@Override
	public boolean noDecision() {
		return false;
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
		final DecisionAlternative_JAVA_MAP other = (DecisionAlternative_JAVA_MAP) obj;
		if (!this.predicate.equals(other.predicate)) {
			return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		return this.hashCode;
	}
	
	@Override
	public String toString() {
		return this.toString;
	}
}
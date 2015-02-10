package jbse.tree;

public final class DecisionAlternative_XSWITCH implements DecisionAlternative {
	/** The value of the case entry. */
	private final int value;
	
	private final boolean isConcrete;
	
	private final int branchNumber;
	
	private DecisionAlternative_XSWITCH(int value, boolean isConcrete, int branchNumber) {
		this.value = value;
		this.isConcrete = isConcrete;
		this.branchNumber = branchNumber;
	}
	
	/**
	 * Factory method for switch entries (except default), concrete.
	 * 
	 * @param value the {@code int} value of the case entry.
	 * @param branchNumber an {@code int}, the number of the branch.
	 */
	public static DecisionAlternative_XSWITCH toConcrete(int value, int branchNumber) { 
		return new DecisionAlternative_XSWITCH(value, true, branchNumber);
	}	
	
	/**
	 * Factory method for switch entries (except default), nonconcrete. 
	 * 
	 * @param value the {@code int} value of the case entry.
	 * @param branchNumber an {@code int}, the number of the branch.
	 */
	public static DecisionAlternative_XSWITCH toNonconcrete(int value, int branchNumber) { 
		return new DecisionAlternative_XSWITCH(value, false, branchNumber);
	}

	/**
	 * Factory method for default switch entries, concrete.
	 * 
	 * @param branchNumber an {@code int}, the number of the branch.
	 * @return the corresponding {@link DecisionAlternative_XSWITCH}.
	 */
	public static DecisionAlternative_XSWITCH toConcreteDefault(int branchNumber) {
		return new DecisionAlternative_XSWITCH(0, true, branchNumber);
	}
	
	/**
	 * Factory method for default switch entries, nonconcrete. 
	 * 
	 * @param branchNumber
	 * @return the corresponding {@link DecisionAlternative_XSWITCH}.
	 */
	public static DecisionAlternative_XSWITCH toNonconcreteDefault(int branchNumber) {
		return new DecisionAlternative_XSWITCH(0, false, branchNumber);
	}
	
	@Deprecated
	public DecisionAlternative_XSWITCH mkNonconcrete() {
		return new DecisionAlternative_XSWITCH(this.value, false, this.branchNumber);
	}
	
	public boolean isDefault() {
		return (this.value == 0);
	}
	
	public int value() {
		return this.value;
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
		DecisionAlternative_XSWITCH other = (DecisionAlternative_XSWITCH) obj;
		if (isConcrete != other.isConcrete) {
			return false;
		}
		if (value != other.value) {
			return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isConcrete ? 1231 : 1237);
		result = prime * result + value;
		return result;
	}

	@Override
	public String toString() {
		if (this.isDefault()) {
			return "DEFAULT";
		} else {
			return Integer.toString(this.value);
		}
	}

	@Override
	public String getIdentifier() {
		return this.toString();
	}
	
	@Override
	public int getBranchNumber() {
		return this.branchNumber;
	}

	@Override
	public boolean concrete() {
		return this.isConcrete;
	}

	@Override
	public boolean trivial() {
		return this.concrete();
	}
}

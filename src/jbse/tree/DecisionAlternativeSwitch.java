package jbse.tree;

public final class DecisionAlternativeSwitch implements DecisionAlternative {
	/** The value of the case entry. */
	private final int value;
	
	private final boolean isConcrete;
	
	private final int branchNumber;
	
	private DecisionAlternativeSwitch(int value, boolean isConcrete, int branchNumber) {
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
	public static DecisionAlternativeSwitch toConcrete(int value, int branchNumber) { 
		return new DecisionAlternativeSwitch(value, true, branchNumber);
	}	
	
	/**
	 * Factory method for switch entries (except default), nonconcrete. 
	 * 
	 * @param value the {@code int} value of the case entry.
	 * @param branchNumber an {@code int}, the number of the branch.
	 */
	public static DecisionAlternativeSwitch toNonconcrete(int value, int branchNumber) { 
		return new DecisionAlternativeSwitch(value, false, branchNumber);
	}

	/**
	 * Factory method for default switch entries, concrete.
	 * 
	 * @param branchNumber an {@code int}, the number of the branch.
	 * @return the corresponding {@link DecisionAlternativeSwitch}.
	 */
	public static DecisionAlternativeSwitch toConcreteDefault(int branchNumber) {
		return new DecisionAlternativeSwitch(0, true, branchNumber);
	}
	
	/**
	 * Factory method for default switch entries, nonconcrete. 
	 * 
	 * @param branchNumber
	 * @return the corresponding {@link DecisionAlternativeSwitch}.
	 */
	public static DecisionAlternativeSwitch toNonconcreteDefault(int branchNumber) {
		return new DecisionAlternativeSwitch(0, false, branchNumber);
	}
	
	@Deprecated
	public DecisionAlternativeSwitch mkNonconcrete() {
		return new DecisionAlternativeSwitch(this.value, false, this.branchNumber);
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
		DecisionAlternativeSwitch other = (DecisionAlternativeSwitch) obj;
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

package jbse.tree;

public final class DecisionAlternative_XSWITCH implements DecisionAlternative {
	/** The value of the case entry. */
	private final int value;
	
	private final boolean isConcrete;
	
	private final String toString;
	
	private DecisionAlternative_XSWITCH(int value, boolean isConcrete, int branchNumber) {
		this.value = value;
		this.isConcrete = isConcrete;
		this.toString = "XSWITCH:" + (isDefault() ? "DEFAULT" : Integer.toString(this.value));
	}
	
	/**
	 * Factory method for switch entries (except default), concrete.
	 * 
	 * @param value the {@code int} value of the case entry. It must be
	 *        greater than zero.
	 */
	public static DecisionAlternative_XSWITCH toConcrete(int value, int branchNumber) { 
		return new DecisionAlternative_XSWITCH(value, true, branchNumber);
	}	
	
	/**
	 * Factory method for switch entries (except default), nonconcrete. 
	 * 
	 * @param value the {@code int} value of the case entry. It must be
     *        greater than zero.
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
	 * @param branchNumber an {@code int}, the number of the branch.
	 * @return the corresponding {@link DecisionAlternative_XSWITCH}.
	 */
	public static DecisionAlternative_XSWITCH toNonconcreteDefault(int branchNumber) {
		return new DecisionAlternative_XSWITCH(0, false, branchNumber);
	}
	
	public boolean isDefault() {
		return (this.value == 0);
	}
	
	public int value() {
		return this.value;
	}

	@Override
	public String getIdentifier() {
		return this.toString;
	}
	
	@Override
	public int getBranchNumber() {
		return this.value;
	}

    @Override
    public boolean trivial() {
        return this.isConcrete;
    }

	@Override
	public boolean concrete() {
		return this.isConcrete;
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
        final DecisionAlternative_XSWITCH other = (DecisionAlternative_XSWITCH) obj;
        if (this.value != other.value) {
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.toString;
    }
}

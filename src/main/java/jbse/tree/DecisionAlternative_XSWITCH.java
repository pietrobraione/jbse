package jbse.tree;

public final class DecisionAlternative_XSWITCH implements DecisionAlternative {
	/** The value of the case entry, when {@code isDefault == false}. */
	private final int value;
	
	/** Is this the default case? */
	private final boolean isDefault;
	
	private final boolean isConcrete;
	
	private final String toString;
	
	private DecisionAlternative_XSWITCH(int value, boolean isDefault, boolean isConcrete, int branchNumber) {
		this.value = value;
		this.isDefault = isDefault;
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
		return new DecisionAlternative_XSWITCH(value, false, true, branchNumber);
	}	
	
	/**
	 * Factory method for switch entries (except default), nonconcrete. 
	 * 
	 * @param value the {@code int} value of the case entry. It must be
     *        greater than zero.
	 * @param branchNumber an {@code int}, the number of the branch.
	 */
	public static DecisionAlternative_XSWITCH toNonconcrete(int value, int branchNumber) { 
		return new DecisionAlternative_XSWITCH(value, false, false, branchNumber);
	}

	/**
	 * Factory method for default switch entries, concrete.
	 * 
	 * @param branchNumber an {@code int}, the number of the branch.
	 * @return the corresponding {@link DecisionAlternative_XSWITCH}.
	 */
	public static DecisionAlternative_XSWITCH toConcreteDefault(int branchNumber) {
		return new DecisionAlternative_XSWITCH(0, true, true, branchNumber);
	}
	
	/**
	 * Factory method for default switch entries, nonconcrete. 
	 * 
	 * @param branchNumber an {@code int}, the number of the branch.
	 * @return the corresponding {@link DecisionAlternative_XSWITCH}.
	 */
	public static DecisionAlternative_XSWITCH toNonconcreteDefault(int branchNumber) {
		return new DecisionAlternative_XSWITCH(0, true, false, branchNumber);
	}
	
	public boolean isDefault() {
		return this.isDefault;
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
    public final boolean noDecision() {
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

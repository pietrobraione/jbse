package jbse.tree;

public final class DecisionAlternative_XSWITCH implements DecisionAlternative {
	/** The value of the case entry, when {@code isDefault == false}. */
	private final int value;
	
	/** Is this the default case? */
	private final boolean isDefault;
	
	/** Does this alternative result from a decision on concrete values?*/
	private final boolean isConcrete;
	
    /** The {@link String} representation of this object. */
	private final String toString;
	
	private DecisionAlternative_XSWITCH(int value, boolean isDefault, boolean isConcrete) {
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
	public static DecisionAlternative_XSWITCH toConcrete(int value) { 
		return new DecisionAlternative_XSWITCH(value, false, true);
	}	
	
	/**
	 * Factory method for switch entries (except default), nonconcrete. 
	 * 
	 * @param value the {@code int} value of the case entry. It must be
     *        greater than zero.
	 * @param branchNumber an {@code int}, the number of the branch.
	 */
	public static DecisionAlternative_XSWITCH toNonconcrete(int value) { 
		return new DecisionAlternative_XSWITCH(value, false, false);
	}

	/**
	 * Factory method for default switch entries, concrete.
	 * 
	 * @param branchNumber an {@code int}, the number of the branch.
	 * @return the corresponding {@link DecisionAlternative_XSWITCH}.
	 */
	public static DecisionAlternative_XSWITCH toConcreteDefault() {
		return new DecisionAlternative_XSWITCH(0, true, true);
	}
	
	/**
	 * Factory method for default switch entries, nonconcrete. 
	 * 
	 * @param branchNumber an {@code int}, the number of the branch.
	 * @return the corresponding {@link DecisionAlternative_XSWITCH}.
	 */
	public static DecisionAlternative_XSWITCH toNonconcreteDefault() {
		return new DecisionAlternative_XSWITCH(0, true, false);
	}
	
	/**
	 * Checks whether this decision alternative
	 * is for the default case.
	 * 
	 * @return a {@code boolean}, {@code true} iff
	 *         this decision alternative is for the
	 *         default case.
	 */
	public boolean isDefault() {
		return this.isDefault;
	}

	/**
	 * Returns the value for the switch case this decision
	 * alternative represents (if it is a nondefault one).
	 * 
	 * @return an {@code int}; The returned value is meaningful
	 *         only if {@link #isDefault}{@code () == false}.
	 */
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

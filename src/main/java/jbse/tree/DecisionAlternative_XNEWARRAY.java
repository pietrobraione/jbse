package jbse.tree;

/**
 * {@link DecisionAlternative} for the array creation (*newarray) bytecodes:
 * newarray, anewarray, multianewarray.
 * 
 * @author Pietro Braione
 */
public abstract class DecisionAlternative_XNEWARRAY implements DecisionAlternative {
	private final boolean isConcrete;
	private final int branchNumber;

	protected DecisionAlternative_XNEWARRAY(boolean isConcrete, int branchNumber) {
    	this.isConcrete = isConcrete;
		this.branchNumber = branchNumber;
	}
	    
    public static DecisionAlternative_XNEWARRAY toConcrete(boolean ok) {
        return (ok ? new DecisionAlternative_XNEWARRAY_Ok(true) : new DecisionAlternative_XNEWARRAY_Wrong(true));
    }

    public static DecisionAlternative_XNEWARRAY toNonconcrete(boolean ok) {
        return (ok ? new DecisionAlternative_XNEWARRAY_Ok(false) : new DecisionAlternative_XNEWARRAY_Wrong(false));
    }

    public abstract boolean ok();
	
	@Override
	public final String getIdentifier() {
		return toString();
	}
	
	@Override
	public final int getBranchNumber() {
		return this.branchNumber;
	}

	@Override
	public final boolean trivial() {
		return this.concrete();
	}

    @Override
    public final boolean concrete() {
        return this.isConcrete;
    }
    
    @Override
    public final boolean noDecision() {
        return false;
    }
}

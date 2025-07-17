package jbse.tree;

/**
 * A {@link DecisionAlternative} for the outcome of a 
 * cast/instanceof bytecode.
 * 
 * @author Pietro Braione
 *
 */
public abstract class DecisionAlternative_CASTINSTANCEOF implements DecisionAlternative {	
    private final int branchNumber;
    
    protected DecisionAlternative_CASTINSTANCEOF(int branchNumber) {
    	this.branchNumber = branchNumber;
    }
    

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
		return true; //not really...
	}
    
    @Override
    public final boolean concrete() {
        return true; //not really...
    }
    
    @Override
    public final boolean noDecision() {
        return false;
    }
}

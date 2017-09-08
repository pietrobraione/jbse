package jbse.tree;

/**
 * A {@link DecisionAlternative} for the outcome of a 
 * "branch if comparison" bytecode.
 * 
 * @author Pietro Braione
 *
 */
public abstract class DecisionAlternative_IFX implements DecisionAlternative {	
    private final boolean value;
    private final boolean isConcrete;
    private final int branchNumber;
    
    protected DecisionAlternative_IFX(boolean value, boolean isConcrete, int branchNumber) { 
    	this.value = value; 
    	this.isConcrete = isConcrete;
    	this.branchNumber = branchNumber;
    }
    
    public static DecisionAlternative_IFX toConcrete(boolean b) {
        return (b ? new DecisionAlternative_IFX_True(true) : new DecisionAlternative_IFX_False(true));
    }

    public static DecisionAlternative_IFX toNonconcrete(boolean b) {
        return (b ? new DecisionAlternative_IFX_True(false) : new DecisionAlternative_IFX_False(false));
    }

    public final boolean value() { 
    	return this.value; 
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
		return this.isConcrete;
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

package jbse.tree;

/**
 * A {@link DecisionAlternative} for the outcome of a 
 * "branch if integer comparison" bytecode.
 * 
 * @author Pietro Braione
 *
 */
public abstract class DecisionAlternativeIf implements DecisionAlternative {	
    private final boolean value;
    private final boolean isConcrete;
    private final int branchNumber;
    
    protected DecisionAlternativeIf(boolean value, boolean isConcrete, int branchNumber) { 
    	this.value = value; 
    	this.isConcrete = isConcrete;
    	this.branchNumber = branchNumber;
    }
    
    @Deprecated
	public abstract DecisionAlternativeIf mkNonconcrete();
	
    public static DecisionAlternativeIf toConcrete(boolean b) {
        return (b ? new DecisionAlternativeIfTrue(true) : new DecisionAlternativeIfFalse(true));
    }

    public static DecisionAlternativeIf toNonconcrete(boolean b) {
        return (b ? new DecisionAlternativeIfTrue(false) : new DecisionAlternativeIfFalse(false));
    }

    public final boolean value() { 
    	return this.value; 
    }    
	
	@Override
	public final String getIdentifier() {
		return toString();
	}
	
	@Override
	public final boolean concrete() {
		return this.isConcrete;
	}
	
	@Override
	public final int getBranchNumber() {
		return this.branchNumber;
	}

	@Override
	public final boolean trivial() {
		return this.concrete();
	}
}

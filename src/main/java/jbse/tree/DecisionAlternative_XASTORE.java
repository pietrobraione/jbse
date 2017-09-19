package jbse.tree;

/**
 * {@link DecisionAlternative} for the store into array (*astore) bytecodes
 * ([a/b/c/d/f/i/l/s]astore) and {@code System.arraycopy}.
 * 
 * @author Pietro Braione
 */
public abstract class DecisionAlternative_XASTORE implements DecisionAlternative {	
	private final boolean isIn;
	private final boolean isConcrete;
	private final int branchNumber;

	protected DecisionAlternative_XASTORE(boolean isIn, boolean isConcrete, int branchNumber) {
		this.isIn = isIn;
		this.isConcrete = isConcrete;
		this.branchNumber = branchNumber;
	}
	
    public static DecisionAlternative_XASTORE toConcrete(boolean isIn) {
        return (isIn ? new DecisionAlternative_XASTORE_In(true) : new DecisionAlternative_XASTORE_Out(true));
    }

    public static DecisionAlternative_XASTORE toNonconcrete(boolean isIn) {
        return (isIn ? new DecisionAlternative_XASTORE_In(false) : new DecisionAlternative_XASTORE_Out(false));
    }

	public final boolean isInRange() { 
		return this.isIn; 
	}
	
	@Override
	public final String getIdentifier() {
		return this.toString();
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


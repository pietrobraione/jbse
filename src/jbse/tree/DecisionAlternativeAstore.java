package jbse.tree;

/**
 * {@link DecisionAlternative} for the store into array (*astore) bytecodes:
 * aastore, castore, bastore, dastore, fastore, iastore, lastore, sastore.
 * 
 * @author Pietro Braione
 */
public abstract class DecisionAlternativeAstore implements DecisionAlternative {	
	private final boolean isIn;
	private final boolean isConcrete;
	private final int branchNumber;

	protected DecisionAlternativeAstore(boolean isIn, boolean isConcrete, int branchNumber) {
		this.isIn = isIn;
		this.isConcrete = isConcrete;
		this.branchNumber = branchNumber;
	}
	
    public static DecisionAlternativeAstore toConcrete(boolean isIn) {
        return (isIn ? new DecisionAlternativeAstoreIn(true) : new DecisionAlternativeAstoreOut(true));
    }

    public static DecisionAlternativeAstore toNonconcrete(boolean isIn) {
        return (isIn ? new DecisionAlternativeAstoreIn(false) : new DecisionAlternativeAstoreOut(false));
    }

    @Deprecated
	public abstract DecisionAlternativeAstore mkNonconcrete();
	
	public final boolean isInRange() { 
		return this.isIn; 
	}
	
	@Override
	public final String getIdentifier() {
		return this.toString();
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


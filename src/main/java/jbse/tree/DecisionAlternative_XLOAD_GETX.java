package jbse.tree;

/**
 * {@link DecisionAlternative} for all the load from local variable (*load*) and 
 * load from field (get*) bytecodes: [a/d/f/i/l]load[_0/1/2/3], get[field/static].
 * 
 * @author Pietro Braione
 */
public abstract class DecisionAlternative_XLOAD_GETX 
implements DecisionAlternative_XYLOAD_GETX_Loads {
	protected static final String ALT_CODE = "XLOAD_GETX";
	
	private final String branchId;
	private final int branchNumber;
	
	public DecisionAlternative_XLOAD_GETX(String branchId, int branchNumber) {
		this.branchId = branchId;
		this.branchNumber = branchNumber;
	}
	
	public abstract void accept(VisitorDecisionAlternative_XLOAD_GETX v) throws Exception;

	@Override
	public final String getIdentifier() {
		return this.branchId;
	}
	
	@Override
	public final int getBranchNumber() {
		return this.branchNumber;
	}
    
    @Override
    public final boolean noDecision() {
        return false;
    }
}

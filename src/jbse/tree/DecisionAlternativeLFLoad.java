package jbse.tree;

/**
 * {@link DecisionAlternative} for all the load from local variable (*load*) and 
 * load from field (get*) bytecodes: aload[_0/1/2/3], dload[_0/1/2/3], 
 * fload[_0/1/2/3], iload[_0/1/2/3], lload[_0/1/2/3], getfield, getstatic.
 * 
 * @author Pietro Braione
 */
public abstract class DecisionAlternativeLFLoad implements DecisionAlternativeLoad {
	protected static final String ALT_CODE = "L";
	
	private final String branchId;
	private final int branchNumber;
	
	public DecisionAlternativeLFLoad(String branchId, int branchNumber) {
		this.branchId = branchId;
		this.branchNumber = branchNumber;
	}
	
	public abstract void accept(DecisionAlternativeLFLoadVisitor v) throws Exception;

	@Override
	public final String getIdentifier() {
		return this.branchId;
	}
	
	@Override
	public final int getBranchNumber() {
		return this.branchNumber;
	}
}

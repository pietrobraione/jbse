package jbse.tree;

import jbse.val.Expression;

/**
 * {@link DecisionAlternative} for the load from array (*aload) bytecodes:
 * [a/b/c/d/f/i/l/s]aload.
 * 
 * @author Pietro Braione
 *
 */
public abstract class DecisionAlternative_XALOAD implements DecisionAlternative {
	protected static final String ALT_CODE = "A";

	private final Expression arrayAccessExpression;
	
	private final String branchId;
	
	private final int branchNumber;

	protected DecisionAlternative_XALOAD(String branchId, Expression arrayAccessExpression, int branchNumber) {
		this.branchId = branchId;
		this.arrayAccessExpression = arrayAccessExpression;
		this.branchNumber = branchNumber;
	}
	
	public abstract void accept(VisitorDecisionAlternative_XALOAD v) throws Exception;

	public final Expression getArrayAccessExpression() {
		return this.arrayAccessExpression;
	}
	
	@Override
	public final String getIdentifier() {
		return this.branchId;
	}
	
	@Override
	public final int getBranchNumber() {
		return this.branchNumber;
	}
}

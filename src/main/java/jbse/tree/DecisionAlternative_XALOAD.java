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
	protected static final String ALT_CODE = "XALOAD";

	private final Expression arrayAccessExpression;
	
	private final String branchId;
	
	private final int branchNumber;
	
	private final int hashCode;

	protected DecisionAlternative_XALOAD(String branchId, Expression arrayAccessExpression, int branchNumber) {
		this.branchId = branchId;
		this.arrayAccessExpression = arrayAccessExpression;
		this.branchNumber = branchNumber;
        final int prime = 109;
        int result = 1;
        result = prime * result + 
            ((this.arrayAccessExpression == null) ? 
            0 : this.arrayAccessExpression.hashCode());
        this.hashCode = result;
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
	    
    @Override
    public final boolean noDecision() {
        return false;
    }


    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DecisionAlternative_XALOAD other = (DecisionAlternative_XALOAD) obj;
        if (this.arrayAccessExpression == null) {
            if (other.arrayAccessExpression != null) return false;
        } else if (!this.arrayAccessExpression.equals(other.arrayAccessExpression)) {
            return false;
        }
        return true;
    }
    
    @Override
    public final String toString() {
        return this.branchId;
    }
}

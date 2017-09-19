package jbse.tree;

import jbse.val.Expression;

/**
 * {@link DecisionAlternative_XALOAD} for the case the array access
 * was out of bounds.
 * 
 * @author Pietro Braione
 */
public final class DecisionAlternative_XALOAD_Out extends DecisionAlternative_XALOAD {
    private final boolean isTrivial;
    private final int hashCode;
    
	/**
	 * Constructor, nonconcrete.
	 * 
	 * @param arrayAccessExpression the array access {@link Expression}.
	 * @param branchNumber an {@code int}, the branch number.
	 */
	public DecisionAlternative_XALOAD_Out(Expression arrayAccessExpression, int branchNumber) {
		super(ALT_CODE + "_Out", arrayAccessExpression, branchNumber);
		this.isTrivial = (arrayAccessExpression == null);
        final int prime = 1217;
        int result = super.hashCode();
        result = prime * result;
        this.hashCode = result;
	}

	/**
	 * Constructor, concrete.
	 */
	public DecisionAlternative_XALOAD_Out(int branchNumber) {
		this(null, branchNumber);
	}
	
	@Override
	public void accept(VisitorDecisionAlternative_XALOAD v) throws Exception {
		v.visitDecisionAlternative_XALOAD_Out(this);
	}

    @Override
    public boolean trivial() {
        return this.isTrivial;
    }

	@Override
	public boolean concrete() {
		return this.isTrivial;
	}

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        return true;
    }		
}

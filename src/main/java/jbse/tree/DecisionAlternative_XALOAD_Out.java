package jbse.tree;

import jbse.val.Expression;
import jbse.val.Primitive;
import jbse.val.Term;

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
	 * @param arrayAccessExpression an {@link Expression} containing {@code indexFormal}, 
	 *        signifying the condition under which the array access yields {@code valToLoad} 
	 *        as result. It can be {@code null}, in which case it is equivalent to {@code true} but 
	 *        additionally denotes the fact that the array was accessed by a concrete index.
	 * @param indexFormal the {@link Term} used in {@code accessExpression} to indicate
	 *        the array index. It must not be {@code null}.
	 * @param indexActual a {@link Primitive}, the actual index used to access the array.
	 *        It must not be {@code null}.
	 * @param arrayAccessExpressionSimplified a simplification of {@code arrayAccessExpression}, 
	 *        or {@code null} if {@code arrayAccessExpression} simplifies to {@code true}.
	 * @param branchNumber an {@code int}, the branch number.
	 */
	public DecisionAlternative_XALOAD_Out(Expression arrayAccessExpression, Term indexFormal, Primitive indexActual, Expression arrayAccessExpressionSimplified, int branchNumber) {
		super(ALT_CODE + "_Out:" + (arrayAccessExpressionSimplified == null ? "true" : arrayAccessExpressionSimplified), arrayAccessExpression, indexFormal, indexActual, arrayAccessExpressionSimplified, branchNumber);
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
		this(null, null , null, null, branchNumber);
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

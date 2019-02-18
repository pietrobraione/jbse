package jbse.tree;

import jbse.val.Expression;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.ReferenceSymbolic;
import jbse.val.Term;

/**
 * {@link DecisionAlternative_XALOAD} for the case a read access to an array
 * returned a {@link ReferenceSymbolic} to null.
 * 
 * @author Pietro Braione
 */
public final class DecisionAlternative_XALOAD_Null extends DecisionAlternative_XALOAD_Unresolved implements DecisionAlternative_XYLOAD_GETX_Null {
    private final int hashCode;

    /**
     * Constructor.
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
     * @param referenceToResolve the {@link ReferenceSymbolic} loaded from the array.
     * @param fresh {@code true} iff {@code referenceToResolve} is fresh, i.e., 
     *        is not stored in the array and, therefore, must be written
     *        back to the array.
     * @param arrayReference when {@code fresh == true} is a {@link Reference} to the array 
     *        where {@code referenceToResolve} originates from.
     * @param branchNumber an {@code int}, the branch number.
     */
    public DecisionAlternative_XALOAD_Null(Expression arrayAccessExpression, Term indexFormal, Primitive indexActual, Expression arrayAccessExpressionSimplified, ReferenceSymbolic referenceToResolve, boolean fresh, Reference arrayReference, int branchNumber) {
        super(ALT_CODE + "_Null:" + (arrayAccessExpressionSimplified == null ? "true" : arrayAccessExpressionSimplified), arrayAccessExpression, indexFormal, indexActual, arrayAccessExpressionSimplified, referenceToResolve, fresh, arrayReference, branchNumber);
        final int prime = 3331;
        int result = super.hashCode();
        result = prime * result;
        this.hashCode = result;
    }

    @Override
    public void accept(VisitorDecisionAlternative_XALOAD v) throws Exception {
        v.visitDecisionAlternative_XALOAD_Null(this);
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        return true;
    }       
}

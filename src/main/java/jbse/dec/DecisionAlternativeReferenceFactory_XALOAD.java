package jbse.dec;

import jbse.bc.ClassFile;
import jbse.tree.DecisionAlternative_XALOAD_Aliases;
import jbse.tree.DecisionAlternative_XALOAD_Expands;
import jbse.tree.DecisionAlternative_XALOAD_Null;
import jbse.val.Expression;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.ReferenceSymbolic;
import jbse.val.Term;

/**
 * A Concrete Factory for decision alternatives used when the reference
 * that must be resolved comes from an array (XALOAD bytecodes).
 *  
 * @author Pietro Braione
 *
 */
class DecisionAlternativeReferenceFactory_XALOAD 
implements DecisionAlternativeReferenceFactory<DecisionAlternative_XALOAD_Aliases, 
DecisionAlternative_XALOAD_Expands, DecisionAlternative_XALOAD_Null> {
    private final Expression arrayAccessExpression;
    private final Term indexFormal;
    private final Primitive indexActual;
    private final Expression arrayAccessExpressionSimplified;
    private final boolean fresh;
    private final Reference arrayReference;

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
     * @param fresh {@code true} iff {@code valToLoad} is fresh, i.e., 
     *        is not stored in the array and, therefore, must be written
     *        back to the array.
     * @param arrayReference when {@code fresh == true} is a {@link Reference} to the array 
     *        where {@code valueToLoad} originates from.
     */
    public DecisionAlternativeReferenceFactory_XALOAD(Expression arrayAccessExpression, Term indexFormal, Primitive indexActual, Expression arrayAccessExpressionSimplified, boolean fresh, Reference arrayReference) {
        this.arrayAccessExpression = arrayAccessExpression;
        this.indexFormal = indexFormal;
        this.indexActual = indexActual;
        this.arrayAccessExpressionSimplified = arrayAccessExpressionSimplified;
        this.fresh = fresh;
        this.arrayReference = arrayReference;
    }

    @Override
    public DecisionAlternative_XALOAD_Aliases 
    createAlternativeRefAliases(ReferenceSymbolic referenceToResolve, long objectPosition, ReferenceSymbolic objectOrigin, int branchNumber) {
        return new DecisionAlternative_XALOAD_Aliases(this.arrayAccessExpression, this.indexFormal, this.indexActual, this.arrayAccessExpressionSimplified, 
        											  referenceToResolve, this.fresh, this.arrayReference, objectPosition, objectOrigin, branchNumber);
    }

    @Override
    public DecisionAlternative_XALOAD_Expands 
    createAlternativeRefExpands(ReferenceSymbolic referenceToResolve, ClassFile classFile, int branchNumber) {
        return new DecisionAlternative_XALOAD_Expands(this.arrayAccessExpression, this.indexFormal, this.indexActual, this.arrayAccessExpressionSimplified,
        											  referenceToResolve, this.fresh, this.arrayReference, classFile, branchNumber);
    }

    @Override
    public DecisionAlternative_XALOAD_Null 
    createAlternativeRefNull(ReferenceSymbolic referenceToResolve, int branchNumber) {
        return new DecisionAlternative_XALOAD_Null(this.arrayAccessExpression, this.indexFormal, this.indexActual, this.arrayAccessExpressionSimplified, 
        										   referenceToResolve, this.fresh, this.arrayReference, branchNumber);
    }
}
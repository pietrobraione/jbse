package jbse.dec;

import jbse.bc.ClassFile;
import jbse.tree.DecisionAlternative_XALOAD_Aliases;
import jbse.tree.DecisionAlternative_XALOAD_Expands;
import jbse.tree.DecisionAlternative_XALOAD_Null;
import jbse.val.Expression;
import jbse.val.MemoryPath;
import jbse.val.Reference;
import jbse.val.ReferenceSymbolic;

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
    private final boolean fresh;
    private final Reference arrayReference;

    /**
     * Constructor.
     * 
     * @param arrayAccessExpression the array access {@link Expression}.
     * @param fresh {@code true} iff {@code valToLoad} is fresh, i.e., 
     *        is not stored in the array and, therefore, must be written
     *        back to the array.
     * @param arrayReference when {@code fresh == true} is a {@link Reference} to the array 
     *        where {@code valueToLoad} originates from.
     */
    public DecisionAlternativeReferenceFactory_XALOAD(Expression arrayAccessExpression, boolean fresh, Reference arrayReference) {
        this.arrayAccessExpression = arrayAccessExpression;
        this.fresh = fresh;
        this.arrayReference = arrayReference;
    }

    @Override
    public DecisionAlternative_XALOAD_Aliases 
    createAlternativeRefAliases(ReferenceSymbolic referenceToResolve, long objectPosition, MemoryPath objectOrigin, int branchNumber) {
        return new DecisionAlternative_XALOAD_Aliases(this.arrayAccessExpression, referenceToResolve, this.fresh,
                                                      this.arrayReference, objectPosition, objectOrigin, branchNumber);
    }

    @Override
    public DecisionAlternative_XALOAD_Expands 
    createAlternativeRefExpands(ReferenceSymbolic referenceToResolve, ClassFile classFile, int branchNumber) {
        return new DecisionAlternative_XALOAD_Expands(this.arrayAccessExpression, referenceToResolve, this.fresh, 
                                                      this.arrayReference, classFile, branchNumber);
    }

    @Override
    public DecisionAlternative_XALOAD_Null 
    createAlternativeRefNull(ReferenceSymbolic referenceToResolve, int branchNumber) {
        return new DecisionAlternative_XALOAD_Null(this.arrayAccessExpression, referenceToResolve, this.fresh, 
                                                   this.arrayReference, branchNumber);
    }
}
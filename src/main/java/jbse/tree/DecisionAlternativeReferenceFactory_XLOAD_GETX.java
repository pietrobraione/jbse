package jbse.tree;

import jbse.bc.ClassFile;
import jbse.val.ReferenceSymbolic;

/**
 * A Concrete Factory for decision alternatives used when the reference
 * that must be resolved comes from a local variable (XLOAD_GETX bytecodes).
 *  
 * @author Pietro Braione
 *
 */
public final class DecisionAlternativeReferenceFactory_XLOAD_GETX 
implements DecisionAlternativeReferenceFactory<DecisionAlternative_XLOAD_GETX_Aliases, 
DecisionAlternative_XLOAD_GETX_Expands, DecisionAlternative_XLOAD_GETX_Null> {
    @Override
    public DecisionAlternative_XLOAD_GETX_Aliases 
    createAlternativeRefAliases(ReferenceSymbolic referenceToResolve, long objectPosition, ReferenceSymbolic objectOrigin, int branchNumber) {
        return new DecisionAlternative_XLOAD_GETX_Aliases(referenceToResolve, objectPosition, objectOrigin, branchNumber);
    }

    @Override
    public DecisionAlternative_XLOAD_GETX_Expands 
    createAlternativeRefExpands(ReferenceSymbolic referenceToResolve, ClassFile classFile, int branchNumber) {
        return new DecisionAlternative_XLOAD_GETX_Expands(referenceToResolve, classFile, branchNumber);
    }

    @Override
    public DecisionAlternative_XLOAD_GETX_Null 
    createAlternativeRefNull(ReferenceSymbolic referenceToResolve, int branchNumber) {
        return new DecisionAlternative_XLOAD_GETX_Null(referenceToResolve, branchNumber);
    }
}
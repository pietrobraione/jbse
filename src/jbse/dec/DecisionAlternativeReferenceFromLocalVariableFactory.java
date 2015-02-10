package jbse.dec;

import jbse.tree.DecisionAlternative_XLOAD_GETX_RefAliases;
import jbse.tree.DecisionAlternative_XLOAD_GETX_RefExpands;
import jbse.tree.DecisionAlternative_XLOAD_GETX_RefNull;
import jbse.val.ReferenceSymbolic;

/**
 * A Concrete Factory for decision alternatives used when the reference
 * that must be resolved comes from a local variable.
 *  
 * @author Pietro Braione
 *
 */
class DecisionAlternativeReferenceFromLocalVariableFactory 
implements DecisionAlternativeReferenceFactory<DecisionAlternative_XLOAD_GETX_RefAliases, 
DecisionAlternative_XLOAD_GETX_RefExpands, DecisionAlternative_XLOAD_GETX_RefNull> {
	@Override
	public DecisionAlternative_XLOAD_GETX_RefAliases 
	createAlternativeRefAliases(ReferenceSymbolic refToResolve, long objectPosition, String objectOrigin, int branchNumber) {
		return new DecisionAlternative_XLOAD_GETX_RefAliases(refToResolve, objectPosition, objectOrigin, branchNumber);
	}

	@Override
	public DecisionAlternative_XLOAD_GETX_RefExpands 
	createAlternativeRefExpands(ReferenceSymbolic refToResolve, String className, int branchNumber) {
		return new DecisionAlternative_XLOAD_GETX_RefExpands(refToResolve, className, branchNumber);
	}

	@Override
	public DecisionAlternative_XLOAD_GETX_RefNull 
	createAlternativeRefNull(ReferenceSymbolic refToResolve, int branchNumber) {
		return new DecisionAlternative_XLOAD_GETX_RefNull(refToResolve, branchNumber);
	}
}
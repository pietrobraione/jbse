package jbse.dec;

import jbse.tree.DecisionAlternativeLFLoadRefAliases;
import jbse.tree.DecisionAlternativeLFLoadRefExpands;
import jbse.tree.DecisionAlternativeLFLoadRefNull;
import jbse.val.ReferenceSymbolic;

/**
 * A Concrete Factory for decision alternatives used when the reference
 * that must be resolved comes from a local variable.
 *  
 * @author Pietro Braione
 *
 */
class DecisionAlternativeReferenceFromLocalVariableFactory 
implements DecisionAlternativeReferenceFactory<DecisionAlternativeLFLoadRefAliases, 
DecisionAlternativeLFLoadRefExpands, DecisionAlternativeLFLoadRefNull> {
	@Override
	public DecisionAlternativeLFLoadRefAliases 
	createAlternativeRefAliases(ReferenceSymbolic refToResolve, long objectPosition, String objectOrigin, int branchNumber) {
		return new DecisionAlternativeLFLoadRefAliases(refToResolve, objectPosition, objectOrigin, branchNumber);
	}

	@Override
	public DecisionAlternativeLFLoadRefExpands 
	createAlternativeRefExpands(ReferenceSymbolic refToResolve, String className, int branchNumber) {
		return new DecisionAlternativeLFLoadRefExpands(refToResolve, className, branchNumber);
	}

	@Override
	public DecisionAlternativeLFLoadRefNull 
	createAlternativeRefNull(ReferenceSymbolic refToResolve, int branchNumber) {
		return new DecisionAlternativeLFLoadRefNull(refToResolve, branchNumber);
	}
}
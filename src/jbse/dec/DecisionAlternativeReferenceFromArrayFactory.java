package jbse.dec;

import jbse.tree.DecisionAlternativeAloadRefAliases;
import jbse.tree.DecisionAlternativeAloadRefExpands;
import jbse.tree.DecisionAlternativeAloadRefNull;
import jbse.val.Expression;
import jbse.val.ReferenceSymbolic;

/**
 * A Concrete Factory for decision alternatives used when the reference
 * that must be resolved comes from an array.
 *  
 * @author Pietro Braione
 *
 */
public class DecisionAlternativeReferenceFromArrayFactory 
implements DecisionAlternativeReferenceFactory<DecisionAlternativeAloadRefAliases, 
DecisionAlternativeAloadRefExpands, DecisionAlternativeAloadRefNull> {
	private final Expression exp;
	
	public DecisionAlternativeReferenceFromArrayFactory(Expression exp) {
		this.exp = exp;
	}

	@Override
	public DecisionAlternativeAloadRefAliases 
	createAlternativeRefAliases(ReferenceSymbolic refToResolve, long objectPosition, String objectOrigin, int branchNumber) {
		return new DecisionAlternativeAloadRefAliases(exp, 
				refToResolve, objectPosition, objectOrigin, branchNumber);
	}

	@Override
	public DecisionAlternativeAloadRefExpands 
	createAlternativeRefExpands(ReferenceSymbolic refToResolve, String className, int branchNumber) {
		return new DecisionAlternativeAloadRefExpands(exp, 
				refToResolve, className, branchNumber);
	}

	@Override
	public DecisionAlternativeAloadRefNull 
	createAlternativeRefNull(ReferenceSymbolic refToResolve, int branchNumber) {
		return new DecisionAlternativeAloadRefNull(exp, 
				refToResolve, branchNumber);
	}
	
}
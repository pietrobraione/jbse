package jbse.dec;

import jbse.tree.DecisionAlternative_XALOAD_RefAliases;
import jbse.tree.DecisionAlternative_XALOAD_RefExpands;
import jbse.tree.DecisionAlternative_XALOAD_RefNull;
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
implements DecisionAlternativeReferenceFactory<DecisionAlternative_XALOAD_RefAliases, 
DecisionAlternative_XALOAD_RefExpands, DecisionAlternative_XALOAD_RefNull> {
	private final Expression exp;
	
	public DecisionAlternativeReferenceFromArrayFactory(Expression exp) {
		this.exp = exp;
	}

	@Override
	public DecisionAlternative_XALOAD_RefAliases 
	createAlternativeRefAliases(ReferenceSymbolic refToResolve, long objectPosition, String objectOrigin, int branchNumber) {
		return new DecisionAlternative_XALOAD_RefAliases(exp, 
				refToResolve, objectPosition, objectOrigin, branchNumber);
	}

	@Override
	public DecisionAlternative_XALOAD_RefExpands 
	createAlternativeRefExpands(ReferenceSymbolic refToResolve, String className, int branchNumber) {
		return new DecisionAlternative_XALOAD_RefExpands(exp, 
				refToResolve, className, branchNumber);
	}

	@Override
	public DecisionAlternative_XALOAD_RefNull 
	createAlternativeRefNull(ReferenceSymbolic refToResolve, int branchNumber) {
		return new DecisionAlternative_XALOAD_RefNull(exp, 
				refToResolve, branchNumber);
	}
	
}
package jbse.dec;

import jbse.tree.DecisionAlternative_XALOAD_Aliases;
import jbse.tree.DecisionAlternative_XALOAD_Expands;
import jbse.tree.DecisionAlternative_XALOAD_Null;
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
implements DecisionAlternativeReferenceFactory<DecisionAlternative_XALOAD_Aliases, 
DecisionAlternative_XALOAD_Expands, DecisionAlternative_XALOAD_Null> {
	private final Expression exp;
	
	public DecisionAlternativeReferenceFromArrayFactory(Expression exp) {
		this.exp = exp;
	}

	@Override
	public DecisionAlternative_XALOAD_Aliases 
	createAlternativeRefAliases(ReferenceSymbolic refToResolve, long objectPosition, String objectOrigin, int branchNumber) {
		return new DecisionAlternative_XALOAD_Aliases(exp, 
				refToResolve, objectPosition, objectOrigin, branchNumber);
	}

	@Override
	public DecisionAlternative_XALOAD_Expands 
	createAlternativeRefExpands(ReferenceSymbolic refToResolve, String className, int branchNumber) {
		return new DecisionAlternative_XALOAD_Expands(exp, 
				refToResolve, className, branchNumber);
	}

	@Override
	public DecisionAlternative_XALOAD_Null 
	createAlternativeRefNull(ReferenceSymbolic refToResolve, int branchNumber) {
		return new DecisionAlternative_XALOAD_Null(exp, 
				refToResolve, branchNumber);
	}
	
}
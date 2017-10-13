package jbse.dec;

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
	private final Expression exp;
	private final boolean fresh;
	private final Reference arrayToWriteBack;
	
	public DecisionAlternativeReferenceFactory_XALOAD(Expression exp, boolean fresh, Reference arrayToWriteBack) {
		this.exp = exp;
		this.fresh = fresh;
		this.arrayToWriteBack = arrayToWriteBack;
	}

	@Override
	public DecisionAlternative_XALOAD_Aliases 
	createAlternativeRefAliases(ReferenceSymbolic refToResolve, long objectPosition, MemoryPath objectOrigin, int branchNumber) {
		return new DecisionAlternative_XALOAD_Aliases(this.exp, this.fresh, this.arrayToWriteBack,
				refToResolve, objectPosition, objectOrigin, branchNumber);
	}

	@Override
	public DecisionAlternative_XALOAD_Expands 
	createAlternativeRefExpands(ReferenceSymbolic refToResolve, String className, int branchNumber) {
		return new DecisionAlternative_XALOAD_Expands(this.exp, this.fresh, this.arrayToWriteBack, 
				refToResolve, className, branchNumber);
	}

	@Override
	public DecisionAlternative_XALOAD_Null 
	createAlternativeRefNull(ReferenceSymbolic refToResolve, int branchNumber) {
		return new DecisionAlternative_XALOAD_Null(this.exp, this.fresh, this.arrayToWriteBack, 
				refToResolve, branchNumber);
	}
	
}
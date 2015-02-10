package jbse.tree;

import jbse.val.Expression;
import jbse.val.ReferenceSymbolic;

/**
 * {@link DecisionAlternative_XALOAD} for the cases of read accesses to an array
 * which return references.
 * 
 * @author Pietro Braione
 */
public abstract class DecisionAlternative_XALOAD_Ref extends DecisionAlternative_XALOAD implements DecisionAlternative_XYLOAD_GETX_Ref {
	private final ReferenceSymbolic valueToLoad;

	public DecisionAlternative_XALOAD_Ref(String branchId, Expression arrayAccessExpression, ReferenceSymbolic valueToLoad, int branchNumber) {
		super(branchId, arrayAccessExpression, branchNumber);
		this.valueToLoad = valueToLoad;
	}

	@Override
	public ReferenceSymbolic getValueToLoad() {
		return this.valueToLoad;
	}
	
	@Override
	public boolean concrete() {
		return false;
	}

	@Override
	public boolean trivial() {
		return this.concrete();
	}
}

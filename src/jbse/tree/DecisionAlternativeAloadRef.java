package jbse.tree;

import jbse.val.Expression;
import jbse.val.ReferenceSymbolic;

/**
 * {@link DecisionAlternativeAload} for the cases of read accesses to an array
 * which return references.
 * 
 * @author Pietro Braione
 */
public abstract class DecisionAlternativeAloadRef extends DecisionAlternativeAload implements DecisionAlternativeLoadRef {
	private final ReferenceSymbolic valueToLoad;

	public DecisionAlternativeAloadRef(String branchId, Expression arrayAccessExpression, ReferenceSymbolic valueToLoad, int branchNumber) {
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

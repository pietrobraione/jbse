package jbse.tree;

import jbse.mem.ReferenceSymbolic;

public abstract class DecisionAlternativeLFLoadRef extends DecisionAlternativeLFLoad implements DecisionAlternativeLoadRef {
	private final ReferenceSymbolic valueToLoad;

	public DecisionAlternativeLFLoadRef(String branchId, ReferenceSymbolic valueToLoad, int branchNumber) {
		super(branchId, branchNumber);
		this.valueToLoad = valueToLoad;
	}

	@Override
	public final ReferenceSymbolic getValueToLoad() {
		return this.valueToLoad;
	}

	@Override
	public final boolean concrete() {
		return false;
	}

	@Override
	public final boolean trivial() {
		return this.concrete();
	}
}

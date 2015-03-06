package jbse.tree;

import jbse.val.ReferenceSymbolic;

public abstract class DecisionAlternative_XLOAD_GETX_Unresolved 
extends DecisionAlternative_XLOAD_GETX implements DecisionAlternative_XYLOAD_GETX_Unresolved {
	private final ReferenceSymbolic valueToLoad;

	public DecisionAlternative_XLOAD_GETX_Unresolved(String branchId, ReferenceSymbolic valueToLoad, int branchNumber) {
		super(branchId, branchNumber);
		this.valueToLoad = valueToLoad;
	}

	@Override
	public final ReferenceSymbolic getValueToLoad() {
		return this.valueToLoad;
	}

    @Override
    public final boolean trivial() {
        return false;
    }

	@Override
	public final boolean concrete() {
		return false;
	}
}

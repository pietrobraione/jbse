package jbse.tree;

import jbse.val.ReferenceSymbolic;

/**
 * {@link DecisionAlternative_XLOAD_GETX} for the case a read access to a field
 * returned a {@link ReferenceSymbolic} to an object in the heap that has 
 * been discovered earlier in the symbolic execution.
 * 
 * @author Pietro Braione
 */
public final class DecisionAlternative_XLOAD_GETX_RefAliases extends DecisionAlternative_XLOAD_GETX_Ref implements DecisionAlternative_XYLOAD_GETX_RefAliases {
	private final long aliasPosition;

	public DecisionAlternative_XLOAD_GETX_RefAliases(ReferenceSymbolic referenceToResolve, long aliasPosition, String objectOrigin, int branchNumber) {
		super(ALT_CODE + "A" + objectOrigin, referenceToResolve, branchNumber);
		this.aliasPosition = aliasPosition;
	}

	@Override
	public long getAliasPosition() {
		return this.aliasPosition;
	}

	@Override
	public void accept(VisitorDecisionAlternative_XLOAD_GETX v) throws Exception {
		v.visitDecisionAlternative_XLOAD_GETX_RefAliases(this);
	}
}

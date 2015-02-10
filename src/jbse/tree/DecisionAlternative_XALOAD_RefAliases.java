package jbse.tree;

import jbse.val.Expression;
import jbse.val.ReferenceSymbolic;

/**
 * {@link DecisionAlternative_XALOAD} for the case a read access to an array
 * returned a {@link ReferenceSymbolic} to an object in the heap that has 
 * been discovered earlier in the symbolic execution.
 * 
 * @author Pietro Braione
 */
public final class DecisionAlternative_XALOAD_RefAliases extends DecisionAlternative_XALOAD_Ref implements DecisionAlternative_XYLOAD_GETX_RefAliases {
	private final long aliasPosition;

	public DecisionAlternative_XALOAD_RefAliases(Expression arrayAccessExpression, 
			ReferenceSymbolic referenceToResolve, long aliasPosition, String objectOrigin, int branchNumber) {
		super(ALT_CODE + "A" + objectOrigin, arrayAccessExpression, referenceToResolve, branchNumber);
		this.aliasPosition = aliasPosition;
	}

	@Override
	public long getAliasPosition() {
		return this.aliasPosition;
	}

	@Override
	public void accept(VisitorDecisionAlternative_XALOAD v) throws Exception {
		v.visitDecisionAlternative_XALOAD_RefAliases(this);
	}
	//TODO equals, hashCode, toString
}

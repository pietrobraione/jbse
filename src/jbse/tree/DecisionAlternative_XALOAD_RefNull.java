package jbse.tree;

import jbse.val.Expression;
import jbse.val.ReferenceSymbolic;

/**
 * {@link DecisionAlternative_XALOAD} for the case a read access to an array
 * returned a {@link ReferenceSymbolic} to null.
 * 
 * @author Pietro Braione
 */
public final class DecisionAlternative_XALOAD_RefNull extends DecisionAlternative_XALOAD_Ref implements DecisionAlternative_XYLOAD_GETX_RefNull {
	public DecisionAlternative_XALOAD_RefNull(Expression arrayAccessExpression, ReferenceSymbolic referenceToResolve, int branchNumber) {
		super(ALT_CODE + "N", arrayAccessExpression, referenceToResolve, branchNumber);
	}

	@Override
	public void accept(VisitorDecisionAlternative_XALOAD v) throws Exception {
		v.visitDecisionAlternative_XALOAD_RefNull(this);
	}
	//TODO equals, hashCode, toString
}

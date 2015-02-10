package jbse.tree;

import jbse.val.ReferenceSymbolic;

/**
 * {@link DecisionAlternative_XLOAD_GETX} for the case a read access to a field
 * returned a {@link ReferenceSymbolic} to null.
 * 
 * @author Pietro Braione
 */
public final class DecisionAlternative_XLOAD_GETX_RefNull extends DecisionAlternative_XLOAD_GETX_Ref implements DecisionAlternative_XYLOAD_GETX_RefNull {
	public DecisionAlternative_XLOAD_GETX_RefNull(ReferenceSymbolic referenceToResolve, int branchNumber) {
		super(ALT_CODE + "N", referenceToResolve, branchNumber);
	}

	@Override
	public void accept(VisitorDecisionAlternative_XLOAD_GETX v) throws Exception {
		v.visitDecisionAlternative_XLOAD_GETX_RefNull(this);
	}
}

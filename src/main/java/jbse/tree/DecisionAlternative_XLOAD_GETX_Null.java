package jbse.tree;

import jbse.val.ReferenceSymbolic;

/**
 * {@link DecisionAlternative_XLOAD_GETX} for the case a read access to a field
 * returned a {@link ReferenceSymbolic} to null.
 * 
 * @author Pietro Braione
 */
public final class DecisionAlternative_XLOAD_GETX_Null extends DecisionAlternative_XLOAD_GETX_Unresolved implements DecisionAlternative_XYLOAD_GETX_Null {
	public DecisionAlternative_XLOAD_GETX_Null(ReferenceSymbolic referenceToResolve, int branchNumber) {
		super(ALT_CODE + "_Null", referenceToResolve, branchNumber);
	}

	@Override
	public void accept(VisitorDecisionAlternative_XLOAD_GETX v) throws Exception {
		v.visitDecisionAlternative_XLOAD_GETX_Null(this);
	}
    
    //TODO toString, hashCode, equals
}

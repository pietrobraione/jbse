package jbse.tree;

import jbse.val.ReferenceSymbolic;

/**
 * {@link DecisionAlternative_XLOAD_GETX} for the case a read access to a field
 * returned a {@link ReferenceSymbolic} to an object in the heap that has
 * not been yet discovered during execution.
 * 
 * @author Pietro Braione
 */
public final class DecisionAlternative_XLOAD_GETX_Expands extends DecisionAlternative_XLOAD_GETX_Unresolved implements DecisionAlternative_XYLOAD_GETX_Expands {
	private final String classNameOfTargetObject;

	public DecisionAlternative_XLOAD_GETX_Expands(ReferenceSymbolic referenceToResolve, String classNameOfTargetObject, int branchNumber) {
		super(ALT_CODE + "_Expands:" + classNameOfTargetObject, referenceToResolve, branchNumber);
		this.classNameOfTargetObject = classNameOfTargetObject;
	}

	@Override
	public void accept(VisitorDecisionAlternative_XLOAD_GETX v) throws Exception {
		v.visitDecisionAlternative_XLOAD_GETX_Expands(this);
	}

	@Override
	public String getClassNameOfTargetObject() {
		return this.classNameOfTargetObject;
	}
}

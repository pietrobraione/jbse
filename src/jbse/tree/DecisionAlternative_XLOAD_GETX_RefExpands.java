package jbse.tree;

import jbse.val.ReferenceSymbolic;

/**
 * {@link DecisionAlternative_XLOAD_GETX} for the case a read access to a field
 * returned a {@link ReferenceSymbolic} to an object in the heap that has
 * not been yet discovered during execution.
 * 
 * @author Pietro Braione
 */
public final class DecisionAlternative_XLOAD_GETX_RefExpands extends DecisionAlternative_XLOAD_GETX_Ref implements DecisionAlternative_XYLOAD_GETX_RefExpands {
	private String classNameOfTargetObject;

	public DecisionAlternative_XLOAD_GETX_RefExpands(ReferenceSymbolic referenceToResolve, String classNameOfTargetObject, int branchNumber) {
		super(ALT_CODE + "X" + classNameOfTargetObject, referenceToResolve, branchNumber);
		this.classNameOfTargetObject = classNameOfTargetObject;
	}

	@Override
	public void accept(VisitorDecisionAlternative_XLOAD_GETX v) throws Exception {
		v.visitDecisionAlternative_XLOAD_GETX_RefExpands(this);
	}

	@Override
	public String getClassNameOfTargetObject() {
		return this.classNameOfTargetObject;
	}
}

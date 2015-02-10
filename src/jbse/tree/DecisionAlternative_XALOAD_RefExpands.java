package jbse.tree;

import jbse.val.Expression;
import jbse.val.ReferenceSymbolic;

/**
 * {@link DecisionAlternative_XALOAD} for the case a read access to an array
 * returned a {@link ReferenceSymbolic} to an object in the heap that has
 * not been yet discovered during execution.
 * 
 * @author Pietro Braione
 */
public final class DecisionAlternative_XALOAD_RefExpands extends DecisionAlternative_XALOAD_Ref implements DecisionAlternative_XYLOAD_GETX_RefExpands {
	private final String classNameOfTargetObject;

	public DecisionAlternative_XALOAD_RefExpands(Expression arrayAccessExpression, ReferenceSymbolic referenceToResolve, String classNameOfTargetObject, int branchNumber) {
		super(ALT_CODE + "X" + classNameOfTargetObject, arrayAccessExpression, referenceToResolve, branchNumber);
		this.classNameOfTargetObject = classNameOfTargetObject;
	}

	@Override
	public String getClassNameOfTargetObject() {
		return this.classNameOfTargetObject;
	}

	@Override
	public void accept(VisitorDecisionAlternative_XALOAD v) throws Exception {
		v.visitDecisionAlternative_XALOAD_RefExpands(this);
	}
	//TODO equals, hashCode, toString
}

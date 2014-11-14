package jbse.tree;

import jbse.val.Expression;
import jbse.val.ReferenceSymbolic;

/**
 * {@link DecisionAlternativeAload} for the case a read access to an array
 * returned a {@link ReferenceSymbolic} to an object in the heap that has
 * not been yet discovered during execution.
 * 
 * @author Pietro Braione
 */
public class DecisionAlternativeAloadRefExpands extends DecisionAlternativeAloadRef implements DecisionAlternativeLoadRefExpands {
	private final String classNameOfTargetObject;

	public DecisionAlternativeAloadRefExpands(Expression arrayAccessExpression, ReferenceSymbolic referenceToResolve, String classNameOfTargetObject, int branchNumber) {
		super(ALT_CODE + "X" + classNameOfTargetObject, arrayAccessExpression, referenceToResolve, branchNumber);
		this.classNameOfTargetObject = classNameOfTargetObject;
	}

	@Override
	public String getClassNameOfTargetObject() {
		return this.classNameOfTargetObject;
	}

	@Override
	public void accept(DecisionAlternativeAloadVisitor v) throws Exception {
		v.visitDecisionAlternativeAloadRefExpands(this);
	}
	//TODO equals, hashCode, toString
}

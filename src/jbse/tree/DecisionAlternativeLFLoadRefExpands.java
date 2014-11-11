package jbse.tree;

import jbse.mem.ReferenceSymbolic;

/**
 * {@link DecisionAlternativeLFLoad} for the case a read access to a field
 * returned a {@link ReferenceSymbolic} to an object in the heap that has
 * not been yet discovered during execution.
 * 
 * @author Pietro Braione
 */
public class DecisionAlternativeLFLoadRefExpands extends DecisionAlternativeLFLoadRef implements DecisionAlternativeLoadRefExpands {
	private String classNameOfTargetObject;

	public DecisionAlternativeLFLoadRefExpands(ReferenceSymbolic referenceToResolve, String classNameOfTargetObject, int branchNumber) {
		super(ALT_CODE + "X" + classNameOfTargetObject, referenceToResolve, branchNumber);
		this.classNameOfTargetObject = classNameOfTargetObject;
	}

	@Override
	public void accept(DecisionAlternativeLFLoadVisitor v) throws Exception {
		v.visitDecisionAlternativeLFLoadRefExpands(this);
	}

	@Override
	public String getClassNameOfTargetObject() {
		return this.classNameOfTargetObject;
	}
}

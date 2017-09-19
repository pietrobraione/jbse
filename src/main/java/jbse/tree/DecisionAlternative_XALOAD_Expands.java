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
public final class DecisionAlternative_XALOAD_Expands extends DecisionAlternative_XALOAD_Unresolved implements DecisionAlternative_XYLOAD_GETX_Expands {
	private final String classNameOfTargetObject;
	private final int hashCode;

	public DecisionAlternative_XALOAD_Expands(Expression arrayAccessExpression, ReferenceSymbolic referenceToResolve, String classNameOfTargetObject, int branchNumber) {
		super(ALT_CODE + "_Expands:" + arrayAccessExpression + ":" + classNameOfTargetObject, arrayAccessExpression, referenceToResolve, branchNumber);
		this.classNameOfTargetObject = classNameOfTargetObject;
        final int prime = 829;
        int result = super.hashCode();
        result = prime * result +
            ((this.classNameOfTargetObject == null)
            ? 0
            : this.classNameOfTargetObject.hashCode());
        this.hashCode = result;
	}

	@Override
	public String getClassNameOfTargetObject() {
		return this.classNameOfTargetObject;
	}

	@Override
	public void accept(VisitorDecisionAlternative_XALOAD v) throws Exception {
		v.visitDecisionAlternative_XALOAD_Expands(this);
	}

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        final DecisionAlternative_XALOAD_Expands other =
            (DecisionAlternative_XALOAD_Expands) obj;
        if (this.classNameOfTargetObject == null) {
            if (other.classNameOfTargetObject != null) {
                return false;
            }
        } else if (!this.classNameOfTargetObject
        .equals(other.classNameOfTargetObject)) {
            return false;
        }
        return true;
    }
}

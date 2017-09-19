package jbse.tree;

import jbse.val.Expression;
import jbse.val.ReferenceSymbolic;

/**
 * {@link DecisionAlternative_XALOAD} for the case a read access to an array
 * returned a {@link ReferenceSymbolic} to null.
 * 
 * @author Pietro Braione
 */
public final class DecisionAlternative_XALOAD_Null extends DecisionAlternative_XALOAD_Unresolved implements DecisionAlternative_XYLOAD_GETX_Null {
    private final int hashCode;
    
	public DecisionAlternative_XALOAD_Null(Expression arrayAccessExpression, ReferenceSymbolic referenceToResolve, int branchNumber) {
		super(ALT_CODE + "_Null:" + arrayAccessExpression, arrayAccessExpression, referenceToResolve, branchNumber);
        final int prime = 3331;
        int result = super.hashCode();
        result = prime * result;
        this.hashCode = result;
	}

	@Override
	public void accept(VisitorDecisionAlternative_XALOAD v) throws Exception {
		v.visitDecisionAlternative_XALOAD_Null(this);
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
        return true;
    }       
}

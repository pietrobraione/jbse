package jbse.tree;

import jbse.val.ReferenceSymbolic;

/**
 * {@link DecisionAlternative_XLOAD_GETX} for the case a read access to a field/variable
 * returned a {@link ReferenceSymbolic} to null.
 * 
 * @author Pietro Braione
 */
public final class DecisionAlternative_XLOAD_GETX_Null extends DecisionAlternative_XLOAD_GETX_Unresolved implements DecisionAlternative_XYLOAD_GETX_Null {
    private final int hashCode;

    /**
     * Constructor.
     * 
     * @param referenceToResolve the {@link ReferenceSymbolic} loaded from the field/variable.
     * @param branchNumber an {@code int}, the branch number.
     */
    public DecisionAlternative_XLOAD_GETX_Null(ReferenceSymbolic referenceToResolve, int branchNumber) {
        super(ALT_CODE + "_Null", referenceToResolve, branchNumber);
        final int prime = 6079;
        int result = super.hashCode();
        result = prime * result;
        this.hashCode = result;
    }

    @Override
    public void accept(VisitorDecisionAlternative_XLOAD_GETX v) throws Exception {
        v.visitDecisionAlternative_XLOAD_GETX_Null(this);
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        return true;
    }       
}

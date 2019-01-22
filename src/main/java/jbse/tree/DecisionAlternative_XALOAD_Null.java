package jbse.tree;

import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.ReferenceSymbolic;

/**
 * {@link DecisionAlternative_XALOAD} for the case a read access to an array
 * returned a {@link ReferenceSymbolic} to null.
 * 
 * @author Pietro Braione
 */
public final class DecisionAlternative_XALOAD_Null extends DecisionAlternative_XALOAD_Unresolved implements DecisionAlternative_XYLOAD_GETX_Null {
    private final int hashCode;

    /**
     * Constructor.
     * 
     * @param arrayAccessExpression the array access {@link Primitive}.
     * @param referenceToResolve the {@link ReferenceSymbolic} loaded from the array.
     * @param fresh {@code true} iff {@code referenceToResolve} is fresh, i.e., 
     *        is not stored in the array and, therefore, must be written
     *        back to the array.
     * @param arrayReference when {@code fresh == true} is a {@link Reference} to the array 
     *        where {@code referenceToResolve} originates from.
     * @param branchNumber an {@code int}, the branch number.
     */
    public DecisionAlternative_XALOAD_Null(Primitive arrayAccessExpression, ReferenceSymbolic referenceToResolve, boolean fresh, Reference arrayReference, int branchNumber) {
        super(ALT_CODE + "_Null:" + arrayAccessExpression, arrayAccessExpression, referenceToResolve, fresh, arrayReference, branchNumber);
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
        if (!super.equals(obj)) {
            return false;
        }
        return true;
    }       
}

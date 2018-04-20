package jbse.tree;

import jbse.val.ReferenceSymbolic;

/**
 * {@link DecisionAlternative_XLOAD_GETX} for the case a read access to a field/variable
 * returned a {@link ReferenceSymbolic} to an object in the heap that has 
 * been discovered earlier in the symbolic execution.
 * 
 * @author Pietro Braione
 */
public final class DecisionAlternative_XLOAD_GETX_Aliases extends DecisionAlternative_XLOAD_GETX_Unresolved implements DecisionAlternative_XYLOAD_GETX_Aliases {
    private final long objectPosition;
    private final int hashCode;

    /**
     * Constructor.
     * 
     * @param referenceToResolve the {@link ReferenceSymbolic} loaded from the field/variable.
     * @param objectPosition a {@code long}, the position in the heap of the object
     *        {@code referenceToResolve} refers to.
     * @param objectOrigin a {@link ReferenceSymbolic}, the origin of the object {@code referenceToResolve} 
     *        refers to.
     * @param branchNumber an {@code int}, the branch number.
     */
    public DecisionAlternative_XLOAD_GETX_Aliases(ReferenceSymbolic referenceToResolve, long objectPosition, ReferenceSymbolic objectOrigin, int branchNumber) {
        super(ALT_CODE + "_Aliases:" + objectOrigin.asOriginString(), referenceToResolve, branchNumber);
        this.objectPosition = objectPosition;
        final int prime = 7573;
        int result = super.hashCode();
        result = prime * result +
            (int) (this.objectPosition ^ (this.objectPosition >>> 32));
        this.hashCode = result;
    }

    @Override
    public long getObjectPosition() {
        return this.objectPosition;
    }

    @Override
    public void accept(VisitorDecisionAlternative_XLOAD_GETX v) throws Exception {
        v.visitDecisionAlternative_XLOAD_GETX_Aliases(this);
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
        final DecisionAlternative_XLOAD_GETX_Aliases other = (DecisionAlternative_XLOAD_GETX_Aliases) obj;
        if (this.objectPosition != other.objectPosition) {
            return false;
        }
        return true;
    }
}

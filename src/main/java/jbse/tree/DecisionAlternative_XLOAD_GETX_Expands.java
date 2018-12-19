package jbse.tree;

import jbse.bc.ClassFile;
import jbse.val.ReferenceSymbolic;

/**
 * {@link DecisionAlternative_XLOAD_GETX} for the case a read access to a field/variable
 * returned a {@link ReferenceSymbolic} to an object in the heap that has
 * not been yet discovered during execution.
 * 
 * @author Pietro Braione
 */
public final class DecisionAlternative_XLOAD_GETX_Expands extends DecisionAlternative_XLOAD_GETX_Unresolved implements DecisionAlternative_XYLOAD_GETX_Expands {
    private final ClassFile classFileOfTargetObject;
    private final int hashCode;

    /**
     * Constructor.
     * 
     * @param referenceToResolve the {@link ReferenceSymbolic} loaded from the field/variable.
     * @param classFileOfTargetObject the {@link ClassFile} for the class of the
     *        object {@code referenceToResolve} expands to.
     * @param branchNumber an {@code int}, the branch number.
     */
    public DecisionAlternative_XLOAD_GETX_Expands(ReferenceSymbolic referenceToResolve, ClassFile classFileOfTargetObject, int branchNumber) {
        super(ALT_CODE + "_Expands:" + classFileOfTargetObject.getClassName(), referenceToResolve, branchNumber);
        this.classFileOfTargetObject = classFileOfTargetObject;
        final int prime = 2819;
        int result = 1;
        result = prime * result + 
            ((this.classFileOfTargetObject == null) ? 0 : this.classFileOfTargetObject.hashCode());
        this.hashCode = result;
    }

    @Override
    public void accept(VisitorDecisionAlternative_XLOAD_GETX v) throws Exception {
        v.visitDecisionAlternative_XLOAD_GETX_Expands(this);
    }

    @Override
    public ClassFile getClassFileOfTargetObject() {
        return this.classFileOfTargetObject;
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
        final DecisionAlternative_XLOAD_GETX_Expands other = (DecisionAlternative_XLOAD_GETX_Expands) obj;
        if (this.classFileOfTargetObject == null) {
            if (other.classFileOfTargetObject != null) {
                return false;
            }
        } else if (!this.classFileOfTargetObject.equals(other.classFileOfTargetObject)) {
            return false;
        }
        return true;
    }
}

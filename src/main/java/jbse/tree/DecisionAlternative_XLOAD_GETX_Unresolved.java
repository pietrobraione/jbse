package jbse.tree;

import jbse.val.ReferenceSymbolic;

public abstract class DecisionAlternative_XLOAD_GETX_Unresolved 
extends DecisionAlternative_XLOAD_GETX implements DecisionAlternative_XYLOAD_GETX_Unresolved {
    private final ReferenceSymbolic valueToLoad;
    private final int hashCode;

    public DecisionAlternative_XLOAD_GETX_Unresolved(String branchId, ReferenceSymbolic valueToLoad, int branchNumber) {
        super(branchId, branchNumber);
        this.valueToLoad = valueToLoad;
        final int prime = 5003;
        int result = 1;
        result = prime * result + 
            ((this.valueToLoad == null) ? 0 : this.valueToLoad.hashCode());
        this.hashCode = result;
    }

    @Override
    public final ReferenceSymbolic getValueToLoad() {
        return this.valueToLoad;
    }

    @Override
    public final boolean trivial() {
        return false;
    }

    @Override
    public final boolean concrete() {
        return false;
    }
    
    @Override
    public int hashCode() {
        return this.hashCode;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DecisionAlternative_XLOAD_GETX_Unresolved other = (DecisionAlternative_XLOAD_GETX_Unresolved) obj;
        if (this.valueToLoad == null) {
            if (other.valueToLoad != null) {
                return false;
            }
        } else if (!this.valueToLoad.equals(other.valueToLoad)) {
            return false;
        }
        return true;
    }
}

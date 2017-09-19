package jbse.tree;

import jbse.val.Expression;
import jbse.val.ReferenceSymbolic;

/**
 * {@link DecisionAlternative_XALOAD} for the case where
 * the value loaded on the operand stack is an unresolved 
 * symbolic reference.
 * 
 * @author Pietro Braione
 */
public abstract class DecisionAlternative_XALOAD_Unresolved 
extends DecisionAlternative_XALOAD_In implements DecisionAlternative_XYLOAD_GETX_Unresolved {
	private final ReferenceSymbolic valueToLoad;
	private final int hashCode;

	public DecisionAlternative_XALOAD_Unresolved(String branchId, Expression arrayAccessExpression, ReferenceSymbolic valueToLoad, int branchNumber) {
		super(branchId, arrayAccessExpression, branchNumber);
		this.valueToLoad = valueToLoad;
        final int prime = 683;
        int result = super.hashCode();
        result = prime * result +
            ((this.valueToLoad == null) ? 0 : this.valueToLoad.hashCode());
        this.hashCode = result;
	}

	@Override
	public ReferenceSymbolic getValueToLoad() {
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
        if (!super.equals(obj)) {
            return false;
        }
        final DecisionAlternative_XALOAD_Unresolved other =
            (DecisionAlternative_XALOAD_Unresolved) obj;
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

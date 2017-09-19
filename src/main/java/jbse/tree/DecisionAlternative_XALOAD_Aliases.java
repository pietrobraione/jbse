package jbse.tree;

import jbse.val.Expression;
import jbse.val.MemoryPath;
import jbse.val.ReferenceSymbolic;

/**
 * {@link DecisionAlternative_XALOAD} for the case a read access to an array
 * returned a {@link ReferenceSymbolic} to an object in the heap that has 
 * been discovered earlier in the symbolic execution.
 * 
 * @author Pietro Braione
 */
public final class DecisionAlternative_XALOAD_Aliases 
extends DecisionAlternative_XALOAD_Unresolved implements DecisionAlternative_XYLOAD_GETX_Aliases {
	private final long aliasPosition;
	private final int hashCode;

	public DecisionAlternative_XALOAD_Aliases(Expression arrayAccessExpression, 
			ReferenceSymbolic referenceToResolve, long aliasPosition, MemoryPath objectOrigin, int branchNumber) {
		super(ALT_CODE + "_Aliases:" + arrayAccessExpression + ":" + objectOrigin.toString(), arrayAccessExpression, referenceToResolve, branchNumber);
		this.aliasPosition = aliasPosition;
        final int prime = 1733;
        int result = super.hashCode();
        result = prime * result +
            (int) (this.aliasPosition ^ (this.aliasPosition >>> 32));
		this.hashCode = result;
	}

	@Override
	public long getAliasPosition() {
		return this.aliasPosition;
	}

	@Override
	public void accept(VisitorDecisionAlternative_XALOAD v) throws Exception {
		v.visitDecisionAlternative_XALOAD_Aliases(this);
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
        final DecisionAlternative_XALOAD_Aliases other =
            (DecisionAlternative_XALOAD_Aliases) obj;
        if (this.aliasPosition != other.aliasPosition) {
            return false;
        }
        return true;
    }
}

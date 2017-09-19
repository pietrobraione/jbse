package jbse.tree;

import jbse.val.MemoryPath;
import jbse.val.ReferenceSymbolic;

/**
 * {@link DecisionAlternative_XLOAD_GETX} for the case a read access to a field
 * returned a {@link ReferenceSymbolic} to an object in the heap that has 
 * been discovered earlier in the symbolic execution.
 * 
 * @author Pietro Braione
 */
public final class DecisionAlternative_XLOAD_GETX_Aliases extends DecisionAlternative_XLOAD_GETX_Unresolved implements DecisionAlternative_XYLOAD_GETX_Aliases {
	private final long aliasPosition;

	public DecisionAlternative_XLOAD_GETX_Aliases(ReferenceSymbolic referenceToResolve, long aliasPosition, MemoryPath objectOrigin, int branchNumber) {
		super(ALT_CODE + "_Aliases:" + objectOrigin.toString(), referenceToResolve, branchNumber);
		this.aliasPosition = aliasPosition;
	}

	@Override
	public long getAliasPosition() {
		return this.aliasPosition;
	}

	@Override
	public void accept(VisitorDecisionAlternative_XLOAD_GETX v) throws Exception {
		v.visitDecisionAlternative_XLOAD_GETX_Aliases(this);
	}
    
    //TODO toString, hashCode, equals
}

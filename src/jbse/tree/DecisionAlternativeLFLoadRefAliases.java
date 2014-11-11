package jbse.tree;

import jbse.mem.ReferenceSymbolic;

/**
 * {@link DecisionAlternativeLFLoad} for the case a read access to a field
 * returned a {@link ReferenceSymbolic} to an object in the heap that has 
 * been discovered earlier in the symbolic execution.
 * 
 * @author Pietro Braione
 */
public class DecisionAlternativeLFLoadRefAliases extends DecisionAlternativeLFLoadRef implements DecisionAlternativeLoadRefAliases {
	private final long aliasPosition;

	public DecisionAlternativeLFLoadRefAliases(ReferenceSymbolic referenceToResolve, long aliasPosition, String objectOrigin, int branchNumber) {
		super(ALT_CODE + "A" + objectOrigin, referenceToResolve, branchNumber);
		this.aliasPosition = aliasPosition;
	}

	@Override
	public long getAliasPosition() {
		return this.aliasPosition;
	}

	@Override
	public void accept(DecisionAlternativeLFLoadVisitor v) throws Exception {
		v.visitDecisionAlternativeLFLoadRefAliases(this);
	}
}

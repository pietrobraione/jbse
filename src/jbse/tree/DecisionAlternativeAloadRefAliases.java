package jbse.tree;

import jbse.mem.Expression;
import jbse.mem.ReferenceSymbolic;

/**
 * {@link DecisionAlternativeAload} for the case a read access to an array
 * returned a {@link ReferenceSymbolic} to an object in the heap that has 
 * been discovered earlier in the symbolic execution.
 * 
 * @author Pietro Braione
 */
public class DecisionAlternativeAloadRefAliases extends DecisionAlternativeAloadRef implements DecisionAlternativeLoadRefAliases {
	private final long aliasPosition;

	public DecisionAlternativeAloadRefAliases(Expression arrayAccessExpression, 
			ReferenceSymbolic referenceToResolve, long aliasPosition, String objectOrigin, int branchNumber) {
		super(ALT_CODE + "A" + objectOrigin, arrayAccessExpression, referenceToResolve, branchNumber);
		this.aliasPosition = aliasPosition;
	}

	@Override
	public long getAliasPosition() {
		return this.aliasPosition;
	}

	@Override
	public void accept(DecisionAlternativeAloadVisitor v) throws Exception {
		v.visitDecisionAlternativeAloadRefAliases(this);
	}
	//TODO equals, hashCode, toString
}

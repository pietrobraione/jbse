package jbse.tree;

import jbse.mem.Expression;
import jbse.mem.ReferenceSymbolic;

/**
 * {@link DecisionAlternativeAload} for the case a read access to an array
 * returned a {@link ReferenceSymbolic} to null.
 * 
 * @author Pietro Braione
 */
public class DecisionAlternativeAloadRefNull extends DecisionAlternativeAloadRef implements DecisionAlternativeLoadRefNull {
	public DecisionAlternativeAloadRefNull(Expression arrayAccessExpression, ReferenceSymbolic referenceToResolve, int branchNumber) {
		super(ALT_CODE + "N", arrayAccessExpression, referenceToResolve, branchNumber);
	}

	@Override
	public void accept(DecisionAlternativeAloadVisitor v) throws Exception {
		v.visitDecisionAlternativeAloadRefNull(this);
	}
	//TODO equals, hashCode, toString
}

package jbse.tree;

import jbse.val.ReferenceSymbolic;

/**
 * {@link DecisionAlternativeLFLoad} for the case a read access to a field
 * returned a {@link ReferenceSymbolic} to null.
 * 
 * @author Pietro Braione
 */
public class DecisionAlternativeLFLoadRefNull extends DecisionAlternativeLFLoadRef implements DecisionAlternativeLoadRefNull {
	public DecisionAlternativeLFLoadRefNull(ReferenceSymbolic referenceToResolve, int branchNumber) {
		super(ALT_CODE + "N", referenceToResolve, branchNumber);
	}

	@Override
	public void accept(DecisionAlternativeLFLoadVisitor v) throws Exception {
		v.visitDecisionAlternativeLFLoadRefNull(this);
	}
}

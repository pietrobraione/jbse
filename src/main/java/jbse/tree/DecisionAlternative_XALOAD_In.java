package jbse.tree;

import jbse.val.Expression;

/**
 * {@link DecisionAlternative_XALOAD} for the case a read access to an array
 * was inbounds.
 * 
 * @author Pietro Braione
 */
public abstract class DecisionAlternative_XALOAD_In 
extends DecisionAlternative_XALOAD implements DecisionAlternative_XYLOAD_GETX_Loads {
	protected DecisionAlternative_XALOAD_In(String branchId, Expression arrayAccessExpression, int branchNumber) {
		super(branchId, arrayAccessExpression, branchNumber);
	}
}

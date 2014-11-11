package jbse.tree;

import jbse.mem.Expression;
import jbse.mem.Value;

public abstract class DecisionAlternativeAloadNoResolution extends DecisionAlternativeAload {
	protected DecisionAlternativeAloadNoResolution(String branchId, Expression arrayAccessExpression, int branchNumber) {
		super(branchId, arrayAccessExpression, branchNumber);
	}
	
	public abstract boolean ok();

	@Deprecated
	public abstract DecisionAlternativeAload mkNonconcrete(Expression accessExpression, Value valueToLoad, boolean fresh);
}

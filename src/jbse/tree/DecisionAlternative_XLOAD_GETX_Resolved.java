package jbse.tree;

import jbse.mem.Util;
import jbse.val.ReferenceSymbolic;
import jbse.val.Value;

/**
 * {@link DecisionAlternative_XLOAD_GETX} for the case a read access to a field
 * returned a resolved {@link ReferenceSymbolic} or a nonreference (either symbolic 
 * or concrete) value.
 * 
 * @author Pietro Braione
 */
public final class DecisionAlternative_XLOAD_GETX_Resolved extends DecisionAlternative_XLOAD_GETX implements DecisionAlternative_XYLOAD_GETX_Resolved {
	private final static int RES_BN = 1;
	private final Value valueToLoad;
	private final boolean isConcrete;
	
	public DecisionAlternative_XLOAD_GETX_Resolved(Value valueToLoad) {
		super(ALT_CODE + "R", RES_BN);
		this.valueToLoad = valueToLoad;
		this.isConcrete = !Util.isSymbolicReference(valueToLoad);
	}

	@Override
	public Value getValueToLoad() {
		return this.valueToLoad;
	}

	@Override
	public void accept(VisitorDecisionAlternative_XLOAD_GETX v) throws Exception {
		v.visitDecisionAlternative_XLOAD_GETX_Resolved(this);
	}

	@Override
	public boolean concrete() {
		return this.isConcrete;
	}

	@Override
	public boolean trivial() {
		return true;
	}
}

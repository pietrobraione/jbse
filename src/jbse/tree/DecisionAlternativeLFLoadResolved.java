package jbse.tree;

import jbse.mem.ReferenceSymbolic;
import jbse.mem.Util;
import jbse.mem.Value;

/**
 * {@link DecisionAlternativeLFLoad} for the case a read access to a field
 * returned a resolved {@link ReferenceSymbolic} or a nonreference (either symbolic 
 * or concrete) value.
 * 
 * @author Pietro Braione
 */
public class DecisionAlternativeLFLoadResolved extends DecisionAlternativeLFLoad implements DecisionAlternativeLoadResolved {
	private final static int RES_BN = 1;
	private final Value valueToLoad;
	private final boolean isConcrete;
	
	public DecisionAlternativeLFLoadResolved(Value valueToLoad) {
		super(ALT_CODE + "R", RES_BN);
		this.valueToLoad = valueToLoad;
		this.isConcrete = !Util.isSymbolicReference(valueToLoad);
	}

	@Override
	public Value getValueToLoad() {
		return this.valueToLoad;
	}

	@Override
	public void accept(DecisionAlternativeLFLoadVisitor v) throws Exception {
		v.visitDecisionAlternativeLFLoadResolved(this);
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

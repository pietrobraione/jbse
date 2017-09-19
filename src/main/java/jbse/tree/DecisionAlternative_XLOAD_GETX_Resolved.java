package jbse.tree;

import jbse.mem.Util;
import jbse.val.Value;

/**
 * {@link DecisionAlternative_XLOAD_GETX} for the case where
 * the value loaded on the operand stack is a previously resolved 
 * symbolic reference or a primitive (either symbolic or concrete) 
 * value.
 * 
 * @author Pietro Braione
 */
public final class DecisionAlternative_XLOAD_GETX_Resolved 
extends DecisionAlternative_XLOAD_GETX implements DecisionAlternative_XYLOAD_GETX_Resolved {
    private static final int HASH_CODE = 1;
	private final Value valueToLoad;
	private final boolean isConcrete;
	
	public DecisionAlternative_XLOAD_GETX_Resolved(Value valueToLoad) {
		super(ALT_CODE + "_Resolved", HASH_CODE);
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
    public boolean trivial() {
        return true;
    }

	@Override
	public boolean concrete() {
		return this.isConcrete;
	}

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        return HASH_CODE;
    }
    
    @Override
    public String toString() {
        return getIdentifier();
    }
}

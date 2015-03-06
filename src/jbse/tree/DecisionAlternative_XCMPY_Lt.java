package jbse.tree;

import jbse.val.Operator;

public final class DecisionAlternative_XCMPY_Lt extends DecisionAlternative_XCMPY {
    private static final String LT_ID = "XCMPY_Lt";
	private static final int HASH_CODE = 3;
	
	DecisionAlternative_XCMPY_Lt(boolean isConcrete) {
		super(-1, Operator.LT, isConcrete, (isConcrete ? 1 : HASH_CODE));
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
		return LT_ID;
	}
}
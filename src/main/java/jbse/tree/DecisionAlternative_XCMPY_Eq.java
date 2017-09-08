package jbse.tree;

import jbse.val.Operator;

public final class DecisionAlternative_XCMPY_Eq extends DecisionAlternative_XCMPY {
	private static final String EQ_ID = "XCMPY_Eq";
	private static final int HASH_CODE = 2;
	
	public DecisionAlternative_XCMPY_Eq(boolean isConcrete) {
		super(0, Operator.EQ, isConcrete, (isConcrete ? 1 : HASH_CODE));
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
		return EQ_ID;
	}
}
package jbse.tree;

import jbse.val.Operator;

public final class DecisionAlternative_XCMPY_Gt extends DecisionAlternative_XCMPY {
	private static final String GT_ID = "GT";
	private static final int GT_BN = 1;
	private static final int HASH_CODE = 256204547;

	public DecisionAlternative_XCMPY_Gt(boolean isConcrete) {
		super(1, Operator.GT, isConcrete, GT_BN);
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
		return GT_ID;
	}
}
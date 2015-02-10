package jbse.tree;

import jbse.val.Operator;

public final class DecisionAlternative_XCMPY_Eq extends DecisionAlternative_XCMPY {
	private static final String EQ_ID = "EQ";
	private static final int EQ_BN = 2;
	private static final int HASH_CODE = 633912551;
	
	public DecisionAlternative_XCMPY_Eq(boolean isConcrete) {
		super(0, Operator.EQ, isConcrete, (isConcrete ? 1 : EQ_BN));
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
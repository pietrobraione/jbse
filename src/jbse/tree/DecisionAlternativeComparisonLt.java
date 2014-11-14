package jbse.tree;

import jbse.val.Operator;

public final class DecisionAlternativeComparisonLt extends DecisionAlternativeComparison {
    private static final String LT_ID = "LT";
	private static final int LT_BN = 3;
	private static final int HASH_CODE = 101663;
	
	DecisionAlternativeComparisonLt(boolean isConcrete) {
		super(-1, Operator.LT, isConcrete, (isConcrete ? 1 : LT_BN));
	}
	
	@Override
	public DecisionAlternativeComparisonLt mkNonconcrete() {
		return new DecisionAlternativeComparisonLt(false);
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
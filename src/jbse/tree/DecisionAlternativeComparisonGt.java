package jbse.tree;

import jbse.mem.Operator;

public final class DecisionAlternativeComparisonGt extends DecisionAlternativeComparison {
	private static final String GT_ID = "GT";
	private static final int GT_BN = 1;
	private static final int HASH_CODE = 256204547;

	public DecisionAlternativeComparisonGt(boolean isConcrete) {
		super(1, Operator.GT, isConcrete, GT_BN);
	}
	
	@Override
	public DecisionAlternativeComparisonGt mkNonconcrete() {
		return new DecisionAlternativeComparisonGt(false);
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
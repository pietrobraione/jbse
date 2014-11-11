package jbse.tree;

/**
 * True, from check of nonconcrete values.
 */
public final class DecisionAlternativeIfTrue extends DecisionAlternativeIf {
	private static final String T_ID = "T";
	private static final int T_BN = 1;
	private static final int HASH_CODE = 314652131;
	
	DecisionAlternativeIfTrue(boolean isConcrete) {
		super(true, isConcrete, T_BN);
	}
	
	@Override
	public DecisionAlternativeIfTrue mkNonconcrete() {
		return new DecisionAlternativeIfTrue(false);
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
		return T_ID;
	}
}
package jbse.tree;

/**
 * False, from check of nonconcrete values.
 */
public final class DecisionAlternativeIfFalse extends DecisionAlternativeIf {
	private static final String F_ID = "F";
	private static final int F_BN = 2;
	private static final int HASH_CODE = 838041647;
	
	DecisionAlternativeIfFalse(boolean isConcrete) {
		super(false, isConcrete, (isConcrete ? 1 : F_BN));
	}
	
	@Override
	public DecisionAlternativeIfFalse mkNonconcrete() {
		return new DecisionAlternativeIfFalse(false);
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
		return F_ID;
	}
}
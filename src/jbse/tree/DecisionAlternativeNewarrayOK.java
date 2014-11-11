package jbse.tree;

public final class DecisionAlternativeNewarrayOK extends DecisionAlternativeNewarray {
	private static final String OK_ID = "T";
	private static final int OK_BN = 1;
	private static final int HASH_CODE = 447453271;
	
	DecisionAlternativeNewarrayOK(boolean isConcrete) {
		super(isConcrete, OK_BN);
	}
	
	@Override
	public boolean ok() {
		return true;
	}

	@Override
	public DecisionAlternativeNewarrayOK mkNonconcrete() {
		return new DecisionAlternativeNewarrayOK(false);
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
		return OK_ID;
	}
}

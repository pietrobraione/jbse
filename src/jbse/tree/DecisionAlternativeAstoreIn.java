package jbse.tree;

final class DecisionAlternativeAstoreIn extends DecisionAlternativeAstore {
	private static final String IN_ID = "IN";
	private static final int IN_BN = 1;
	private static final int HASH_CODE = 122960861;
	
	public DecisionAlternativeAstoreIn(boolean isConcrete) {
		super(true, isConcrete, IN_BN);
	}
	
	@Override
	public DecisionAlternativeAstoreIn mkNonconcrete() {
		return new DecisionAlternativeAstoreIn(false);
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
		return IN_ID;
	}
}
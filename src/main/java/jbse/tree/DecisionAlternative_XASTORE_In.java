package jbse.tree;

final class DecisionAlternative_XASTORE_In extends DecisionAlternative_XASTORE {
	private static final String IN_ID = "XASTORE_In";
	private static final int HASH_CODE = 2;
	
	public DecisionAlternative_XASTORE_In(boolean isConcrete) {
		super(true, isConcrete, (isConcrete ? 1 : HASH_CODE));
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
package jbse.tree;

public final class DecisionAlternative_XNEWARRAY_Wrong extends DecisionAlternative_XNEWARRAY {
	private static final String WRONG_ID = "XNEWARRAY_Wrong";
	private static final int HASH_CODE = 2;

	DecisionAlternative_XNEWARRAY_Wrong(boolean isConcrete) {
		super(isConcrete, (isConcrete ? 1 : HASH_CODE));
	}
	
	@Override
	public boolean ok() {
		return false;
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
		return WRONG_ID;
	}
}

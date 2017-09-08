package jbse.tree;

/**
 * {@link DecisionAlternative_IFX} for the case where
 * the comparison yields false.
 */
public final class DecisionAlternative_IFX_False extends DecisionAlternative_IFX {
	private static final String F_ID = "IFX_False";
	private static final int HASH_CODE = 2;
	
	DecisionAlternative_IFX_False(boolean isConcrete) {
		super(false, isConcrete, (isConcrete ? 1 : HASH_CODE));
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
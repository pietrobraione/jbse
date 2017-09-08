package jbse.tree;

/**
 * {@link DecisionAlternative_IFX} for the case where
 * the comparison yields true.
 */
public final class DecisionAlternative_IFX_True extends DecisionAlternative_IFX {
	private static final String T_ID = "IFX_True";
	private static final int HASH_CODE = 1;
	
	DecisionAlternative_IFX_True(boolean isConcrete) {
		super(true, isConcrete, HASH_CODE);
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
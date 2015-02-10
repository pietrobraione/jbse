package jbse.tree;

/**
 * True, from check of nonconcrete values.
 */
public final class DecisionAlternative_IFX_True extends DecisionAlternative_IFX {
	private static final String T_ID = "T";
	private static final int T_BN = 1;
	private static final int HASH_CODE = 314652131;
	
	DecisionAlternative_IFX_True(boolean isConcrete) {
		super(true, isConcrete, T_BN);
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
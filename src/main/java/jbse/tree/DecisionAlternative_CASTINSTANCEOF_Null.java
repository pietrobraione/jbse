package jbse.tree;

/**
 * {@link DecisionAlternative_CASTINSTANCEOF} for the case where
 * the reference is null.
 */
public final class DecisionAlternative_CASTINSTANCEOF_Null extends DecisionAlternative_CASTINSTANCEOF {
	private static final String C_ID = "CASTINSTANCEOF_Null";
	private static final int HASH_CODE = 100001;
	private static final DecisionAlternative_CASTINSTANCEOF_Null INSTANCE = new DecisionAlternative_CASTINSTANCEOF_Null();
	
	private DecisionAlternative_CASTINSTANCEOF_Null() {
		super(HASH_CODE);
	}
	
	public static DecisionAlternative_CASTINSTANCEOF_Null instance() {
		return INSTANCE;
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
		return C_ID;
	}
}
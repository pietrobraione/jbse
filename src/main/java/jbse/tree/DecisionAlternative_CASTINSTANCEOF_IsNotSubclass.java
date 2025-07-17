package jbse.tree;

/**
 * {@link DecisionAlternative_CASTINSTANCEOF} for the case where
 * the reference points to an object that is not a subclass of the 
 * cast class.
 */
public final class DecisionAlternative_CASTINSTANCEOF_IsNotSubclass extends DecisionAlternative_CASTINSTANCEOF {
	private static final String C_ID = "CASTINSTANCEOF_IsNotSubclass";
	private static final int HASH_CODE = 100003;
	private static final DecisionAlternative_CASTINSTANCEOF_IsNotSubclass INSTANCE = new DecisionAlternative_CASTINSTANCEOF_IsNotSubclass();
	
	DecisionAlternative_CASTINSTANCEOF_IsNotSubclass() {
		super(HASH_CODE);
	}
	
	public static DecisionAlternative_CASTINSTANCEOF_IsNotSubclass instance() {
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
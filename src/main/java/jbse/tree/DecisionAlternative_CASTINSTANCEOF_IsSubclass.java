package jbse.tree;

/**
 * {@link DecisionAlternative_CASTINSTANCEOF} for the case where
 * the reference points to an object that is a subclass of the 
 * cast class.
 */
public final class DecisionAlternative_CASTINSTANCEOF_IsSubclass extends DecisionAlternative_CASTINSTANCEOF {
	private static final String C_ID = "CASTINSTANCEOF_IsSubclass";
	private static final int HASH_CODE = 9967;
	private static final DecisionAlternative_CASTINSTANCEOF_IsSubclass INSTANCE_REFINE = new DecisionAlternative_CASTINSTANCEOF_IsSubclass(true);
	private static final DecisionAlternative_CASTINSTANCEOF_IsSubclass INSTANCE_NOREFINE = new DecisionAlternative_CASTINSTANCEOF_IsSubclass(false);

	private final boolean refine;
	
	private DecisionAlternative_CASTINSTANCEOF_IsSubclass(boolean refine) {
		super(HASH_CODE + (refine ? 0 : 6));
		this.refine = refine;
	}
	
	public static DecisionAlternative_CASTINSTANCEOF_IsSubclass instance (boolean refine) {
		return (refine ? INSTANCE_REFINE : INSTANCE_NOREFINE);
	}
	
	public boolean refine() {
		return this.refine;
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
		final DecisionAlternative_CASTINSTANCEOF_IsSubclass other = (DecisionAlternative_CASTINSTANCEOF_IsSubclass) obj;
		if (this.refine == other.refine) {
			return true;
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return HASH_CODE + (this.refine ? 0 : 6);
	}
	
	@Override
	public String toString() {
		return C_ID + (this.refine ? "_ref" : "_noref");
	}
}
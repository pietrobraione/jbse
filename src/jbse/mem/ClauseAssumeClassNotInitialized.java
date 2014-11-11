package jbse.mem;

/**
 * A path condition {@link Clause}, an assumption 
 * that some class is not initialized when the symbolic 
 * execution starts.
 *
 * @author Pietro Braione
 *
 */
public class ClauseAssumeClassNotInitialized implements Clause {
	private final String className;
		
	/**
	 * Constructor.
	 * 
	 * @param className a {@code String}, the name of the class.
	 *        It must not be {@code null}.
	 */
	ClauseAssumeClassNotInitialized(String className) { 
		this.className = className; 
	}

	/**
	 * Returns the class name.
	 * 
	 * @return a {@link String}, the name of the class assumed initialized.
	 */
	public String getClassName() { return this.className; }	

	@Override
	public void accept(ClauseVisitor v) throws Exception {
		v.visitClauseAssumeClassNotInitialized(this);
	}

	@Override
	public int hashCode() {
		final int prime = 1283;
		int result = 1;
		result = prime * result
				+ ((className == null) ? 0 : className.hashCode());
		return result;
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
		ClauseAssumeClassNotInitialized other = (ClauseAssumeClassNotInitialized) obj;
		if (className == null) {
			if (other.className != null) {
				return false;
			}
		} else if (!className.equals(other.className)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "!pre_init(" + this.className +")";
	}

	
	@Override
	public ClauseAssumeClassNotInitialized clone() {
		final ClauseAssumeClassNotInitialized o;
		try {
			o = (ClauseAssumeClassNotInitialized) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e);
		}
		return o;
	}
}

package jbse.mem;


/**
 * A path condition {@link Clause}, an assumption 
 * that some class is already initialized when the symbolic 
 * execution starts.
 *
 * @author Pietro Braione
 *
 */
public class ClauseAssumeClassInitialized implements Clause {
	private final String className;
	private final Klass k;
		
	/**
	 * Constructor.
	 * 
	 * @param className a {@code String}, the name of the class.
	 *        It must not be {@code null}.
	 * @param k the symbolic {@link Klass} corresponding to {@code className}
	 *        (cached here for convenience).
	 */
	ClauseAssumeClassInitialized(String className, Klass k) { 
		this.className = className; 
		this.k = k.clone(); //safety copy
	}

	/**
	 * Returns the class name.
	 * 
	 * @return a {@link String}, the name of the class assumed initialized.
	 */
	public String getClassName() { return this.className; }	

	Klass getKlass() { 
		return this.k.clone(); //preserves the safety copy
	}
	
	@Override
	public void accept(ClauseVisitor v) throws Exception {
		v.visitClauseAssumeClassInitialized(this);
	}

	@Override
	public int hashCode() {
		final int prime = 89;
		int result = 1;
		result = prime * result
				+ ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((k == null) ? 0 : k.hashCode());
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
		ClauseAssumeClassInitialized other = (ClauseAssumeClassInitialized) obj;
		if (className == null) {
			if (other.className != null) {
				return false;
			}
		} else if (!className.equals(other.className)) {
			return false;
		}
		if (k == null) {
			if (other.k != null) {
				return false;
			}
		} else if (!k.equals(other.k)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "pre_init(" + this.className +")";
	}

	
	@Override
	public ClauseAssumeClassInitialized clone() {
		final ClauseAssumeClassInitialized o;
		try {
			o = (ClauseAssumeClassInitialized) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e);
		}
		return o;
	}
}

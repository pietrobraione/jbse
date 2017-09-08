package jbse.bc;

/**
 * Class that represents the signature of a method or a field.
 */
public class Signature {
	public final static String SIGNATURE_SEPARATOR = ":";
	
    private final String containerClass;
    private final String descriptor;
    private final String name;
    
    /**
     * Constructor; given the class, the descriptor and the name of a 
     * method or field creates a signature for it.
     * 
     * @param containerClass the name of the class containing the method or field.
     * @param descriptor the descriptor of the method or field.
     * @param name the name of the method or field.
     */	
    public Signature(String containerClass, String descriptor, String name) {
        this.containerClass = containerClass;
        this.descriptor = descriptor;
        this.name = name;
    }
    
    /**
     * Returns the name of the class.
     *  
     * @return a {@link String}, the name of the class (e.g. "java/lang/Object").
     */
    public String getClassName() {
        return this.containerClass;
    }
    
    /**
     * Returns the descriptor.
     * 
     * @return a {@link String}, the descriptor.
     */
    public String getDescriptor() {
        return this.descriptor;
    }
    
    /**
     * Returns the name.
     * 
     * @return a {@link String}, the name.
     */
    public String getName() {
        return this.name;
    }
    
    @Override
    public String toString() {
        return this.containerClass + SIGNATURE_SEPARATOR + this.descriptor + SIGNATURE_SEPARATOR + this.name;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((containerClass == null) ? 0 : containerClass.hashCode());
		result = prime * result
				+ ((descriptor == null) ? 0 : descriptor.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Signature other = (Signature) obj;
		if (containerClass == null) {
			if (other.containerClass != null)
				return false;
		} else if (!containerClass.equals(other.containerClass))
			return false;
		if (descriptor == null) {
			if (other.descriptor != null)
				return false;
		} else if (!descriptor.equals(other.descriptor))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}

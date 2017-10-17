package jbse.bc;

/**
 * Class that represents the signature of a method or a field.
 * It is immutable.
 */
public class Signature {
    public final static String SIGNATURE_SEPARATOR = ":";

    private final String containerClass;
    private final String descriptor;
    private final String name;
    private final int hashCode;

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
        final int prime = 31;
        int hash = 1;
        hash = prime * hash + ((this.containerClass == null) ? 0 : this.containerClass.hashCode());
        hash = prime * hash + ((this.descriptor == null) ? 0 : this.descriptor.hashCode());
        hash = prime * hash + ((this.name == null) ? 0 : this.name.hashCode());
        this.hashCode = hash;
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
        return this.hashCode;
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
        final Signature other = (Signature) obj;
        if (this.containerClass == null) {
            if (other.containerClass != null) {
                return false;
            }
        } else if (!this.containerClass.equals(other.containerClass)) {
            return false;
        }
        if (this.descriptor == null) {
            if (other.descriptor != null) {
                return false;
            }
        } else if (!this.descriptor.equals(other.descriptor)) {
            return false;
        }
        if (this.name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!this.name.equals(other.name)) {
            return false;
        }
        return true;
    }
}

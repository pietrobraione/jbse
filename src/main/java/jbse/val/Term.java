package jbse.val;

import jbse.val.exc.InvalidTypeException;

/**
 * An arbitrary {@link Symbolic} {@link Primitive} value, on which 
 * assumptions can be made. Terms serve essentially two purposes: They
 * are used to represent internal or bound variables, which are not 
 * meant to be used externally but rather replaced by some other
 * {@link Primitive} (e.g., a term is used to represent the generic array
 * index, but is usually replaced with the concrete value used to access
 * the array); And they are used for interfacing the symbolic executor 
 * with an external decision procedure whenever a {@link Primitive} cannot 
 * be directly sent (e.g., some pure function applications cannot be 
 * handled by some decision procedures, and are therefore consistently
 * replaced by suitable terms). <br />
 * 
 * Terms are identified by a {@link String} name that 
 * must be provided upon construction.
 * 
 * @author Pietro Braione
 */
public final class Term extends Primitive implements Symbolic {
    /** The conventional value of the {@link Term}, a {@link String}. */
    private final String value;

    /** Cached hash code. */
    private final int hashCode;

    /**
     * Constructor.
     * 
     * @param type a {@code char}, the type of this value.
     * @param value a {@code String}, the name of the term. Two 
     *        {@link Term}s with same name will be considered the
     *        same {@link Term}.
     * @throws InvalidTypeException if {@code type} is not primitive. 
     */
    Term(char type, String value) throws InvalidTypeException {
        super(type);
        this.value = value;

        //calculates the hash code
        final int prime = 293;
        int tmpHashCode = 1;
        tmpHashCode = prime * tmpHashCode + ((value == null) ? 0 : value.hashCode());
        this.hashCode = tmpHashCode;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public String asOriginString() {
        return this.value;
    }

    @Override
    public HistoryPoint historyPoint() {
        return null;
    }

    @Override
    public Symbolic root() {
        return this;
    }

    @Override
    public boolean hasContainer(Symbolic s) {
        if (s == null) {
            throw new NullPointerException();
        }
        return s.equals(this);
    }

    @Override
    public void accept(PrimitiveVisitor v) throws Exception {
        v.visitTerm(this);
    }

    @Override
    public boolean surelyTrue() {
        return false;
    }

    @Override
    public boolean surelyFalse() {
        return false;
    }

    @Override
    public boolean isSymbolic() {
        return true;
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
        final Term other = (Term) obj;
        if (this.value == null) {
            if (other.value != null) { 
                return false;
            }
        } else if (!this.value.equals(other.value)) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.value;
    }
}

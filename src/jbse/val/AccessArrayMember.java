package jbse.val;

/**
 * An access to an array member through an index.
 * 
 * @author Pietro Braione
 *
 */
public final class AccessArrayMember extends AccessNonroot {
    private final Primitive index;
    private final String toString;

    public AccessArrayMember(Primitive index) {
        this.index = index;
        this.toString = "[" + index.toString() + "]";
    }
    
    public Primitive index() {
        return this.index;
    }
    
    @Override
    public String toString() {
        return this.toString;
    }

}

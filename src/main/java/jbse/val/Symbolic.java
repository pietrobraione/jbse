package jbse.val;


/**
 * Interface that must be implemented by all the symbolic values.
 * 
 * @author Pietro Braione
 *
 */
public interface Symbolic {
    /**
     * Returns a {@link String} value
     * for the symbol. Two different
     * symbols must have different value.
     * 
     * @return a {@link String}.
     */
    String getValue();

    /**
     * Returns a {@link String} that
     * reflects the origin of this
     * symbol.
     * 
     * @return a {@link String}.
     */
    String asOriginString();
}

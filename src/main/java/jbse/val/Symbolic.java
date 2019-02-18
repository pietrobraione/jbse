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
    
    /**
     * Returns the {@link HistoryPoint} of
     * creation of this symbol.
     * 
     * @return a {@link HistoryPoint}.
     */
    HistoryPoint historyPoint();
    
    /**
     * Returns the root {@link Symbolic} (i.e., the topmost
     * container) where this {@link Symbolic} originates from.
     * 
     * @return a {@link Symbolic} (returns {@code this} if
     *         this {@link Symbolic} is a topmost container).
     */
    Symbolic root();
    
    /**
     * Checks whether another {@link Symbolic} is a 
     * container for this {@link Symbolic}.
     * 
     * @param r a {@link Symbolic}. It must not be {@code null}.
     * @return {@code true} iff {@code s} (recursively) is a container
     *         for this {@link Symbolic}. Note that 
     *         {@code s.isContainer(s) == true}.
     * @throws NullPointerException if {@code r == null}. 
     */
    boolean hasContainer(Symbolic s);
}

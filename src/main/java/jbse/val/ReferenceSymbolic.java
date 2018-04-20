package jbse.val;

import static jbse.common.Type.REFERENCE;

import jbse.mem.Klass;

/**
 * Class for symbolic {@link Reference}s to the {@link Heap}.
 * 
 * @author Pietro Braione
 */
public abstract class ReferenceSymbolic extends Reference implements Symbolic {
    /** 
     * The static type of the reference (or null). 
     */
    private final String staticType;

    /**
     * Constructor.
     * 
     * @param staticType a {@link String}, the static type of the
     *        variable from which this reference originates, 
     *        or {@code null} if this object refers to a {@link Klass}.
     */
    ReferenceSymbolic(String staticType) {
    	super(REFERENCE);
        this.staticType = staticType;
    }
    
    /**
     * Returns the static type of this reference.
     * 
     * @return a {@code String}.
     */
    public final String getStaticType() {
    	return this.staticType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getValue() {
    	return toString();
    }

    /**
     * {@inheritDoc}
     * For {@link ReferenceSymbolic} values it will always return {@code true}.
     */
    @Override
    public final boolean isSymbolic() {
    	return true;
    }
}

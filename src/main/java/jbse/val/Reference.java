package jbse.val;

import jbse.common.Type;

/**
 * Class for references to heap objects (instances and arrays).
 */
public abstract class Reference extends Value {
    protected Reference(char type) {
        super(type);
    }

    protected Reference() {
        this(Type.REFERENCE);
    }
    
    @Override   
    public final void accept(ValueVisitor v) throws Exception {
    	final ReferenceVisitor rv = (ReferenceVisitor) v;
    	accept(rv);
    }

    /**
     * Accepts a {@link ReferenceVisitor}.
     * 
     * @param v a {@link ReferenceVisitor}.
     * @throws Exception whenever {@code v} throws an {@link Exception}.
     */
    public abstract void accept(ReferenceVisitor v) throws Exception;
}
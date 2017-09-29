package jbse.val;

import jbse.common.Type;
import jbse.mem.Util;

/**
 * Class that represent the null {@link ReferenceConcrete}; it is a Singleton.
 * 
 * @author Pietro Braione
 */
public final class Null extends ReferenceConcrete {
	private static ReferenceConcrete instance = new Null();
    
    /**
     * Constructor.
     */
    private Null() {
    	super(Type.NULLREF, Util.POS_NULL);
    }

    public static ReferenceConcrete getInstance() {
        return instance;
    }
    
    /**
     * Returns the value
     */
    public String getValue() {
	    return("null");
    }

    /**
     * returns String representation of Value
     */
    @Override
    public String toString() {
        return getValue();
    }
    
    @Override
    public boolean equals(Object o) {
        return (o == this);
    }
}
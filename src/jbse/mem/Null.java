package jbse.mem;

import jbse.Type;

/**
 * Class that represent the null {@link ReferenceConcrete}; it is a Singleton.
 * 
 * @author Pietro Braione
 */
public class Null extends ReferenceConcrete {
	private static Reference instance = new Null();
    
    /**
     * Constructor.
     */
    private Null() {
    	super(Type.NULLREF, Util.POS_NULL);
    }

    public static Reference getInstance() {
        return instance;
    }
    
    /**
     * Returns the value
     */
    public String getValue() {
	    return("null");
    }
    
    @Override
    public Null clone() {
    	return this;
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
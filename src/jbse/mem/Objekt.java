package jbse.mem;

import java.util.Arrays;
import java.util.HashMap;

import jbse.bc.Signature;

/**
 * A Java object which may reside in the heap or in the static store, 
 * i.e., either a class, or an instance of a class, or an array.
 */
public abstract class Objekt implements Cloneable {
    
	/** 
	 * The creation epoch of an {@link Objekt}.
	 * 
	 * @author Pietro Braione
	 *
	 */
	protected enum Epoch { 
		EPOCH_BEFORE_START, EPOCH_AFTER_START
	}
	
    /** Static type identifier. Immutable. */
	protected final String type;

	/**
	 * The origin of the object in the case it is created by 
	 * lazy initialization. Immutable.
	 */
	protected final String origin;

    /** The creation epoch of this {@link Objekt}. Immutable. */
    protected final Epoch epoch;

    /** All the signatures of all the fields. Immutable. */
    protected final Signature[] fieldSignatures;
    
    /** 
     * The fields as a map of signatures (as strings) to variables.
     * Variables are possibly mutable (immutable for arrays). 
     */
    protected HashMap<String, Variable> fields;
	
    /**
     * Constructor.
     * 
     * @param calc a {@link Calculator}.
     * @param fieldSignatures an array of field {@link Signature}s.
     * @param type a {@link String}, the class of this object.
     * @param origin a {@link String}, the
     * chain of reference accesses which allowed to discover
     * the object in first instance.
     * @param epoch the creation {@link Epoch} of this object.
     */
    protected Objekt(Calculator calc, Signature[] fieldSignatures, String type, String origin, Epoch epoch) {
        this.fields = new HashMap<String, Variable>();
        this.fieldSignatures = Arrays.copyOf(fieldSignatures, fieldSignatures.length); //safety copy
        for (Signature s : this.fieldSignatures) {
            this.fields.put(s.toString(), new Variable(calc, s.getDescriptor(), s.getName()));
        }
    	this.type = type;
    	this.origin = origin;
    	this.epoch = epoch;
    }
    
	/**
     * Returns the class name of this {@link Objekt} (i.e., 
     * {@code "java/lang/Object"} or {@code "[[I"}).
     * 
     * @return a {@link String}.
     */
	public final String getType() {
		return this.type;
	}
    
    /**
     * Returns the object's origin.
     * 
     * @return a {@link String}
     */
    public final String getOrigin() {
    	return this.origin;
    }
    
    /**
     * Checks the epoch of this {@link Objekt}.
     *  
     * @return {@code true} iff the object is symbolic, i.e., 
     *         is an object that was present in the heap 
     *         before the start of the symbolic execution.
     */
    public final boolean isSymbolic() {
    	return (this.epoch == Epoch.EPOCH_BEFORE_START); 
    }
    
    /**
     * Returns the {@link Signature}s of all the fields
     * declared in this {@link Instance}.
     * 
     * @return the {@link Signature}{@code []} used for the
     *         construction of this object.
     */
    public final Signature[] getFieldSignatures() {
    	return this.fieldSignatures;
    }
    
    /**
     * Gets the value in a field of the {@link Instance}.
     * 
     * @param sig the {@link Signature} of the field.
     * @return a {@link Value} object which is the value 
     * stored in the field of the {@link Instance}, or 
     * {@code null} if the {@link Instance} has no 
     * field with that {@code name}. 
     */
    public final Value getFieldValue(Signature sig) {
    	//TODO does it work with visibility modifiers???
        try {
            return this.fields.get(sig.toString()).getValue();  //toString() is necessary, type erasure doesn't play well
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Gets the value in a field of the {@link Instance}.
     * 
     * @param fieldName the name of the field.
     * @return a {@link Value} object which is the value 
     * stored in the field of the {@link Instance}, or 
     * {@code null} if the {@link Instance} has no 
     * field with that {@code name}. 
     */
    public final Value getFieldValue(String fieldName) {
    	//TODO does it work with visibility modifiers???
        try {
        	for (Signature sig: this.fieldSignatures) {
        		if (sig.getName().equals(fieldName)) {
        			return getFieldValue(sig);
        		}
        	}
        	return null;
        } catch (Exception e) {
            return null;
        }
    }
    
	@Override
    public Objekt clone() {
    	try {
    		return (Objekt) super.clone();
    	} catch (CloneNotSupportedException e) {
    		throw new InternalError(e);
    	}
    }
}
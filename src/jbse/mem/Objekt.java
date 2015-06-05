package jbse.mem;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jbse.bc.Signature;
import jbse.val.Calculator;
import jbse.val.Value;

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
	private final String origin;

    /** The creation epoch of this {@link Objekt}. Immutable. */
    private final Epoch epoch;
    
    /** The (base-level) hash code of this {@link Objekt}. Immutable. */
    private final int hashCode;

    /** All the signatures of all the fields. Immutable. */
    private final List<Signature> fieldSignatures;
    
    /** 
     * The fields as a map of signatures (as strings) to variables.
     * Immutable for arrays, but mutable otherwise (the map
     * is by itself immutable but the stored Variables may be 
     * mutable). 
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
    protected Objekt(Calculator calc, String type, String origin, Epoch epoch, Signature... fieldSignatures) {
        this.fields = new HashMap<>();
        this.fieldSignatures = Arrays.asList(fieldSignatures.clone()); //safety copy
        for (Signature s : this.fieldSignatures) {
            this.fields.put(s.toString(), new Variable(calc, s.getDescriptor(), s.getName()));
        }
    	this.type = type;
    	this.origin = origin;
    	this.epoch = epoch;
    	this.hashCode = hashCode(); //we piggyback the underlying JVM for hash codes
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
     * Returns the hash code of this {@code Objekt}.
     * Do not be confused! This is the base-level 
     * hash code, i.e., the hash code JBSE will 
     * expose during symbolic execution.
     * 
     * @return an {@code int}.
     */
    public final int getObjektHashCode() {
        return this.hashCode;
    }
    
    /**
     * Returns the {@link Signature}s of all the fields
     * declared in this {@link Instance}.
     * 
     * @return an immutable 
     *         {@link Collection}{@code <}{@link Signature}{@code >}.
     */
    public final Collection<Signature> getFieldSignatures() {
    	return Collections.unmodifiableCollection(this.fieldSignatures);
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
     * Sets the value of a field. Throws a runtime exception 
     * in the case the field does not exist or is immutable.
     * 
     * @param field the {@link Signature} of the field.
     * @param item the new {@link Value} that must be assigned to
     *             the field.
     */
    //TODO throw a better exception in the case a field does not exist or is immutable
    public void setFieldValue(Signature field, Value item) {
        this.fields.get(field.toString()).setValue(item); //toString() is necessary, type erasure doesn't play well
    }
    
    /**
     * Returns an immutable view of this 
     * {@link Objekt}'s fields.
     * 
     * @return an immutable 
     *         {@link Map}{@code <}{@link String}{@code , }{@link Variable}{@code <}
     *         backed by this {@link Objekt}'s fields map.
     */
    public Map<String, Variable> fields() {
        return Collections.unmodifiableMap(this.fields);
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
        for (Signature sig: this.fieldSignatures) {
            if (sig.getName().equals(fieldName)) {
                return getFieldValue(sig);
            }
        }
        return null;
    }
    
    protected final HashMap<String, Variable> fieldsDeepCopy() {
        final HashMap<String, Variable> retVal = new HashMap<>();
        for (String key : this.fields.keySet()) {
            final Variable variableClone = this.fields.get(key).clone();
            retVal.put(key, variableClone);
        }
        return retVal;
    }
    
	@Override
    public Objekt clone() {
    	try {
    		return (Objekt) super.clone();
    	} catch (CloneNotSupportedException e) {
    		throw new InternalError(e);
    	}
    	//note that we do not clone this.fields because
    	//it is immutable for arrays and mutable for instances;
    	//note also that the clone will have same base-level
    	//hash code as the original.
    }
}
package jbse.mem;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jbse.bc.Signature;
import jbse.val.Calculator;
import jbse.val.HistoryPoint;
import jbse.val.Primitive;
import jbse.val.ReferenceSymbolic;
import jbse.val.Value;

/**
 * A Java object which may reside in the heap or in the static store, 
 * i.e., either a class, or an instance of a class, or an array.
 */
public abstract class Objekt implements Cloneable {
    /** Whether this object is symbolic. Immutable. */
    private boolean symbolic;
    
    /** Static type identifier. Immutable. */
	protected final String type;

	/**
	 * The origin of the object in the case it is created by 
	 * lazy initialization. Immutable.
	 */
	private final ReferenceSymbolic origin;

    /** The creation epoch of this {@link Objekt}. Immutable. */
    private final HistoryPoint epoch;
    
    /** All the signatures of all the fields. Immutable. */
    private final List<Signature> fieldSignatures;
    
    /** 
     * The hash code of this {@link Objekt}. Mutable only
     * because it must be set after creation, but should not
     * be changed after its initialization.
     */
    private Primitive hashCode;

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
     * @param symbolic a {@code boolean}, whether this object is symbolic
     *        (i.e., not explicitly created during symbolic execution by
     *        a {@code new*} bytecode, but rather assumed).
     * @param calc a {@link Calculator}.
     * @param type a {@link String}, the class of this object.
     * @param origin the {@link ReferenceSymbolic} providing origin of 
     *        the {@code Objekt}, if symbolic, or {@code null}, if concrete.
     * @param epoch the creation {@link HistoryPoint} of this object.
     * @param fieldSignatures an array of field {@link Signature}s.
     */
    protected Objekt(boolean symbolic, Calculator calc, String type, ReferenceSymbolic origin, HistoryPoint epoch, Signature... fieldSignatures) {
        this.symbolic = symbolic;
        this.fields = new HashMap<>();
        this.fieldSignatures = Arrays.asList(fieldSignatures.clone()); //safety copy
        for (Signature s : this.fieldSignatures) {
            this.fields.put(s.toString(), new Variable(calc, s.getDescriptor(), s.getName()));
        }
    	this.type = type;
    	this.origin = origin;
    	this.epoch = epoch;
    	this.hashCode = calc.valInt(hashCode()); //TODO calc.valInt(hashCode()) is a VERY POOR choice! Use a suitable symbol also when the Objekt is concrete.
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
     * @return a {@link ReferenceSymbolic}
     */
    public final ReferenceSymbolic getOrigin() {
    	return this.origin;
    }
    
    /**
     * Returns the creation epoch of this object
     * as a {@link HistoryPoint}.
     * 
     * @return a {@link HistoryPoint}.
     */
    public final HistoryPoint historyPoint() {
        return this.epoch;
    }
    
    /**
     * Checks the epoch of this {@link Objekt}.
     *  
     * @return {@code true} iff the object is symbolic, i.e., 
     *         it was not explicitly created during symbolic execution 
     *         by a {@code new*} bytecode, but rather assumed.
     */
    public final boolean isSymbolic() {
    	return this.symbolic; 
    }
    
    /**
     * Sets the hash code of this {@link Objekt}.
     * 
     * @param hashCode a {@link Primitive} for the hash code
     *        of this {@link Objekt}.
     */
    public final void setObjektHashCode(Primitive hashCode) {
        this.hashCode = hashCode;
    }
    
    /**
     * Returns the hash code of this {@code Objekt}.
     * 
     * @return a {@code Primitive}.
     */
    public final Primitive getObjektHashCode() {
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
    	//note also that the clone will have same
    	//hash code as the original.
    }
}
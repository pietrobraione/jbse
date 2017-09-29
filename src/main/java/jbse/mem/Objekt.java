package jbse.mem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jbse.bc.Signature;
import jbse.val.Calculator;
import jbse.val.MemoryPath;
import jbse.val.Primitive;
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
	private final MemoryPath origin;

    /** The creation epoch of this {@link Objekt}. Immutable. */
    private final Epoch epoch;
    
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
     * Maps an int (slot number) to a field signature;
     * used to support sun.misc.Unsafe.
     */
    private final ArrayList<Signature> slotToSignatures;
	
    /**
     * Constructor.
     * 
     * @param calc a {@link Calculator}.
     * @param type a {@link String}, the class of this object.
     * @param origin a {@link MemoryPath}, the
     *        chain of memory accesses which allowed to discover
     *        the object for the first time. It can be null when
     *        {@code epoch == }{@link Epoch#EPOCH_AFTER_START}.
     * @param epoch the creation {@link Epoch} of this object.
     * @param fieldSignatures an array of field {@link Signature}s.
     */
    protected Objekt(Calculator calc, String type, MemoryPath origin, Epoch epoch, Signature... fieldSignatures) {
        this.fields = new HashMap<>();
        this.fieldSignatures = Arrays.asList(fieldSignatures.clone()); //safety copy
        this.slotToSignatures = new ArrayList<>();
        for (Signature s : this.fieldSignatures) {
            this.fields.put(s.toString(), new Variable(calc, s.getDescriptor(), s.getName()));
            this.slotToSignatures.add(s);
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
     * @return a {@link String}
     */
    public final MemoryPath getOrigin() {
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
     * Sets the hash code of this {@link Objekt}.
     * 
     * @param hashCode a {@link Primitive} for the hash code
     *        of this {@link Objekt}. It must be set when 
     *        {@code epoch == }{@link Epoch#EPOCH_BEFORE_START}.
     *        When {@code epoch == }{@link Epoch#EPOCH_AFTER_START}
     *        the hash code of this {@link Objekt} from the underlying
     *        JVM is used.
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
     * Gets the value in a field of the {@link Instance}.
     * 
     * @param slot an {@code int} signifying a slot number
     * of a field.
     * @return a {@link Value} object which is the value 
     * stored in the field of the {@link Instance}, or 
     * {@code null} if {@code slot} is not the slot number
     * of a field. 
     */
    public final Value getFieldValue(int slot) {
    		try {
    			return getFieldValue(this.slotToSignatures.get(slot));
    		} catch (IndexOutOfBoundsException e) {
    			return null;
    		}
    }
    
    /**
     * Returns the slot number of a field.
     * 
     * @param sig the {@link Signature} of the field.
     * @return an {@code int} greater or equal to zero, 
     *         signifying the slot number of the field
     *         with signature {@code sig}, or {@code -1}
     *         if such field does not exist.
     */
    public final int getFieldSlot(Signature sig) {
    		return this.slotToSignatures.indexOf(sig); //not very efficient but we don't care
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
    public final void setFieldValue(Signature field, Value item) {
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
    public final Map<String, Variable> fields() {
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
package jbse.mem;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jbse.bc.ClassFile;
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

    /** ClassFile for this object's class. Immutable. */
    protected final ClassFile classFile;

    /**
     * The origin of the object in the case it is created by 
     * lazy initialization. Immutable.
     */
    private final ReferenceSymbolic origin;

    /** The creation {@link HistoryPoint} of this {@link Objekt}. Immutable. */
    private final HistoryPoint epoch;

    /** 
     * {@code true} if the object must store the static fields,
     * {@code false} if it must store the object (nonstatic) fields.
     */
    private final boolean staticFields;

    /** The number of static fields. Immutable. */
    private final int numOfStaticFields;

    /** 
     * All the signatures of all the fields declared by 
     * this {@link Objekt}'s class (static and nonstatic)
     * or superclasses (nonstatic). The position of a field
     * signature in this list is its slot number,
     * used to support sun.misc.Unsafe. Immutable. */
    private final List<Signature> fieldSignatures;

    /** 
     * The hash code of this {@link Objekt}. Mutable only
     * because it must be set after creation, but should not
     * be changed after its initialization.
     */
    private Primitive defaultHashCode;

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
     * @param type a {@link ClassFile}, the class of this object.
     * @param origin the {@link ReferenceSymbolic} providing origin of 
     *        the {@code Objekt}, if symbolic, or {@code null}, if concrete.
     * @param epoch the creation {@link HistoryPoint} of this object.
     * @param staticFields {@code true} if this object stores
     *        the static fields, {@code false} if this object stores
     *        the object (nonstatic) fields.
     * @param numOfStaticFields an {@code int}, the number of static fields.
     * @param fieldSignatures varargs of field {@link Signature}s, all the
     *        fields this object knows.
     */
    protected Objekt(boolean symbolic, Calculator calc, ClassFile classFile, ReferenceSymbolic origin, HistoryPoint epoch, boolean staticFields, int numOfStaticFields, Signature... fieldSignatures) {
        this.symbolic = symbolic;
        this.fields = new HashMap<>();
        this.staticFields = staticFields;
        this.numOfStaticFields = numOfStaticFields;
        this.fieldSignatures = Arrays.asList(fieldSignatures.clone()); //safety copy
        int curSlot = 0;
        for (Signature s : this.fieldSignatures) {
            if ((staticFields && curSlot < numOfStaticFields) ||
                (!staticFields && curSlot >= numOfStaticFields)) {
                this.fields.put(s.toString(), new Variable(calc, s.getDescriptor(), s.getName()));
            }
            ++curSlot;
        }
        this.classFile = classFile;
        this.origin = origin;
        this.epoch = epoch;
        //this.hashCode must be initialized by means of setters
    }

    /**
     * Returns the class of this object.
     * 
     * @return a {@link ClassFile}.
     */
    public final ClassFile getType() {
        return this.classFile;
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
     * Checks whether this {@link Objekt} is symbolic.
     *  
     * @return {@code true} iff the object is symbolic, i.e., 
     *         it was not explicitly created during symbolic execution 
     *         by a {@code new*} bytecode, but rather assumed.
     */
    public final boolean isSymbolic() {
    	return this.symbolic; 
    }

    /**
     * Sets the default hash code of this {@link Objekt}.
     * 
     * @param defaultHashCode a {@link Primitive} with type {@code int} 
     *        for the default hash code of this {@link Objekt}.
     */
    public final void setObjektDefaultHashCode(Primitive defaultHashCode) {
        //TODO check that the type of defaultHashCode is INT.
        this.defaultHashCode = defaultHashCode;
    }

    /**
     * Returns the default hash code of this {@code Objekt}.
     * 
     * @return a {@code Primitive}.
     */
    public final Primitive getObjektDefaultHashCode() {
        //TODO check that this.defaultHashCode is not null.
        return this.defaultHashCode;
    }

    /**
     * Returns the {@link Signature}s of all the fields
     * this {@link Objekt} stores.
     * 
     * @return an immutable 
     *         {@link Collection}{@code <}{@link Signature}{@code >}.
     */
    public final Collection<Signature> getStoredFieldSignatures() {
        if (this.staticFields) {
            return Collections.unmodifiableCollection(this.fieldSignatures.subList(0, this.numOfStaticFields));
        } else {
            return Collections.unmodifiableCollection(this.fieldSignatures.subList(this.numOfStaticFields, this.fieldSignatures.size()));
        }
    }

    /**
     * Checks whether an object has a slot with a given number.
     * 
     * @param slot an {@code int}.
     * @return {@code true} iff the object has a field with slot
     *         number {@code slot}. For arrays it is {@code true}
     *         iff the array's length is concrete and {@code slot}
     *         is in range.
     */
    //TODO overridden by array, refactor to avoid this
    public boolean hasSlot(int slot) {
        if (this.staticFields) {
            return (0 <= slot && slot < this.numOfStaticFields);
        } else {
            return (this.numOfStaticFields <= slot && slot < this.fieldSignatures.size());
        }
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
            return getFieldValue(this.fieldSignatures.get(slot));
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Returns the slot number of a field.
     * 
     * @param field the {@link Signature} of the field.
     * @return an {@code int} greater or equal to zero, 
     *         signifying the slot number of the field
     *         with signature {@code field}, or {@code -1}
     *         if such field does not exist.
     */
    public final int getFieldSlot(Signature field) {
        return this.fieldSignatures.indexOf(field); //not very efficient but we don't care
    }

    /**
     * Sets the value of a field. Throws a runtime exception 
     * in the case the field does not exist or is immutable.
     * 
     * @param field the {@link Signature} of the field.
     * @param item the new {@link Value} that must be assigned to
     *        the field.
     */
    //TODO throw a better exception in the case a field does not exist or is immutable
    public final void setFieldValue(Signature field, Value item) {
        this.fields.get(field.toString()).setValue(item); //toString() is necessary, type erasure doesn't play well
    }

    /**
     * Sets the value of a field. Throws a runtime exception 
     * in the case the field does not exist or is immutable.
     * 
     * @param slot an {@code int} signifying a slot number
     *        of a field.
     * @param item the new {@link Value} that must be assigned to
     *        the field.
     */
    //TODO throw a better exception in the case a field does not exist or is immutable
    public final void setFieldValue(int slot, Value item) {
        setFieldValue(this.fieldSignatures.get(slot), item);
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
        //it is immutable for arrays and mutable for instances
        //so the two subclasses may either deep-copy it or share;
        //note also that the clone will have same
        //hash code as the original.
    }
}
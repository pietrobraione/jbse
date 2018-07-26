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
 * Base class for all classes that implement {@link Objekt}s.
 */
public abstract class ObjektImpl implements Objekt {
    /** Whether this object is symbolic. Immutable. */
    private boolean symbolic;

    /** ClassFile for this object's class. Immutable. */
    protected final ClassFile classFile;

    /**
     * The origin of the object in the case it is created by 
     * lazy initialization. Immutable.
     */
    private final ReferenceSymbolic origin;

    /** The creation {@link HistoryPoint} of this {@link ObjektImpl}. Immutable. */
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
     * this {@link ObjektImpl}'s class (static and nonstatic)
     * or superclasses (nonstatic). The position of a field
     * signature in this list is its slot number,
     * used to support sun.misc.Unsafe. Immutable. */
    private final List<Signature> fieldSignatures;

    /** 
     * The identity hash code of this {@link ObjektImpl}. Mutable only
     * because it must be set after creation, but should not
     * be changed after its initialization.
     */
    private Primitive identityHashCode;

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
    protected ObjektImpl(boolean symbolic, Calculator calc, ClassFile classFile, ReferenceSymbolic origin, HistoryPoint epoch, boolean staticFields, int numOfStaticFields, Signature... fieldSignatures) {
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
    
    abstract ObjektWrapper<? extends ObjektImpl> makeWrapper(Heap destinationHeap, long destinationPosition);

    @Override
    public final ClassFile getType() {
        return this.classFile;
    }

    @Override
    public final ReferenceSymbolic getOrigin() {
    	return this.origin;
    }

    @Override
    public final HistoryPoint historyPoint() {
        return this.epoch;
    }
    
    @Override
    public final boolean isSymbolic() {
    	return this.symbolic; 
    }

    @Override
    public final void setIdentityHashCode(Primitive identityHashCode) {
        //TODO check that the type of identityHashCode is INT.
        this.identityHashCode = identityHashCode;
    }

    @Override
    public final Primitive getIdentityHashCode() {
        //TODO check that this.identityHashCode is not null.
        return this.identityHashCode;
    }

    @Override
    public final Collection<Signature> getStoredFieldSignatures() {
        if (this.staticFields) {
            return Collections.unmodifiableCollection(this.fieldSignatures.subList(0, this.numOfStaticFields));
        } else {
            return Collections.unmodifiableCollection(this.fieldSignatures.subList(this.numOfStaticFields, this.fieldSignatures.size()));
        }
    }

    //TODO overridden by array, refactor to avoid this
    @Override
    public boolean hasSlot(int slot) {
        if (this.staticFields) {
            return (0 <= slot && slot < this.numOfStaticFields);
        } else {
            return (this.numOfStaticFields <= slot && slot < this.fieldSignatures.size());
        }
    }

    @Override
    public final Value getFieldValue(Signature sig) {
        //TODO does it work with visibility modifiers???
        try {
            return this.fields.get(sig.toString()).getValue();  //toString() is necessary, type erasure doesn't play well
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public final Value getFieldValue(String fieldName) {
        //TODO does it work with visibility modifiers???
        for (Signature sig: this.fieldSignatures) {
            if (sig.getName().equals(fieldName)) {
                return getFieldValue(sig);
            }
        }
        return null;
    }

    @Override
    public final Value getFieldValue(int slot) {
        try {
            return getFieldValue(this.fieldSignatures.get(slot));
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    @Override
    public final int getFieldSlot(Signature field) {
        return this.fieldSignatures.indexOf(field); //not very efficient but we don't care
    }

    @Override
    public final void setFieldValue(Signature field, Value item) {
        this.fields.get(field.toString()).setValue(item); //toString() is necessary, type erasure doesn't play well
    }

    @Override
    public final void setFieldValue(int slot, Value item) {
        setFieldValue(this.fieldSignatures.get(slot), item);
    }

    @Override
    public final Map<String, Variable> fields() {
        return Collections.unmodifiableMap(this.fields);
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
    public ObjektImpl clone() {
        try {
            return (ObjektImpl) super.clone();
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
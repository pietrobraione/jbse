package jbse.mem;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.bc.exc.FieldNotFoundException;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.val.Calculator;
import jbse.val.HistoryPoint;
import jbse.val.Primitive;
import jbse.val.ReferenceSymbolic;
import jbse.val.Value;

/**
 * Base class for all classes that implement {@link Objekt}s.
 */
public abstract class ObjektImpl implements Objekt {
    /** ClassFile for this object's class. Immutable. */
    protected final ClassFile classFile; //TODO why protected? why here and not just in instance?

    /** Whether this object is symbolic. */
    private boolean symbolic;

    /**
     * The origin of the object in the case it is created by 
     * lazy initialization. Immutable.
     */
    private ReferenceSymbolic origin; //TODO it is not final because we had to add a makeSymbolic method. See notes to it.

    /** The creation {@link HistoryPoint} of this {@link ObjektImpl}. Immutable. */
    private final HistoryPoint epoch;

    /** 
     * {@code true} if the object must store the static fields,
     * {@code false} if it must store the nonstatic fields.
     */
    private final boolean staticFields;

    /** The number of static fields. Immutable. */
    private final int numOfStaticFields;

    /** 
     * All the signatures of all the fields declared by 
     * this {@link ObjektImpl}'s class (static and nonstatic)
     * or superclasses (nonstatic). The position of a field
     * signature in this list (starting from the end) is 
     * the field's offset number, as used by sun.misc.Unsafe 
     * methods. Immutable. */
    private final List<Signature> fieldSignatures;

    /** 
     * The identity hash code of this {@link ObjektImpl}. Mutable only
     * because it must be set after creation, but should not
     * be changed after its initialization.
     */
    private Primitive identityHashCode;

    /** 
     * The fields as a map of signatures to variables.
     * Immutable for arrays, but mutable otherwise (the map
     * is by itself immutable but the stored Variables may be 
     * mutable). 
     */
    protected HashMap<Signature, Variable> fields;
    //TODO once fields was a map from String (the signature toString()) to variables; in old comment it was said it was necessary because type erasure didn't play well. Investigate whether the issue still holds.
    
    /**
     * Constructor.
     * 
     * @param calc a {@link Calculator}. It must not be {@code null}. It will
     *        only be used during object construction and will not be stored
     *        in this {@link ObjektImpl}.
     * @param symbolic a {@code boolean}, whether this object is symbolic
     *        (i.e., not explicitly created during symbolic execution by
     *        a {@code new*} bytecode, but rather assumed).
     * @param classFile a {@link ClassFile}, the class of this object.
     *        It must not be {@code null}.
     * @param origin the {@link ReferenceSymbolic} providing origin of 
     *        the {@code Objekt}, if symbolic, or {@code null}, if concrete.
     * @param epoch the creation {@link HistoryPoint} of this object.
     * @param staticFields {@code true} if this object stores
     *        the static fields, {@code false} if this object stores
     *        the object (nonstatic) fields.
     * @throws InvalidInputException if {@code calc == null || classFile == null}.
     */
    protected ObjektImpl(Calculator calc, boolean symbolic, ClassFile classFile, ReferenceSymbolic origin, HistoryPoint epoch, boolean staticFields) 
    throws InvalidInputException {
    	if (calc == null || classFile == null) {
    		throw new InvalidInputException("Attempted to create an ObjektImpl with a null Calculator or ClassFile parameter.");
    	}
        this.symbolic = symbolic;
        this.fields = new HashMap<>();
        this.staticFields = staticFields;
        this.numOfStaticFields = classFile.numOfStaticFields();
        this.fieldSignatures = Arrays.asList(classFile.getObjectFields().clone()); //safety copy - possibly useless
        int curSlot = 0;
        for (Signature fieldSignature : this.fieldSignatures) {
            if ((staticFields && curSlot < numOfStaticFields) ||
                (!staticFields && curSlot >= numOfStaticFields)) {
            	String fieldGenericSignatureType = null;
            	boolean found = false;
            	for (ClassFile cf : classFile.superclasses()) {
            		if (cf.hasFieldDeclaration(fieldSignature)) {
            			found = true;
            			try {
							fieldGenericSignatureType = cf.getFieldGenericSignatureType(fieldSignature);
						} catch (FieldNotFoundException e) {
							//this should never happen
		            		throw new UnexpectedInternalException(e);
						}
            			break;
            		}
            	}
            	if (!found) {
            		//this should never happen
            		throw new UnexpectedInternalException("Generic signature type information for field " + fieldSignature.toString() + " not found in class " + classFile.getClassName() + " and superclasses.");
            	}
            	this.fields.put(fieldSignature, new Variable(calc, fieldSignature.getDescriptor(), fieldGenericSignatureType, fieldSignature.getName()));
            }
            ++curSlot;
        }
        this.classFile = classFile;
        this.origin = origin;
        this.epoch = epoch;
        //this.hashCode must be initialized by means of setters
    }
    
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
    public void makeSymbolic(ReferenceSymbolic origin) throws InvalidInputException {
    	if (origin == null || this.symbolic == true) {
    		throw new InvalidInputException("Attempted to invoke ObjektImpl.makeSymbolic with a null origin, or over an already symbolic Objekt.");
    	}
    	this.symbolic = true;
    	this.origin = origin;
    }

    @Override
    public final void setIdentityHashCode(Primitive identityHashCode) {
        //TODO check that the type of identityHashCode is INT and that identityHashCode is not null.
        this.identityHashCode = identityHashCode;
    }

    @Override
    public final Primitive getIdentityHashCode() {
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
    
    /**
     * Converts a slot offset into a position
     * into {@code this.fieldSignatures}.
     * 
     * @param ofst an offset of a field as
     *        used by sun.misc.Unsafe methods.
     * @return the position in {@code this.fieldSignatures}
     *         of the corresponding field.
     */
    private int ofstToPos(int ofst) {
        return this.fieldSignatures.size() - 1 - ofst;
    }

    //TODO overridden by array, refactor to avoid this
    @Override
    public boolean hasOffset(int ofst) {
        final int pos = ofstToPos(ofst);
        if (this.staticFields) {
            return (0 <= pos && pos < this.numOfStaticFields);
        } else {
            return (this.numOfStaticFields <= pos && pos < this.fieldSignatures.size());
        }
    }

    @Override
    public final Value getFieldValue(Signature sig) {
        try {
            return this.fields.get(sig).getValue();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public final Value getFieldValue(String fieldName, String fieldClass) {
        for (Signature sig: this.fieldSignatures) { //not very efficient but we don't care
            if (sig.getName().equals(fieldName) && sig.getClassName().equals(fieldClass)) {
                return getFieldValue(sig);
            }
        }
        return null;
    }

    @Override
    public final Value getFieldValue(int ofst) {
        try {
            return getFieldValue(this.fieldSignatures.get(ofstToPos(ofst)));
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    @Override
    public final void setFieldValue(Signature field, Value item) throws InvalidInputException {
        this.fields.get(field).setValue(item);
    }

    @Override
    public final void setFieldValue(String fieldName, String fieldClass, Value item) throws InvalidInputException {
        for (Signature sig: this.fieldSignatures) { //not very efficient but we don't care
            if (sig.getName().equals(fieldName) && sig.getClassName().equals(fieldClass)) {
            	setFieldValue(sig, item);
            }
        }
    }

    @Override
    public final void setFieldValue(int ofst, Value item) throws InvalidInputException {
        setFieldValue(this.fieldSignatures.get(ofstToPos(ofst)), item);
    }

    @Override
    public final Map<Signature, Variable> fields() {
        return Collections.unmodifiableMap(this.fields);
    }

    protected final HashMap<Signature, Variable> fieldsDeepCopy() {
        final HashMap<Signature, Variable> retVal = new HashMap<>();
        for (Signature key : this.fields.keySet()) {
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
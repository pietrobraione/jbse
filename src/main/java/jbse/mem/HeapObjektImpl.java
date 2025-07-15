package jbse.mem;

import java.util.Arrays;
import java.util.HashMap;

import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.bc.exc.FieldNotFoundException;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.val.Calculator;
import jbse.val.HistoryPoint;
import jbse.val.ReferenceSymbolic;
import jbse.val.Value;
import jbse.val.exc.InvalidTypeException;

/**
 * Base class for all classes that implement {@link HeapObjekt}s.
 */
public abstract class HeapObjektImpl extends ObjektImpl implements HeapObjekt {
    /**
     * Constructor.
     * 
     * @param calc a {@link Calculator}. It must not be {@code null}. It will
     *        only be used during object construction and will not be stored
     *        in this {@link HeapObjektImpl}.
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
    protected HeapObjektImpl(Calculator calc, boolean symbolic, ClassFile classFile, ReferenceSymbolic origin, HistoryPoint epoch, boolean staticFields) 
    throws InvalidInputException {
    	super(calc, symbolic, classFile, origin, epoch, staticFields);
    }
    
    abstract HeapObjektWrapper<? extends HeapObjektImpl> makeWrapper(Heap destinationHeap, long destinationPosition);

    
    @Override
    public void refine(Calculator calc, ClassFile classSub, State state) throws InvalidInputException {
    	if (classSub == null) {
    		throw new InvalidInputException("Attempted to refine with null arg.");
    	}
    	if (!classSub.isSubclass(this.classFile)) {
    		throw new InvalidInputException("Attempted to refine with a classfile that is not a subclass of the current classfile.");
    	}
    	if (!this.isSymbolic()) {
    		throw new InvalidInputException("Attempted to refine an object that is not symbolic.");
    	}

        this.numOfStaticFields = classSub.numOfStaticFields();
        this.fieldSignatures = Arrays.asList(classSub.getObjectFields().clone()); //safety copy - possibly useless
        final HashMap<Signature, Variable> fieldsOld = this.fields;
        this.fields = new HashMap<>();
        int curSlot = 0;
        for (Signature fieldSignature : this.fieldSignatures) {
        	if ((this.staticFields && curSlot < this.numOfStaticFields) ||
        	(!this.staticFields && curSlot >= this.numOfStaticFields)) {
        		if (this.fields.containsKey(fieldSignature)) {
        			this.fields.put(fieldSignature, fieldsOld.get(fieldSignature));
        		} else {
        			String fieldGenericSignatureType = null;
        			boolean found = false;
        			for (ClassFile cf : classSub.superclasses()) {
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
        			try {
						setFieldValue(fieldSignature, (Value) state.createSymbolMemberField(fieldSignature.getDescriptor(), fieldGenericSignatureType, getOrigin(), fieldSignature.getName(), fieldSignature.getClassName()));
					} catch (InvalidInputException | InvalidTypeException e) {
						//this should never happen
						throw new UnexpectedInternalException(e);
					}
        		}
        	}
        	++curSlot;
        }
        this.classFile = classSub;
    }
    
    @Override
    public HeapObjektImpl clone() {
    	return (HeapObjektImpl) super.clone();
        //note that we do not clone this.fields because
        //it is immutable for arrays and mutable for instances
        //so the two subclasses may either deep-copy it or share;
        //note also that the clone will have same
        //hash code as the original.
    }
}
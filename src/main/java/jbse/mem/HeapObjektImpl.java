package jbse.mem;

import jbse.bc.ClassFile;
import jbse.common.exc.InvalidInputException;
import jbse.val.Calculator;
import jbse.val.HistoryPoint;
import jbse.val.ReferenceSymbolic;

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
    public HeapObjektImpl clone() {
    	return (HeapObjektImpl) super.clone();
        //note that we do not clone this.fields because
        //it is immutable for arrays and mutable for instances
        //so the two subclasses may either deep-copy it or share;
        //note also that the clone will have same
        //hash code as the original.
    }
}
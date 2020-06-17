package jbse.mem;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.val.Calculator;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.ReferenceSymbolic;
import jbse.val.Simplex;
import jbse.val.Term;
import jbse.val.Value;
import jbse.val.exc.InvalidTypeException;

/**
 * Class that wraps an array in the heap, implementing 
 * copy-on-write.
 */
final class ArrayWrapper extends HeapObjektWrapper<ArrayImpl> implements Array {
    /**
     * Constructor.
     * 
     * @param destinationHeap the {@link Heap} where the clone of {@code instance} 
     *        must be put.
     * @param destinationPosition the position in {@code destinationHeap} where
     *        the clone must be put.
     * @param delegate the initial delegate, the {@link ArrayImpl} that must be 
     *        cloned upon writing.
     */
    ArrayWrapper(Heap destinationHeap, long destinationPosition, ArrayImpl delegate) {
        super(destinationHeap, destinationPosition, delegate);
    }

    @Override
    public void makeSymbolic(ReferenceSymbolic origin) throws InvalidInputException {
        possiblyCloneDelegate();
        getDelegate().makeSymbolic(origin);
    }

    @Override
    public Primitive getLength() {
        return getDelegate().getLength();
    }

    @Override
    public Term getIndex() {
        return getDelegate().getIndex();
    }

    @Override
    public boolean hasSimpleRep() {
        return getDelegate().hasSimpleRep();
    }

    @Override
    public boolean isSimple() {
        return getDelegate().isSimple();
    }
    
    @Override
    public void makeInitial() throws InvalidInputException {
    	throw new InvalidInputException("Attempted to makeInitial an array: Arrays can only be made initial through their constructors.");
    }

    @Override
    public AccessOutcome getFast(Calculator calc, Simplex index)
    throws InvalidInputException, InvalidTypeException, FastArrayAccessNotAllowedException {
        return getDelegate().getFast(calc, index);
    }

    @Override
    public Collection<AccessOutcome> get(Calculator calc, Primitive index) 
    throws InvalidInputException, InvalidTypeException {
        return getDelegate().get(calc, index);
    }

    @Override
    public void setFast(Simplex index, Value valToSet)
    throws InvalidInputException, InvalidTypeException, FastArrayAccessNotAllowedException {
        possiblyCloneDelegate();
        getDelegate().setFast(index, valToSet);
    }

    @Override
    public void set(Calculator calc, Primitive index, Value valToSet) 
    throws InvalidInputException, InvalidTypeException {
        possiblyCloneDelegate();
        getDelegate().set(calc, index, valToSet);
    }

    @Override
    public Iterator<? extends AccessOutcomeIn> entries() {
        possiblyCloneDelegate();
        return getDelegate().entries();
    }

    @Override
    public Iterator<? extends AccessOutcomeIn> entriesPossiblyAffectedByAccess(Calculator calc, Primitive index, Value valToSet) 
    throws InvalidInputException {
        possiblyCloneDelegate();
        return getDelegate().entriesPossiblyAffectedByAccess(calc, index, valToSet);
    }

    @Override
    public void cloneEntries(Array src, Calculator calc) throws InvalidInputException, InvalidTypeException {
        possiblyCloneDelegate();
        getDelegate().cloneEntries(src, calc);
    }

    @Override
    public Iterator<? extends AccessOutcomeIn> arraycopy(Calculator calc, Array src, Primitive srcPos, Primitive destPos,
                                                         Primitive length, Consumer<Reference> checkOk) 
    throws InvalidInputException, InvalidTypeException {
        possiblyCloneDelegate();
        return getDelegate().arraycopy(calc, src, srcPos, destPos, length, checkOk);
    }

    @Override
    public Primitive inRange(Calculator calc, Primitive index) 
    throws InvalidInputException, InvalidTypeException {
        return getDelegate().inRange(calc, index);
    }

    @Override
    public Primitive outOfRange(Calculator calc, Primitive index) 
    throws InvalidInputException, InvalidTypeException {
        return getDelegate().outOfRange(calc, index);
    }

    @Override
    public List<AccessOutcomeIn> values() {
        return getDelegate().values();
    }

    @Override
    public String valueString() {
        return getDelegate().valueString();
    }

    @Override
    public Array clone() {
        //a wrapper shall never be cloned
        throw new UnexpectedInternalException("Tried to clone an ArrayWrapper.");
    }
}
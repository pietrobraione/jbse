package jbse.mem;

import static jbse.common.Type.ARRAYOF;
import static jbse.common.Type.INT;
import static jbse.common.Type.NULLREF;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.getArrayMemberType;
import static jbse.common.Type.isPrimitive;
import static jbse.common.Type.toPrimitiveOrVoidInternalName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.Array.AccessOutcome;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.val.Calculator;
import jbse.val.Expression;
import jbse.val.HistoryPoint;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.ReferenceArrayImmaterial;
import jbse.val.ReferenceSymbolic;
import jbse.val.Simplex;
import jbse.val.Term;
import jbse.val.Value;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * Class implementing an array. Upon access it returns a 
 * collection of {@link AccessOutcome}s, 
 * associating {@link Expression}s on the array index to 
 * the outcome of an array access when the access index satisfies it.
 *  
 * @author Pietro Braione
 */
public final class ArrayImpl extends ObjektImpl implements Array {
    /*Fields*/

    /** 
     * {@code true} iff this array is an initial array, i.e., 
     * if it is the (immutable) initial array with its origin
     * that just stores its refinement and backs the array 
     * with same origin that is mutated by symbolic execution. 
     */
    private final boolean isInitial;

    /** 
     * The conventional term used for indicating the array's index.
     * Note that it cannot be declared static because it depends on 
     * the {@link Calculator} stored in the array.
     */
    private final Term INDEX;

    /** The {@link Calculator}. */
    private final Calculator calc;

    /** The signature of the length field. */
    private final Signature lengthSignature;

    /** An {@link Expression} stating that {@code INDEX} is in range. */
    private final Expression indexInRange;

    /** Describes the values stored in the array. */
    private LinkedList<AccessOutcomeInImpl> entries; //TODO do not use AccessOutcome..., but define a suitable private Entry class

    /** 
     * Indicates whether the array has a simple representation, i.e., 
     * whether it has as many entries as its length, each corresponding 
     * to all the possible values of the index, and ordered by index. 
     * This is possible only if {@code this.length} is a {@link Simplex}.
     */ 
    private boolean simpleRep;

    public abstract class AccessOutcomeImpl implements AccessOutcome {
        /** 
         * An {@link Expression} denoting the condition over 
         * the array index yielding this {@link AccessOutcome}. 
         * The {@code null} value denotes {@code true}, either 
         * trivially because of concrete access, or explicit.
         */
        protected Expression accessCondition;

        /**
         * Constructor (outcome returned by a concrete get).
         */
        private AccessOutcomeImpl() {
            this.accessCondition = null;
        }

        /** 
         * Constructor (outcome returned by a nonconcrete get or
         * stored in array entries).
         * 
         * @param accessCondition An {@link Expression} denoting a  
         *        condition over the array index. When {@code null} 
         *        denotes {@code true}.
         */
        private AccessOutcomeImpl(Expression accessCondition) { 
            this.accessCondition = accessCondition;  
        }

        @Override
        public Expression getAccessCondition() { 
            return this.accessCondition; 
        }

        /**
         * Strengthens the access condition of this {@link AccessOutcome}. 
         * 
         * @param condition an {@link Expression} This {@link AccessOutcome}'s 
         *        access condition will be strengthened by conjoining it
         *        with {@code condition}.
         * @throws InvalidOperandException if {@code condition} is {@code null}. 
         * @throws InvalidTypeException if {@code condition} has not boolean type.
         */
        void strengthenAccessCondition(Expression condition) 
        throws InvalidOperandException, InvalidTypeException {
            if (this.accessCondition == null) {
                this.accessCondition = condition;
            } else {
                this.accessCondition = (Expression) this.accessCondition.and(condition);
            }
        }

        @Override
        public void excludeIndexFromAccessCondition(Primitive val)
        throws InvalidOperandException, InvalidTypeException {
            if (val.getType() != INT) {
                throw new InvalidTypeException("attempted array access with index of type " + val.getType());
            }
            final Expression indexIsDifferentFromVal = (Expression) INDEX.eq(val).not();
            try {
                strengthenAccessCondition(indexIsDifferentFromVal);
            } catch (InvalidTypeException e) {
                //this should never happen
                throw new UnexpectedInternalException(e);
            }
        }

        @Override
        public Primitive inRange(Primitive accessIndex) 
        throws InvalidOperandException, InvalidTypeException { 
            return this.accessCondition.replace(INDEX, accessIndex); 
        }
    }

    public abstract class AccessOutcomeInImpl extends AccessOutcomeImpl implements AccessOutcomeIn { 
        /**
         * Constructor (outcome returned by a concrete get).
         */
        private AccessOutcomeInImpl() {
            super();
        }

        /** 
         * Constructor (outcome returned by a nonconcrete get or
         * stored in array entries).
         * 
         * @param accessCondition An {@link Expression} denoting a  
         *        condition over the array index. When {@code null} 
         *        denotes {@code true}.
         */
        private AccessOutcomeInImpl(Expression accessCondition) { 
            super(accessCondition);  
        }

        @Override
        public AccessOutcomeInImpl clone() {
            try {
                return (AccessOutcomeInImpl) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new InternalError(e);
            }
        }
    }

    public final class AccessOutcomeInInitialArrayImpl extends AccessOutcomeInImpl implements AccessOutcomeInInitialArray {
        /**
         * A reference to the other (symbolic) {@link ArrayImpl} that backs 
         * this array.
         */
        private Reference initialArray;

        /**
         * The offset through which {@code initialArray}
         * should be accessed.
         */
        private Primitive offset;

        private AccessOutcomeInInitialArrayImpl(Reference initialArray) 
        throws InvalidOperandException {
            this(initialArray, ArrayImpl.this.calc.valInt(0));
        }

        private AccessOutcomeInInitialArrayImpl(Expression accessCondition, Reference initialArray) 
        throws InvalidOperandException {
            this(accessCondition, initialArray, ArrayImpl.this.calc.valInt(0));
        }

        private AccessOutcomeInInitialArrayImpl(Reference initialArray, Primitive offset) 
        throws InvalidOperandException {
            super();
            if (initialArray == null || offset == null) {
                throw new InvalidOperandException("tried to create an AccessOutcomeInitialArray with null initial array origin or null offset");
            }
            this.initialArray = initialArray;
            this.offset = offset;
        }

        private AccessOutcomeInInitialArrayImpl(Expression accessCondition, Reference initialArray, Primitive offset) 
        throws InvalidOperandException {
            super(accessCondition);
            if (initialArray == null || offset == null) {
                throw new InvalidOperandException("tried to create an AccessOutcomeInitialArray with null initial array origin or null offset");
            }
            this.initialArray = initialArray;
            this.offset = offset;
        }

        @Override
        public Reference getInitialArray() {
            return this.initialArray;
        }

        @Override
        public Primitive getOffset() {
            return this.offset;
        }

        @Override
        public AccessOutcomeInInitialArrayImpl clone() {
            return (AccessOutcomeInInitialArrayImpl) super.clone();
        }

        @Override
        public String toString() {
            return (this.accessCondition == null ? "true" : this.accessCondition.toString()) + 
            " -> " + (this.initialArray.toString() + "[_ + " + this.offset.toString() + "]");
        }
    }

    public final class AccessOutcomeInValueImpl extends AccessOutcomeInImpl implements AccessOutcomeInValue {
        /**
         * A {@link Value} denoting the value returned  
         * by the array access. It can be either a 
         * {@link Value} of the array member type, 
         * or the special {@link ReferenceArrayImmaterial} 
         * value denoting a reference to another array 
         * not yet available in the state's heap, or 
         * {@code null} if the array is an initial symbolic
         * array and no assumption is yet made on the value
         * returned by the access.
         */
        private Value returnedValue;

        /**
         * Constructor (outcome returned by a concrete get).
         * 
         * @param returnedValue A {@link Value} denoting the value returned  
         *        by the array access. It 
         *        can be either a {@link Value} of the array member type, 
         *        or the special {@link ReferenceArrayImmaterial} value denoting 
         *        a reference to another array not yet available in the state's heap,
         *        or {@code null} if the value is unknown.
         */
        private AccessOutcomeInValueImpl(Value returnedValue) {
            super();
            this.returnedValue = returnedValue;
        }

        /**
         * Constructor (outcome returned by a nonconcrete get or
         * stored in array entries).
         * 
         * @param accessCondition an {@link Expression} denoting a  
         *        condition over the array index. 
         * @param returnedValue a {@link Value} denoting the value returned  
         *        by an array access with index satisfying {@code exp}. It 
         *        can be a {@link Value} of the array member type, 
         *        or the special {@link ReferenceArrayImmaterial} value denoting 
         *        a reference to another array not yet available in the state's heap,
         *        or {@code null} if the value is unknown.
         */
        private AccessOutcomeInValueImpl(Expression accessCondition, Value returnedValue) {
            super(accessCondition);
            this.returnedValue = returnedValue;
        }

        @Override
        public Value getValue() { return this.returnedValue; }
        
        @Override
        public void setValue(Value newValue) throws InvalidTypeException {
        	ArrayImpl.this.checkSetValue(newValue);
        	this.returnedValue = newValue;
        }

        @Override
        public AccessOutcomeInValueImpl clone() {
            return (AccessOutcomeInValueImpl) super.clone();
        }

        @Override
        public String toString() {
            return (this.accessCondition == null ? "true" : this.accessCondition.toString()) + 
            " -> " + (this.returnedValue == null ? "?" : this.returnedValue.toString());
        }
    }

    public final class AccessOutcomeOutImpl extends AccessOutcomeImpl implements AccessOutcomeOut { 
        /**
         * Constructor (outcome returned by a concrete get).
         */
        private AccessOutcomeOutImpl() {
            super();
        }

        /**
         * Constructor (outcome returned by a nonconcrete get).
         * 
         * @param accessCondition An {@link Expression} denoting a  
         *        condition over the array index. 
         *        When {@code null} denotes {@code true}. 
         */
        private AccessOutcomeOutImpl(Expression accessCondition) { 
            super(accessCondition); 
        }

        @Override
        public String toString() {
            return (this.accessCondition == null ? "true" : this.accessCondition.toString()) + 
            " -> OUT_OF_RANGE";
        }
    }

    /**
     * Constructor.
     * 
     * @param symbolic a {@code boolean}, whether this object is symbolic
     *        (i.e., not explicitly created during symbolic execution by
     *        a {@code new*} bytecode, but rather assumed).     
     * @param calc a {@code Calculator}.
     * @param initSymbolic {@code true} iff the array must be initialized 
     *        with symbolic values.
     * @param initValue a {@link Value} for initializing the array (ignored
     *        whenever {@code initSymbolic == true}); if {@code initValue == null}
     *        the default value for the array member type is used for initialization.
     * @param length a {@link Primitive}, the number of elements in the array.
     * @param classFile a {@code classFile}, the class of 
     *        this {@link Instance}; It must be {@code classFile.}{@link ClassFile#isReference() isArray}{@code () == true}.
     * @param origin the {@link ReferenceSymbolic} providing origin of 
     *        the {@code Array}, if symbolic, or {@code null}, if concrete.
     * @param epoch the creation {@link HistoryPoint} of the {@link ArrayImpl}.
     * @param isInitial {@code true} iff this array is not an array of the 
     *        current state, but a copy of an (immutable) symbolic array in
     *        the initial state.
     * @param maxSimpleArrayLength an {@code int}, the maximum length an array may have
     *        to be granted simple representation.
     * @throws InvalidTypeException iff {@code classFile} is invalid. 
     */
    public ArrayImpl(boolean symbolic, Calculator calc, boolean initSymbolic, Value initValue, Primitive length, ClassFile classFile, ReferenceSymbolic origin, HistoryPoint epoch, boolean isInitial, int maxSimpleArrayLength) 
    throws InvalidTypeException {
        super(symbolic, calc, classFile, origin, epoch, false, 0, new Signature(classFile.getClassName(), "" + INT, "length"));
        if (classFile == null || !classFile.isArray()) {
            throw new InvalidTypeException("Attempted creation of an array with type " + classFile.getClassName());
        }
        this.isInitial = isInitial;
        this.lengthSignature = new Signature(classFile.getClassName(), "" + INT, "length");
        this.calc = calc;
        try {
            this.INDEX = this.calc.valTerm(INT, INDEX_ID);
        } catch (InvalidTypeException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
        setFieldValue(this.lengthSignature, length);
        try {
            final Expression indexGreaterThanZero = (Expression) INDEX.ge(this.calc.valInt(0));
            final Expression indexLessThanLength = (Expression) INDEX.lt(length);
            this.indexInRange  = (Expression) indexGreaterThanZero.and(indexLessThanLength);		
        } catch (InvalidOperandException | InvalidTypeException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
        setEntriesInit(initSymbolic, initValue, maxSimpleArrayLength);
    }

    /**
     * Constructor.
     * 
     * @param referenceToOtherArray a {@link Reference} to an {@link ArrayImpl} that backs this array.
     * @param otherArray the {@link ArrayImpl} that backs this array.
     * @throws InvalidOperandException if {@code referenceToOtherArray == null}.
     * @throws NullPointerException if {@code otherArray == null}.
     */
    public ArrayImpl(Reference referenceToOtherArray, ArrayImpl otherArray) throws InvalidOperandException {
        super(otherArray.isSymbolic(), otherArray.calc, otherArray.classFile, otherArray.getOrigin(), otherArray.historyPoint(), false, 0, new Signature(otherArray.classFile.getClassName(), "" + INT, "length"));
        //TODO assert other is an initial symbolic array
        this.isInitial = false;
        this.lengthSignature = new Signature(this.classFile.getClassName(), "" + INT, "length");
        this.calc = otherArray.calc;
        try {
            this.INDEX = this.calc.valTerm(INT, INDEX_ID);
        } catch (InvalidTypeException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
        this.fields.get(this.lengthSignature).setValue(otherArray.getLength());
        try {
            final Expression indexGreaterThanZero = (Expression) INDEX.ge(this.calc.valInt(0));
            final Expression indexLessThanLength = (Expression) INDEX.lt(getLength());
            this.indexInRange  = (Expression) indexGreaterThanZero.and(indexLessThanLength);		
        } catch (InvalidOperandException | InvalidTypeException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
        this.entries = new LinkedList<>();
        this.entries.add(new AccessOutcomeInInitialArrayImpl(this.indexInRange, referenceToOtherArray));
    }

    private void setEntriesInit(boolean initSymbolic, Value initValue, int maxSimpleArrayLength) {
        final Value entryValue;
        if (initSymbolic) {
            entryValue = null;
        } else if (initValue == null) {
            entryValue = this.calc.createDefault(getArrayMemberType(this.classFile.getClassName()).charAt(0)); 
        } else {
            entryValue = initValue;
        }

        //in the case length is concrete and not too high, creates an entry for each 
        //possible value in the range (simple representation); the rationale is, it 
        //is better having more, restrictive entries than less, liberal entries, since 
        //most workload is on the theorem prover side, and with restrictive entries 
        //we may hope that normalization will succeed upon array access, thus reducing 
        //the calls to the prover. Of course there is a complementary risk, i.e., that
        //having many entries results in the creation of many branches. 
        this.entries = new LinkedList<>();
        if (getLength() instanceof Simplex) {
            final int ln = ((Integer) ((Simplex) getLength()).getActualValue()).intValue();
            if (ln <= maxSimpleArrayLength) {
                this.simpleRep = true;
                for (int i = 0; i < ln; i++) {
                    try {
                        this.entries.add(new AccessOutcomeInValueImpl((Expression) INDEX.eq(this.calc.valInt(i)),
                                                                  entryValue));
                    } catch (InvalidOperandException | InvalidTypeException e) {
                        //this should never happen
                        throw new UnexpectedInternalException(e);
                    }
                }
                return;
            }
        }
        //otherwise, do not use simple representation
        this.simpleRep = false;
        this.entries.add(new AccessOutcomeInValueImpl(this.indexInRange, entryValue));
    }
    
    @Override
    ObjektWrapper<? extends ObjektImpl> makeWrapper(Heap destinationHeap, long destinationPosition) {
    	return new ArrayWrapper(destinationHeap, destinationPosition, this);
    }

    @Override
    public Primitive getLength() {
        return (Primitive) getFieldValue(this.lengthSignature);
    }
    
    @Override
    public Term getIndex() {
    	return this.INDEX;
    }

    @Override
    public boolean hasSimpleRep() {
        return this.simpleRep;
    }

    @Override
    public boolean isSimple() {
        if (hasSimpleRep()) {
            for (AccessOutcomeIn e : this.entries) {
                if (!(e instanceof AccessOutcomeInValue) || ((AccessOutcomeInValue) e).getValue().isSymbolic()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isInitial() {
        return this.isInitial;
    }
    
    @Override
    public AccessOutcome getFast(Simplex index)
    throws InvalidOperandException, InvalidTypeException, FastArrayAccessNotAllowedException {
        if (index == null) {
            throw new InvalidOperandException("attempted array access with null index");
        }
        if (index.getType() != INT) {
            throw new InvalidTypeException("attempted array access with an index with type " + index.getType());
        }
        if (!this.simpleRep) {
            throw new FastArrayAccessNotAllowedException();
        }
        return get(index).iterator().next();
    }

    @Override
    public Collection<AccessOutcome> get(Primitive index) 
    throws InvalidOperandException, InvalidTypeException {
        final ArrayList<AccessOutcome> retVal = new ArrayList<>();
        final Primitive inRange = inRange(index);

        //builds the answer
        if (hasSimpleRep() && index instanceof Simplex) { 
            //the fast case, access this.values directly by index			
            if (inRange.surelyTrue()) {
                final int indexInt = (Integer) ((Simplex) index).getActualValue();
                final AccessOutcomeIn e = this.entries.get(indexInt);
                if (e instanceof AccessOutcomeInValue) {
                    retVal.add(new AccessOutcomeInValueImpl(((AccessOutcomeInValue) e).getValue()));
                } else { //e instanceof AccessOutcomeInInitialArray
                    final AccessOutcomeInInitialArray eCast = (AccessOutcomeInInitialArray) e;
                    retVal.add(new AccessOutcomeInInitialArrayImpl(eCast.getInitialArray(), eCast.getOffset()));
                }
            } else {
                retVal.add(new AccessOutcomeOutImpl()); 
            }
        } else {
            //scans the entries and adds all the (possibly) satisfiable 
            //inbound cases
            for (AccessOutcomeIn e : this.entries) {
                final Primitive inRangeEntry = e.inRange(index);
                if (inRangeEntry.surelyTrue()) { //this may only happen when index is Simplex
                    if (e instanceof AccessOutcomeInValue) {
                        retVal.add(new AccessOutcomeInValueImpl(((AccessOutcomeInValue) e).getValue()));
                    } else { //e instanceof AccessOutcomeInInitialArray
                        final AccessOutcomeInInitialArray eCast = (AccessOutcomeInInitialArray) e;
                        retVal.add(new AccessOutcomeInInitialArrayImpl(eCast.getInitialArray(), eCast.getOffset()));						
                    }
                } else if (inRangeEntry.surelyFalse()) {
                    //do nothing (not returned in the result)
                } else { //inRangeEntry is possibly satisfiable
                	//TODO is the next block equivalent to just cloning e?
                    if (e instanceof AccessOutcomeInValue) {
                        retVal.add(new AccessOutcomeInValueImpl(e.getAccessCondition(), ((AccessOutcomeInValue) e).getValue()));
                    } else { //e instanceof AccessOutcomeInInitialArray
                        final AccessOutcomeInInitialArray eCast = (AccessOutcomeInInitialArray) e;
                        retVal.add(new AccessOutcomeInInitialArrayImpl(e.getAccessCondition(), eCast.getInitialArray(), eCast.getOffset()));						
                    }
                }
            }

            //manages the out-of-bounds case
            final Primitive outOfRange = outOfRange(index);
            if (outOfRange.surelyTrue()) {
                retVal.add(new AccessOutcomeOutImpl());
            } else if (outOfRange.surelyFalse()) {
                //do nothing
            } else { //outOfRange is possibly satisfiable
                retVal.add(new AccessOutcomeOutImpl((Expression) this.indexInRange.not()));
            }
        }

        return retVal;
    }
    
    /**
     * Very lenient checks before setting the array.
     *  
     * @param newValue the Value which is to be written into the array.
     * @throws InvalidTypeException if {@code newValue} has not a valid type.
     */
    private void checkSetValue(Value newValue) throws InvalidTypeException {
    	if (newValue == null) {
    		return; //in some cases means unknown value, so we accept it
    	}
		final ClassFile arrayMemberClass = ArrayImpl.this.getType().getMemberClass();
		if (arrayMemberClass.isPrimitiveOrVoid() && newValue.getType() != toPrimitiveOrVoidInternalName(arrayMemberClass.getClassName())) {
			throw new InvalidTypeException("Attempted to set an array with member type " + arrayMemberClass.getClassName() + " with a value with type " + newValue.getType() + ".");
		}
		if ((arrayMemberClass.isArray() || arrayMemberClass.isReference()) && 
			newValue.getType() != ARRAYOF && newValue.getType() != REFERENCE && newValue.getType() != NULLREF) {
			throw new InvalidTypeException("Attempted to set an array with member type " + arrayMemberClass.getClassName() + " with a value with type " + newValue.getType() + ".");
		}
    }

    @Override
    public void setFast(Simplex index, Value newValue) 
    throws InvalidOperandException, InvalidTypeException, FastArrayAccessNotAllowedException {
        if (index == null) {
            throw new InvalidOperandException("attempted array access with null index");
        }
        if (index.getType() != INT) {
            throw new InvalidTypeException("attempted array access with an index with type " + index.getType());
        }
        if (!this.simpleRep) {
            throw new FastArrayAccessNotAllowedException();
        }
        checkSetValue(newValue);
        final int actualIndex = (Integer) index.getActualValue();
        final int actualLength = (Integer) ((Simplex) this.getLength()).getActualValue();
        if (actualIndex >= 0 && actualIndex < actualLength) {
            final AccessOutcomeIn e = this.entries.get(actualIndex);
            if (e instanceof AccessOutcomeInValueImpl) {
                ((AccessOutcomeInValueImpl) e).returnedValue = newValue;
            } else {
                final AccessOutcomeInValueImpl eNew = new AccessOutcomeInValueImpl(e.getAccessCondition(), newValue);
                this.entries.set(actualIndex, eNew);
            }
        } 	//TODO else throw an exception???
    }

    @Override
    public void set(final Primitive index, final Value newValue)
    throws InvalidOperandException, InvalidTypeException {
        if (index == null) {
            throw new InvalidOperandException("attempted array access with null index");
        }
        if (index.getType() != INT) {
            throw new InvalidTypeException("attempted array access with an index with type " + index.getType());
        }
        checkSetValue(newValue);
        this.simpleRep = false;
        final Expression formalIndexIsSetIndex = (Expression) INDEX.eq(index);
        final Expression accessExpression = (Expression) this.indexInRange.and(formalIndexIsSetIndex); //if we assume that index may be in range, this is an Expression
        this.entries.add(new AccessOutcomeInValueImpl(accessExpression, newValue));
    }
    
    @Override
    public Iterator<? extends AccessOutcomeIn> entries() {
    	return this.entries.iterator();
    }
    
    @Override
    public Iterator<? extends AccessOutcomeIn> entriesPossiblyAffectedByAccess(final Primitive index, final Value valToSet) {
        return new Iterator<AccessOutcomeIn>() {
            //this iterator filters the relevant members in Array.this.values
            //by wrapping the default iterator to it
            private final Iterator<AccessOutcomeInImpl> it = ArrayImpl.this.entries.iterator();
            private ArrayImpl.AccessOutcomeIn next = null;
            private boolean emitted = true;
            private boolean canRemove = false;

            private void findNext() {
                this.next = null;
                //looks for the next entry possibly affected by the set operation
                while (this.it.hasNext()) {
                    final AccessOutcomeIn e = this.it.next();

                    //determines whether the entry is possibly affected by the set
                    //operation
                    boolean entryAffected;
                    try {
                        entryAffected = !e.inRange(index).surelyFalse() && 
                        (e instanceof AccessOutcomeInInitialArray || ((AccessOutcomeInValue) e).getValue() == null || !((AccessOutcomeInValue) e).getValue().equals(valToSet));
                    } catch (InvalidOperandException | InvalidTypeException exc) {
                        //this should never happen because index was already checked
                        throw new UnexpectedInternalException(exc);
                    }

                    //if the entry is possibly affected, it is the next value
                    if (entryAffected) {
                        this.next = e;
                        return;
                    }
                }
            }

            @Override
            public boolean hasNext() {
                if (this.emitted) {
                    try {
                        findNext();
                    } catch (UnexpectedInternalException e) {
                        throw new RuntimeException(e);
                    }
                }
                this.emitted = false;
                this.canRemove = false;
                return (this.next != null);
            }

            @Override
            public AccessOutcomeIn next() {
                if (this.emitted) {
                    try {
                        findNext();
                    } catch (UnexpectedInternalException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (this.next == null) {
                    throw new NoSuchElementException();
                }
                this.emitted = true;
                this.canRemove = true;
                return this.next;
            }

            @Override
            public void remove() {
                if (this.canRemove) { 
                    it.remove();
                } else {
                    throw new IllegalStateException();
                }
            }
        };
    }
    
    @Override
    public void cloneEntries(Array src) throws InvalidTypeException {
    	final ArrayImpl otherImpl;
    	if (src instanceof ArrayImpl) {
    		otherImpl = (ArrayImpl) src;
    	} else {
    		otherImpl = ((ArrayWrapper) src).getDelegate();
    	}

    	if (!this.classFile.equals(otherImpl.classFile)) {
    		throw new InvalidTypeException("tried to clone entries of a " + otherImpl.classFile + " array into a " + this.classFile + " array");
    	}
    	this.entries.clear();
    	for (AccessOutcomeInImpl entry : otherImpl.entries) {
    		final AccessOutcomeInImpl entryClone = entry.clone();
    		try {
    			entryClone.accessCondition = (Expression) entryClone.accessCondition.replace(this.INDEX, otherImpl.INDEX);
    		} catch (InvalidTypeException | InvalidOperandException e) {
    			//this should never happen
    			throw new UnexpectedInternalException(e);
    		}
    		this.entries.add(entry.clone());
    	}
    }

    /** An iterator that terminates instantaneously. */
    private static final Iterator<AccessOutcomeIn> EMPTY_ITERATOR = 
    new Iterator<AccessOutcomeIn>() {
        @Override public boolean hasNext() { return false; }
        @Override public AccessOutcomeIn next() { throw new NoSuchElementException(); }
        @Override public void remove() { throw new UnsupportedOperationException(); }
    };

    @Override
    public Iterator<? extends AccessOutcomeIn> arraycopy(Array src, Primitive srcPos, Primitive destPos, Primitive length, Consumer<Reference> checkOk) 
    throws InvalidOperandException, InvalidTypeException {
    	final ArrayImpl srcImpl;
    	if (src instanceof ArrayImpl) {
    		srcImpl = (ArrayImpl) src;
    	} else {
    		srcImpl = ((ArrayWrapper) src).getDelegate();
    	}
    	final String srcTypeComponent = getArrayMemberType(src.getType().getClassName());
    	final String destTypeComponent = getArrayMemberType(getType().getClassName());
    	if (this.simpleRep && srcImpl.simpleRep && 
    			srcPos instanceof Simplex && destPos instanceof Simplex && 
    			length instanceof Simplex) {
    		//fast operation
    		int srcPosInt = ((Integer) ((Simplex) srcPos).getActualValue()).intValue();
    		int destPosInt = ((Integer) ((Simplex) destPos).getActualValue()).intValue();
    		int lengthInt = ((Integer) ((Simplex) length).getActualValue()).intValue();
    		final ArrayList<Integer> destPosEntries = new ArrayList<>(); //buffer to avoid concurrent modification when this == srcImpl
                final ArrayList<AccessOutcomeInImpl> destEntries = new ArrayList<>(); //buffer to avoid concurrent modification when this == srcImpl
    		for (int ofst = 0; ofst < lengthInt; ++ofst) {
    			final AccessOutcomeIn srcEntry = srcImpl.entries.get(srcPosInt + ofst);
    			final AccessOutcomeInImpl destEntry;
    			if (srcEntry instanceof AccessOutcomeInValue) {
    				final Value srcValue = ((AccessOutcomeInValue) srcEntry).getValue();
    				if (!isPrimitive(srcTypeComponent) && !isPrimitive(destTypeComponent)) { 
    					checkOk.accept((Reference) srcValue);
    				}
    				destEntry = new AccessOutcomeInValueImpl(srcEntry.getAccessCondition(), srcValue);
    			} else { //srcEntry instanceof AccessOutcomeInInitialArray
    				final Reference initialArray = ((AccessOutcomeInInitialArray) srcEntry).getInitialArray();
    				final Primitive offset = ((AccessOutcomeInInitialArray) srcEntry).getOffset();
    				//TODO find a way to perform assignment compatibility check
    				destEntry = new AccessOutcomeInInitialArrayImpl(srcEntry.getAccessCondition(), initialArray, offset.sub(destPos).add(srcPos));
    			}
    			destPosEntries.add(destPosInt + ofst);
    			destEntries.add(destEntry);
    		}
    		for (int i = 0; i < destPosEntries.size(); ++i) {
                    this.entries.set(destPosEntries.get(i), destEntries.get(i));
    		}
    		return EMPTY_ITERATOR;
    	} else {
    		this.simpleRep = false;
    		final Expression indexInDestRange = (Expression) INDEX.ge(destPos).and(INDEX.lt(destPos.add(length)));
    		final Expression indexNotInDestRange = (Expression) indexInDestRange.not();

    		//constrains the entries of the destination array
    		for (AccessOutcomeInImpl destEntry : this.entries) {
    			destEntry.strengthenAccessCondition(indexNotInDestRange);
    		}

    		//adds new entries corresponding to the source array entries
    		final Primitive srcIndex = INDEX.sub(destPos).add(srcPos);
    		final ArrayList<AccessOutcomeInImpl> destEntries = new ArrayList<>(); //buffer to avoid concurrent modification when this == srcImpl
    		for (AccessOutcomeIn srcEntry : srcImpl.entries) {
    			final Expression accessCondition = (Expression) this.indexInRange.and(srcEntry.inRange(srcIndex)).and(indexInDestRange);
    			final AccessOutcomeInImpl destEntry;
    			if (srcEntry instanceof AccessOutcomeInValue) {
    				final Value srcValue = ((AccessOutcomeInValue) srcEntry).getValue();
    				if (!isPrimitive(srcTypeComponent) && !isPrimitive(destTypeComponent)) { 
    					checkOk.accept((Reference) srcValue);
    				}
    				destEntry = new AccessOutcomeInValueImpl(accessCondition, srcValue);
    			} else { //srcEntry instanceof AccessOutcomeInInitialArray
    				final Reference initialArray = ((AccessOutcomeInInitialArray) srcEntry).getInitialArray();
    				final Primitive offset = ((AccessOutcomeInInitialArray) srcEntry).getOffset();
    				//TODO find a way to perform assignment compatibility check
    				destEntry = new AccessOutcomeInInitialArrayImpl(accessCondition, initialArray, offset.sub(destPos).add(srcPos));
    			}
    			destEntries.add(destEntry);
    		}
    		for (AccessOutcomeInImpl destEntry : destEntries) {
                    this.entries.add(destEntry);
    		}

    		//returns the iterator
    		return ArrayImpl.this.entries.iterator(); //for sake of simplicity all the entries are considered potentially affected
    	}
    }

    @Override
    public Primitive inRange(Primitive index) 
    throws InvalidOperandException, InvalidTypeException {
    	return this.indexInRange.replace(this.INDEX, index);
    }

    @Override
    public Primitive outOfRange(Primitive index) 
    throws InvalidOperandException, InvalidTypeException {
    	final Primitive retVal = inRange(index).not();
    	return retVal;
    }

    @Override
    public List<AccessOutcomeIn> values() {
    	final ArrayList<AccessOutcomeIn> retVal = new ArrayList<>();
    	for (AccessOutcomeIn entry : this.entries) {
    		retVal.add(entry.clone());
    	}
    	return retVal;
    }

    @Override
    public String valueString() {
    	if (this.classFile.getMemberClass().getClassName().equals("char") && isSimple()) {
    		final StringBuilder buf = new StringBuilder();
    		for (AccessOutcomeIn e : this.entries) {
    			buf.append(((AccessOutcomeInValue) e).getValue().toString().substring(1, 2));
    		}
    		return buf.toString();
    	} else {
    		return null;
    	}
    }

    @Override
    public boolean hasSlot(int slot) {
    	return (hasSimpleRep() ? 0 <= slot && slot <= ((Integer) ((Simplex) getLength()).getActualValue()).intValue() : false);
    }

    @Override
    public String toString() {
    	String str = "[Type:" + this.classFile + ", Length:" + this.getLength().toString() + ", Elements: {";
    	boolean firstEntryPassed = false;
    	final StringBuilder buf = new StringBuilder();
    	for (AccessOutcomeIn e : this.entries) {
    		if (firstEntryPassed) {
    			buf.append(", ");
    		} else {
    			firstEntryPassed = true;
    		}
    		buf.append(e.toString()); 
    	}
    	str += buf.toString() + "}]";
    	return str;
    }

    @Override
    public ArrayImpl clone() {
    	final ArrayImpl o = (ArrayImpl) super.clone();

    	o.entries = new LinkedList<>();
    	for (AccessOutcomeInImpl e : this.entries) {
    		o.entries.add(e.clone());
    	}

    	return o;
    }
}

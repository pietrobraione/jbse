package jbse.mem;

import static jbse.common.Type.getArrayMemberType;
import static jbse.common.Type.isPrimitive;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import jbse.bc.Signature;
import jbse.common.Type;
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.val.Calculator;
import jbse.val.Expression;
import jbse.val.MemoryPath;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.ReferenceArrayImmaterial;
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
public final class Array extends Objekt {
    /** 
     * The {@link String} identifier of the {@link Term} used to
     * represent an {@link Array}'s index.
     */
    public static final String INDEX_ID = "{INDEX}";

    private static final int T_BOOLEAN = 4;
    private static final int T_CHAR    = 5;
    private static final int T_FLOAT   = 6;
    private static final int T_DOUBLE  = 7;
    private static final int T_BYTE    = 8;
    private static final int T_SHORT   = 9;
    private static final int T_INT     = 10;
    private static final int T_LONG    = 11;

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
    private LinkedList<AccessOutcomeIn> entries; //TODO do not use AccessOutcome..., but define a suitable private Entry class

    /** 
     * Indicates whether the array has a simple representation, i.e., 
     * whether it has as many entries as its length, each corresponding 
     * to all the possible values of the index, and ordered by index. 
     * This is possible only if {@code this.length} is a {@link Simplex}.
     */ 
    private boolean simpleRep;

    /**
     * The outcome of an array access. An 
     * {@link AccessOutcome} is a pair (condition, result), 
     * where the condition is a predicate over the array 
     * index, and result expresses what happens when the array is   
     * accessed with an index satisfying the condition.
     * 
     * @author Pietro Braione
     */
    public abstract class AccessOutcome {
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
        private AccessOutcome() {
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
        private AccessOutcome(Expression accessCondition) { 
            this.accessCondition = accessCondition;  
        }

        /**
         * Gets the constraint over the symbolic values of the execution 
         * under which the array access yields this outcome.
         * 
         * @return an {@link Expression}, or {@code null} for denoting
         *         {@code true}.
         */
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

        /**
         * Strengthens the access condition of this {@link AccessOutcome}
         * by imposing that the index is different from a value. 
         * 
         * @param val a {@link Primitive} This {@link AccessOutcome}'s 
         *        access condition will be strengthened by conjoining it
         *        with an expression stating that the index is different
         *        from {@code val}.
         * @throws InvalidOperandException if {@code val} is {@code null}. 
         * @throws InvalidTypeException if {@code val} has not int type.
         */
        public void excludeIndexFromAccessCondition(Primitive val)
        throws InvalidOperandException, InvalidTypeException {
            if (val.getType() != Type.INT) {
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

        /**
         * Returns a {@link Primitive} denoting the fact that an index 
         * is in the {@link AccessOutcome}'s range.
         * 
         * @param accessIndex a {@link Primitive} denoting an integer value, 
         * the index by which the array is accessed.
         * @return a {@link Primitive} denoting the fact that 
         * {@code index} is in range (if its truth can be 
         * decided by normalization it is a {@link Simplex} 
         * denoting a boolean, otherwise it is an
         * {@link Expression}).
         * @throws InvalidOperandException if {@code accessIndex} is {@code null}.
         * @throws InvalidTypeException if {@code accessIndex} is not integer.
         */
        public Primitive inRange(Primitive accessIndex) 
        throws InvalidOperandException, InvalidTypeException { 
            return this.accessCondition.replace(INDEX, accessIndex); 
        }
    }

    /**
     * The outcome of an access by means of an index 
     * in the range 0..array.length.
     * 
     * @author Pietro Braione
     */
    public abstract class AccessOutcomeIn extends AccessOutcome implements Cloneable { 
        /**
         * Constructor (outcome returned by a concrete get).
         */
        private AccessOutcomeIn() {
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
        private AccessOutcomeIn(Expression accessCondition) { 
            super(accessCondition);  
        }

        protected AccessOutcomeIn clone() {
            try {
                return (AccessOutcomeIn) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new InternalError(e);
            }
        }
    }

    /**
     * The outcome of an access by means of an index 
     * in the range 0..array.length. Its result is 
     * obtained by accessing another (symbolic) array
     * that was initially in the heap, and that backs
     * this array.
     * 
     * @author Pietro Braione
     */
    public final class AccessOutcomeInInitialArray extends AccessOutcomeIn implements Cloneable {
        /**
         * A reference to the other (symbolic) {@link Array} that backs 
         * this array.
         */
        private Reference initialArray;

        /**
         * The offset through which {@code initialArray}
         * should be accessed.
         */
        private Primitive offset;

        private AccessOutcomeInInitialArray(Reference initialArray) 
        throws InvalidOperandException {
            this(initialArray, Array.this.calc.valInt(0));
        }

        private AccessOutcomeInInitialArray(Expression accessCondition, Reference initialArray) 
        throws InvalidOperandException {
            this(accessCondition, initialArray, Array.this.calc.valInt(0));
        }

        private AccessOutcomeInInitialArray(Reference initialArray, Primitive offset) 
        throws InvalidOperandException {
            super();
            if (initialArray == null || offset == null) {
                throw new InvalidOperandException("tried to create an AccessOutcomeInitialArray with null initial array origin or null offset");
            }
            this.initialArray = initialArray;
            this.offset = offset;
        }

        private AccessOutcomeInInitialArray(Expression accessCondition, Reference initialArray, Primitive offset) 
        throws InvalidOperandException {
            super(accessCondition);
            if (initialArray == null || offset == null) {
                throw new InvalidOperandException("tried to create an AccessOutcomeInitialArray with null initial array origin or null offset");
            }
            this.initialArray = initialArray;
            this.offset = offset;
        }

        public Reference getInitialArray() {
            return this.initialArray;
        }

        public Primitive getOffset() {
            return this.offset;
        }

        @Override
        protected AccessOutcomeInInitialArray clone() {
            return (AccessOutcomeInInitialArray) super.clone();
        }

        @Override
        public String toString() {
            return (this.accessCondition == null ? "true" : this.accessCondition.toString()) + 
            " -> " + (this.initialArray.toString() + "[_ + " + this.offset.toString() + "]");
        }
    }

    /**
     * The outcome of an access by means of an index 
     * in the range 0..array.length. Its result is 
     * a value stored in the array.
     * 
     * @author Pietro Braione
     */
    public final class AccessOutcomeInValue extends AccessOutcomeIn implements Cloneable {
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
        protected Value returnedValue;

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
        private AccessOutcomeInValue(Value returnedValue) {
            super();
            this.returnedValue = returnedValue;
        }

        /**
         * Constructor (outcome returned by a nonconcrete get or
         * stored in array entries).
         * 
         * @param accessCondition An {@link Expression} denoting a  
         *        condition over the array index. 
         * @param returnedValue A {@link Value} denoting the value returned  
         *        by an array access with index satisfying {@code exp}. It 
         *        can be a {@link Value} of the array member type, 
         *        or the special {@link ReferenceArrayImmaterial} value denoting 
         *        a reference to another array not yet available in the state's heap,
         *        or {@code null} if the value is unknown.
         */
        private AccessOutcomeInValue(Expression accessCondition, Value returnedValue) {
            super(accessCondition);
            this.returnedValue = returnedValue;
        }

        /**
         * Gets the value obtained by accessing the array.
         * 
         * @return a {@link Value} of the array member type (possibly a  
         *        {@link ReferenceArrayImmaterial} when the value is  
         *        a reference to another array not yet available in the state's heap),
         *        or {@code null} if the value is unknown.
         */
        public Value getValue() { return this.returnedValue; }

        @Override
        public AccessOutcomeInValue clone() {
            return (AccessOutcomeInValue) super.clone();
        }

        @Override
        public String toString() {
            return (this.accessCondition == null ? "true" : this.accessCondition.toString()) + 
            " -> " + (this.returnedValue == null ? "?" : this.returnedValue.toString());
        }
    }

    /**
     * The outcome of an access by means of an index 
     * not in the range 0..array.length. 
     * 
     * @author Pietro Braione
     */
    public final class AccessOutcomeOut extends AccessOutcome { 
        /**
         * Constructor (outcome returned by a concrete get).
         */
        private AccessOutcomeOut() {
            super();
        }

        /**
         * Constructor (outcome returned by a nonconcrete get).
         * 
         * @param accessCondition An {@link Expression} denoting a  
         *        condition over the array index. 
         *        When {@code null} denotes {@code true}. 
         */
        private AccessOutcomeOut(Expression accessCondition) { 
            super(accessCondition); 
        }

        @Override
        public String toString() {
            return (this.accessCondition == null ? "true" : this.accessCondition.toString()) + 
            " -> OUT_OF_RANGE";
        }
    }

    /**
     * Converts the primitive type encoding for arrays into that of {@link Type}.
     * 
     * @param type a {@code char}, the element type of an array.
     * @return the corresponding primitive type, or {@link Type#ERROR ERROR} 
     *         in case {@code type} does not correspond to a valid primitive type.
     */
    public static char arrayPrimitiveType(int type) {
        char retVal;

        switch (type) {
        case T_BOOLEAN:
            retVal = Type.BOOLEAN;
            break;
        case T_CHAR:
            retVal = Type.CHAR;
            break;
        case T_FLOAT:
            retVal = Type.FLOAT;
            break;
        case T_DOUBLE:
            retVal = Type.DOUBLE;
            break;
        case T_BYTE:
            retVal = Type.BYTE;
            break;
        case T_SHORT:
            retVal = Type.SHORT;
            break;
        case T_INT:
            retVal = Type.INT;
            break;
        case T_LONG:
            retVal = Type.LONG;
            break;
        default:
            retVal = Type.ERROR;
        }
        return retVal;
    }

    /**
     * Constructor.
     * 
     * @param calc a {@code Calculator}.  
     * @param initSymbolic {@code true} iff the array must be initialized 
     *        with symbolic values.
     * @param initValue a {@link Value} for initializing the array (ignored
     *        whenever {@code initSymbolic == true}); if {@code initValue == null}
     *        the default value for the array member type is used for initialization.
     * @param length a {@link Primitive}, the number of elements in the array.
     * @param type a {@link String}, the type of the array.
     * @param origin a {@link MemoryPath}, the
     *        chain of memory accesses which allowed to discover
     *        the {@link Array} for the first time. It can be null when
     *        {@code epoch == }{@link Epoch#EPOCH_AFTER_START}.
     * @param epoch the creation {@link Epoch} of the {@link Array}.
     * @param isInitial {@code true} iff this array is not an array of the 
     *        current state, but a copy of an (immutable) symbolic array in
     *        the initial state. Used only if {@code epoch == }{@link Epoch#EPOCH_AFTER_START}.
     * @param maxSimpleArrayLength an {@code int}, the maximum length an array may have
     *        to be granted simple representation.
     * @throws InvalidTypeException iff {@code type} is invalid. 
     */
    public Array(Calculator calc, boolean initSymbolic, Value initValue, Primitive length, String type, MemoryPath origin, Epoch epoch, boolean isInitial, int maxSimpleArrayLength) 
    throws InvalidTypeException {
        super(calc, type, origin, epoch, false, 0, new Signature(type, "" + Type.INT, "length"));
        if (isIllFormed(type)) {
            throw new InvalidTypeException("attempted creation of an array with type " + type);
        }
        this.isInitial = isInitial;
        this.lengthSignature = new Signature(type, "" + Type.INT, "length");
        this.calc = calc;
        try {
            this.INDEX = this.calc.valTerm(Type.INT, INDEX_ID);
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
     * @param referenceToOtherArray a {@link Reference} to an {@link Array} that backs this array.
     * @param otherArray the {@link Array} that backs this array.
     * @throws InvalidOperandException if {@code referenceToOtherArray == null}.
     * @throws NullPointerException if {@code otherArray == null}.
     */
    public Array(Reference referenceToOtherArray, Array otherArray) throws InvalidOperandException {
        super(otherArray.calc, otherArray.type, otherArray.getOrigin(), Epoch.EPOCH_BEFORE_START, false, 0, new Signature(otherArray.type, "" + Type.INT, "length"));
        //TODO assert other is an initial symbolic array
        this.isInitial = false;
        this.lengthSignature = new Signature(this.type, "" + Type.INT, "length");
        this.calc = otherArray.calc;
        try {
            this.INDEX = this.calc.valTerm(Type.INT, INDEX_ID);
        } catch (InvalidTypeException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
        this.fields.get(this.lengthSignature.toString()).setValue(otherArray.getLength());  //toString() is necessary, type erasure doesn't play well
        try {
            final Expression indexGreaterThanZero = (Expression) INDEX.ge(this.calc.valInt(0));
            final Expression indexLessThanLength = (Expression) INDEX.lt(getLength());
            this.indexInRange  = (Expression) indexGreaterThanZero.and(indexLessThanLength);		
        } catch (InvalidOperandException | InvalidTypeException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
        this.entries = new LinkedList<AccessOutcomeIn>();
        this.entries.add(new AccessOutcomeInInitialArray(this.indexInRange, referenceToOtherArray));
    }

    private static boolean isIllFormed(String type) {
        boolean illFormed = false;
        if (type == null || type.charAt(0) != Type.ARRAYOF || type.length() < 2) {
            illFormed = true;
        } else {
            switch (type.charAt(1)) {
            case Type.BOOLEAN:
            case Type.CHAR:
            case Type.FLOAT:
            case Type.DOUBLE:
            case Type.BYTE:
            case Type.SHORT:
            case Type.INT:
            case Type.LONG:
            case Type.REFERENCE:
            case Type.ARRAYOF:
                break;
            default:
                illFormed = true;
            }
        }
        return illFormed;
    }

    private void setEntriesInit(boolean initSymbolic, Value initValue, int maxSimpleArrayLength) {
        final Value entryValue;
        if (initSymbolic) {
            entryValue = null;
        } else if (initValue == null) {
            entryValue = this.calc.createDefault(Type.getArrayMemberType(this.type)); 
        } else {
            entryValue = initValue;
        }

        //in the case length is concrete and not too high, creates an entry for each 
        //possible value in the range (simple representation); the rationale is, it 
        //is better having more, restrictive entries than less, liberal entries, since 
        //most workload is on the theorem prover side, and with restrictive entries 
        //we may hope that normalization will succeed upon array access, thus reducing 
        //the calls to the prover.
        this.entries = new LinkedList<AccessOutcomeIn>();
        if (getLength() instanceof Simplex) {
            final int ln = ((Integer) ((Simplex) getLength()).getActualValue()).intValue();
            if (ln <= maxSimpleArrayLength) {
                this.simpleRep = true;
                for (int i = 0; i < ln; i++) {
                    try {
                        this.entries.add(new AccessOutcomeInValue((Expression) INDEX.eq(this.calc.valInt(i)),
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
        this.entries.add(new AccessOutcomeInValue(this.indexInRange, entryValue));
    }

    /**
     * Clones the entries of another array into this array,
     * completely replacing the current entries. This method
     * is used to implement {@link java.lang.Object#clone()}.
     * 
     * @param other the other array.
     * @throws InvalidTypeException if {@code other} has different type from {@code this}.
     */
    public void cloneEntries(Array other) throws InvalidTypeException {
        if (!this.type.equals(other.type)) {
            throw new InvalidTypeException("tried to clone entries of a " + other.type + " array into a " + this.type + " array");
        }
        this.entries.clear();
        for (AccessOutcomeIn entry : other.entries) {
            final AccessOutcomeIn entryClone = entry.clone();
            try {
                entryClone.accessCondition = (Expression) entryClone.accessCondition.replace(this.INDEX, other.INDEX);
            } catch (InvalidTypeException | InvalidOperandException e) {
                //this should never happen
                throw new UnexpectedInternalException(e);
            }
            this.entries.add(entry.clone());
        }
    }

    /**
     * Returns the length of the array.
     * 
     * @return a {@link Primitive}.
     */
    public Primitive getLength() {
        return (Primitive) getFieldValue(this.lengthSignature);
    }

    /**
     * Checks whether the array has a simple representation, allowing
     * fast array access.
     * 
     * @return {@code true} iff the array has a simple representation
     *         (this happens only when its length is a {@link Simplex}).
     */
    public boolean hasSimpleRep() {
        return this.simpleRep;
    }

    /**
     * Checks whether the array is simple.
     * 
     * @return {@code true} iff the array is simple, i.e. {@link #hasSimpleRep()}
     *         and all its elements are concrete values.
     */
    public boolean isSimple() {
        if (hasSimpleRep()) {
            for (AccessOutcomeIn e : this.entries) {
                if (!(e instanceof AccessOutcomeInValue) || ((AccessOutcomeInValue) e).returnedValue.isSymbolic()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Checks whether the array is initial.
     * 
     * @return {@code true} iff the array is initial, as set via the constructor.
     */
    public boolean isInitial() {
        return this.isInitial;
    }

    /**
     * Returns the outcomes of an access to the array.
     * 
     * @param index the index of the element in the array, a {@code Primitive}
     *        with type int.
     * @return a {@link Collection}{@code <}{@link AccessOutcome}{@code >}, 
     *         whose {@link AccessOutcome#getExpression}s are specialized on 
     *         {@code index} but are possibly not satisfiable.
     * @throws InvalidOperandException if {@code index} is {@code null}.
     * @throws InvalidTypeException if {@code index} is not an int.
     */
    public Collection<AccessOutcome> get(Primitive index) 
    throws InvalidOperandException, InvalidTypeException {
        final LinkedList<AccessOutcome> retVal = new LinkedList<AccessOutcome>();
        final Primitive inRange = inRange(index);

        //builds the answer
        if (hasSimpleRep() && index instanceof Simplex) { 
            //the fast case, access this.values directly by index			
            if (inRange.surelyTrue()) {
                final int indexInt = (Integer) ((Simplex) index).getActualValue();
                final AccessOutcomeIn e = this.entries.get(indexInt);
                if (e instanceof AccessOutcomeInValue) {
                    retVal.add(new AccessOutcomeInValue(((AccessOutcomeInValue) e).returnedValue));
                } else { //e instanceof AccessOutcomeInInitialArray
                    final AccessOutcomeInInitialArray eCast = (AccessOutcomeInInitialArray) e;
                    retVal.add(new AccessOutcomeInInitialArray(eCast.initialArray, eCast.offset));
                }
            } else {
                retVal.add(new AccessOutcomeOut()); 
            }
        } else {
            //scans the entries and adds all the (possibly) satisfiable 
            //inbound cases
            for (AccessOutcomeIn e : this.entries) {
                final Primitive inRangeEntry = e.inRange(index);
                if (inRangeEntry.surelyTrue()) { //this may only happen when index is Simplex
                    if (e instanceof AccessOutcomeInValue) {
                        retVal.add(new AccessOutcomeInValue(((AccessOutcomeInValue) e).returnedValue));
                    } else { //e instanceof AccessOutcomeInInitialArray
                        final AccessOutcomeInInitialArray eCast = (AccessOutcomeInInitialArray) e;
                        retVal.add(new AccessOutcomeInInitialArray(eCast.initialArray, eCast.offset));						
                    }
                } else if (inRangeEntry.surelyFalse()) {
                    //do nothing
                } else { //inRangeEntry is possibly satisfiable
                    if (e instanceof AccessOutcomeInValue) {
                        retVal.add(new AccessOutcomeInValue((Expression) inRangeEntry, ((AccessOutcomeInValue) e).returnedValue));
                    } else { //e instanceof AccessOutcomeInInitialArray
                        final AccessOutcomeInInitialArray eCast = (AccessOutcomeInInitialArray) e;
                        retVal.add(new AccessOutcomeInInitialArray((Expression) inRangeEntry, eCast.initialArray, eCast.offset));						
                    }
                }
            }

            //manages the out-of-bounds case
            final Primitive outOfRange = inRange.not();
            if (outOfRange.surelyTrue()) {
                retVal.add(new AccessOutcomeOut());
            } else if (outOfRange.surelyFalse()) {
                //do nothing
            } else { //outOfRange is possibly satisfiable
                retVal.add(new AccessOutcomeOut((Expression) outOfRange));
            }
        }

        return retVal;
    }

    /**
     * Sets an element of the array when the array has a simple 
     * representation and the index is a {@link Simplex}. 
     * The array is left unchanged iff index is not in its range.  
     * 
     * @param index the position of the array element to set.  
     * @param item the new {@link Value} to be set at {@code index}.
     * @throws InvalidOperandException if {@code index} is {@code null}.
     * @throws InvalidTypeException if {@code index} is not an int.
     * @throws FastArrayAccessNotAllowedException if the array has not
     * a simple representation.
     */
    public void setFast(Simplex index, Value item) 
    throws InvalidOperandException, InvalidTypeException, FastArrayAccessNotAllowedException {
        if (index == null) {
            throw new InvalidOperandException("attempted array access with null index");
        }
        if (index.getType() != Type.INT) {
            throw new InvalidTypeException("attempted array access with an index with type " + index.getType());
        }
        if (!this.simpleRep) {
            throw new FastArrayAccessNotAllowedException();
        }
        final int actualIndex = (Integer) index.getActualValue();
        final int actualLength = (Integer) ((Simplex) this.getLength()).getActualValue();
        if (actualIndex >= 0 && actualIndex < actualLength) {
            final AccessOutcomeIn e = this.entries.get(actualIndex);
            if (e instanceof AccessOutcomeInValue) {
                ((AccessOutcomeInValue) e).returnedValue = item;
            } else {
                final AccessOutcomeInValue eNew = new AccessOutcomeInValue(e.accessCondition, item);
                this.entries.set(actualIndex, eNew);
            }
        } 	//TODO else throw an exception???
    }

    /** An iterator that terminates instantaneously. */
    private static final Iterator<Array.AccessOutcomeIn> EMPTY_ITERATOR = 
    new Iterator<Array.AccessOutcomeIn>() {
        @Override public boolean hasNext() { return false; }
        @Override public AccessOutcomeIn next() { throw new NoSuchElementException(); }
        @Override public void remove() { throw new UnsupportedOperationException(); }
    };

    /**
     * Sets an element of the array. It <em>assumes</em> that the index 
     * by which the array is accessed may be in range (i.e., that 
     * {@code this.}{@link #inRange(Primitive) inRange(index)} is 
     * satisfiable) and updates the theory accordingly by adding a 
     * new entry. All the entries already present are unaffected.
     * 
     * @param index the position of the array element to set, a {@code Primitive}
     *        denoting an int.  
     * @param valToSet the {@link Value} to be set at {@code index}.
     * @throws InvalidOperandException if {@code index} is {@code null}.
     * @throws InvalidTypeException if {@code index} is not an int. 
     */
    public void set(final Primitive index, final Value valToSet)
    throws InvalidOperandException, InvalidTypeException {
        if (index == null) {
            throw new InvalidOperandException("attempted array access with null index");
        }
        if (index.getType() != Type.INT) {
            throw new InvalidTypeException("attempted array access with an index with type " + index.getType());
        }
        this.simpleRep = false;
        final Expression formalIndexIsSetIndex = (Expression) INDEX.eq(index);
        final Expression accessExpression = (Expression) this.indexInRange.and(formalIndexIsSetIndex); //if we assume that index may be in range, this is an Expression
        this.entries.add(new AccessOutcomeInValue(accessExpression, valToSet));
    }

    /**
     * Returns an iterator to the entries that are possibly affected by 
     * a set operation on this array.
     * 
     * @param index the position of the array element to set, a {@code Primitive}
     *        denoting an int.  
     * @param valToSet the {@link Value} to be set at {@code index}.
     * @return an {@link Iterator}{@code <}{@link AccessOutcomeIn}{@code >}
     *         to the entries of this {@link Array} that are possibly 
     *         modified by the update; the caller must decide whether 
     *         constrain and possibly delete them. The returned entries 
     *         are those for which it is not a contradiction that {@code index}
     *         falls in their range, and either are {@link AccessOutcomeInInitialArray}s, 
     *         or are {@link AccessOutcomeInValue} with unknown value, or 
     *         are {@link AccessOutcomeInValue} with value different from {@code valToSet}.
     */
    public Iterator<Array.AccessOutcomeIn> entriesPossiblyAffectedByAccess(final Primitive index, final Value valToSet) {
        return new Iterator<Array.AccessOutcomeIn>() {
            //this iterator filters the relevant members in Array.this.values
            //by wrapping the default iterator to it
            private final Iterator<Array.AccessOutcomeIn> it = Array.this.entries.iterator();
            private Array.AccessOutcomeIn next = null;
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
                        (e instanceof AccessOutcomeInInitialArray || ((AccessOutcomeInValue) e).returnedValue == null || !((AccessOutcomeInValue) e).returnedValue.equals(valToSet));
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

    /**
     * Implements {@code java.lang.System.arraycopy}, where this array is
     * the destination. It <em>assumes</em> that the preconditions of
     * a successful copy (source and destination copy ranges are 
     * within the bounds of the respective arrays, the arrays are 
     * type-compatible for assignment) and updates the theory accordingly. 
     * 
     * @param src The source {@link Array}.
     * @param srcPos The source initial position.
     * @param destPos The destination initial position.
     * @param length How many elements should be copied.
     * @return an {@link Iterator}{@code <}{@link AccessOutcomeIn}{@code >}
     *         to the entries of this {@link Array} that are possibly 
     *         modified by the update; the caller must decide whether 
     *         constrain and possibly delete them.
     * @throws InvalidOperandException if {@code srcPos} or {@code destPos} 
     *         or {@code length} is {@code null}.
     * @throws InvalidTypeException if {@code srcPos} or {@code destPos} 
     *         or {@code length} is not an int. 
     */
    public Iterator<AccessOutcomeIn> arraycopy(Array src, Primitive srcPos, Primitive destPos, Primitive length, Consumer<Reference> checkOk) 
    throws InvalidOperandException, InvalidTypeException {
        final String srcTypeComponent = getArrayMemberType(src.getType());
        final String destTypeComponent = getArrayMemberType(getType());
        if (this.simpleRep && src.simpleRep && 
        srcPos instanceof Simplex && destPos instanceof Simplex && 
        length instanceof Simplex) {
            //fast operation
            int srcPosInt = ((Integer) ((Simplex) srcPos).getActualValue()).intValue();
            int destPosInt = ((Integer) ((Simplex) destPos).getActualValue()).intValue();
            int lengthInt = ((Integer) ((Simplex) length).getActualValue()).intValue();
            for (int ofst = 0; ofst < lengthInt; ++ofst) {
                final AccessOutcomeIn srcEntry = src.entries.get(srcPosInt + ofst);
                final AccessOutcomeIn destEntry;
                if (srcEntry instanceof AccessOutcomeInValue) {
                    final Value srcValue = ((AccessOutcomeInValue) srcEntry).returnedValue;
                    if (!isPrimitive(srcTypeComponent) && !isPrimitive(destTypeComponent)) { 
                        checkOk.accept((Reference) srcValue);
                    }
                    destEntry = new AccessOutcomeInValue(srcEntry.accessCondition, srcValue);
                } else { //srcEntry instanceof AccessOutcomeInInitialArray
                    final Reference initialArray = ((AccessOutcomeInInitialArray) srcEntry).initialArray;
                    final Primitive offset = ((AccessOutcomeInInitialArray) srcEntry).offset;
                    //TODO find a way to perform assignment compatibility check
                    destEntry = new AccessOutcomeInInitialArray(srcEntry.accessCondition, initialArray, offset.sub(destPos).add(srcPos));
                }
                this.entries.set(destPosInt + ofst, destEntry);
            }
            return EMPTY_ITERATOR;
        } else {
            this.simpleRep = false;
            final Expression indexInDestRange = (Expression) INDEX.ge(destPos).and(INDEX.lt(destPos.add(length)));
            final Expression indexNotInDestRange = (Expression) indexInDestRange.not();

            //constrains the entries of the destination array
            for (AccessOutcomeIn destEntry : this.entries) {
                destEntry.strengthenAccessCondition(indexNotInDestRange);
            }

            //adds new entries corresponding to the source array entries
            final Primitive srcIndex = INDEX.sub(destPos).add(srcPos);
            for (AccessOutcomeIn srcEntry : src.entries) {
                final Expression accessCondition = (Expression) this.indexInRange.and(srcEntry.inRange(srcIndex)).and(indexInDestRange);
                final AccessOutcomeIn destEntry;
                if (srcEntry instanceof AccessOutcomeInValue) {
                    final Value srcValue = ((AccessOutcomeInValue) srcEntry).returnedValue;
                    if (!isPrimitive(srcTypeComponent) && !isPrimitive(destTypeComponent)) { 
                        checkOk.accept((Reference) srcValue);
                    }
                    destEntry = new AccessOutcomeInValue(accessCondition, srcValue);
                } else { //srcEntry instanceof AccessOutcomeInInitialArray
                    final Reference initialArray = ((AccessOutcomeInInitialArray) srcEntry).initialArray;
                    final Primitive offset = ((AccessOutcomeInInitialArray) srcEntry).offset;
                    //TODO find a way to perform assignment compatibility check
                    destEntry = new AccessOutcomeInInitialArray(accessCondition, initialArray, offset.sub(destPos).add(srcPos));
                }
                this.entries.add(destEntry);
            }

            //returns the iterator
            return Array.this.entries.iterator(); //for sake of simplicity all the entries are considered potentially affected
        }
    }

    /**
     * Returns a {@link Primitive} denoting the fact that an index 
     * is in the {@link Array}'s definition range.
     * 
     * @param index a {@link Primitive} denoting an int value.
     * @return a {@link Value} denoting the fact that 
     * {@code index} is in range. If the fact can be 
     * proved or disproved by normalization, a {@link Simplex} 
     * denoting a boolean value is returned, otherwise an
     * {@link Expression} is returned. 
     * @throws InvalidOperandException if {@code index} is {@code null}. 
     * @throws InvalidTypeException if {@code index} is not an int. 
     */
    public Primitive inRange(Primitive index) 
    throws InvalidOperandException, InvalidTypeException {
        return this.indexInRange.replace(INDEX, index);
    }

    /**
     * Returns a {@link Primitive} denoting the fact that 
     * an index is out of range.
     * 
     * @param index a {@link Primitive} denoting an int value.
     * @return an {@link Expression} denoting the fact that 
     * {@code index} is out of range. If the fact can be 
     * proved or disproved by normalization, a {@link Simplex} 
     * denoting {@code true} or {@code false} respectively 
     * is returned. 
     * @throws InvalidOperandException if {@code index} is {@code null}. 
     * @throws InvalidTypeException if {@code index} is not an int. 
     */
    public Primitive outOfRange(Primitive index) 
    throws InvalidOperandException, InvalidTypeException {
        final Primitive retVal = this.inRange(index).not();
        return retVal;
    }

    /**
     * Returns a list of {@link AccessOutcomeIn} representing the 
     * array's theory.
     * 
     * @return a {@link List}{@code <}{@link AccessOutcomeIn}{@code >}.
     */
    public List<AccessOutcomeIn> values() {
        return Collections.unmodifiableList(this.entries);
    }

    /**
     * Returns a string version of an array of chars.
     * 
     * @return {@code null} iff this array is not a concrete
     * array of {@code char}s; otherwise, a {@code String} whose value
     * is the content of this array.
     */
    public String valueString() {
        if (this.type.equals("" + Type.ARRAYOF + Type.CHAR) && isSimple()) {
            final StringBuilder buf = new StringBuilder();
            for (AccessOutcomeIn e : this.entries) {
                buf.append(((AccessOutcomeInValue) e).returnedValue.toString());
            }
            return buf.toString();
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        String str = "[Type:" + this.type + ", Length:" + this.getLength().toString() + ", Elements: {";
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
    public Array clone() {
        final Array o = (Array) super.clone();

        o.entries = new LinkedList<AccessOutcomeIn>();
        for (AccessOutcomeIn e : this.entries) {
            o.entries.add(e.clone());
        }

        return o;
    }
}

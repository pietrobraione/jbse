package jbse.mem;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import jbse.common.Type;
import jbse.common.exc.InvalidInputException;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.val.Calculator;
import jbse.val.Expression;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.ReferenceArrayImmaterial;
import jbse.val.Simplex;
import jbse.val.Term;
import jbse.val.Value;
import jbse.val.exc.InvalidTypeException;

/**
 * Interface for arrays. Upon access it returns a 
 * collection of {@link AccessOutcome}s, 
 * associating {@link Expression}s on the array index to 
 * the outcome of an array access when the access index satisfies it.
 *  
 * @author Pietro Braione
 */
public interface Array extends HeapObjekt {
    /** 
     * The {@link String} identifier of the {@link Term} used to
     * represent an {@link Array}'s index.
     */
    public static final String INDEX_ID = "{INDEX-";

    /**
     * The outcome of an array access. An 
     * {@link AccessOutcome} is a pair (condition, result), 
     * where the condition is a predicate over the array 
     * index, and result expresses what happens when the array is   
     * accessed with an index satisfying the condition.
     * 
     * @author Pietro Braione
     */
    public interface AccessOutcome {
        /**
         * Gets the constraint over the symbolic values of the execution 
         * under which the array access yields this outcome.
         * 
         * @return an {@link Expression}, or {@code null} for denoting
         *         {@code true}.
         */
        Expression getAccessCondition();

        /**
         * Strengthens the access condition of this {@link AccessOutcome}
         * by imposing that the index is different from a value. 
         * 
         * @param calc a Calculator. It must not be {@code null}.
         * @param val a {@link Primitive}. This {@link AccessOutcome}'s 
         *        access condition will be strengthened by conjoining it
         *        with an expression stating that the index is different
         *        from {@code val}.
         * @throws InvalidInputException if {@code calc == null || val == null}. 
         * @throws InvalidTypeException if {@code val} has not {@code int} type.
         */
        void excludeIndexFromAccessCondition(Calculator calc, Primitive val)
        throws InvalidInputException, InvalidTypeException;

        /**
         * Returns a {@link Primitive} denoting the fact that an index 
         * is in the {@link AccessOutcome}'s range.
         * 
         * @param calc a Calculator. It must not be {@code null}.
         * @param accessIndex a {@link Primitive} denoting an integer value, 
         *        the index by which the array is accessed.
         * @return a {@link Primitive} denoting the fact that 
         *         {@code index} is in range (if its truth can be 
         *         decided by normalization it is a {@link Simplex} 
         *         denoting a boolean, otherwise it is an
         *         {@link Expression}).
         * @throws InvalidInputException if {@code calc == null || accessIndex == null}.
         * @throws InvalidTypeException if {@code accessIndex} has not {@code int} type.
         */
        Primitive inRange(Calculator calc, Primitive accessIndex) 
        throws InvalidInputException, InvalidTypeException;
    }

    /**
     * The outcome of an access by means of an index 
     * in the range 0..array.length.
     * 
     * @author Pietro Braione
     */
    public interface AccessOutcomeIn extends AccessOutcome, Cloneable { 
    	AccessOutcomeIn clone();
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
    public interface AccessOutcomeInInitialArray extends AccessOutcomeIn {
    	/**
    	 * Gets a reference to the array in the initial 
    	 * heap that backs this array.
    	 * 
    	 * @return a {@link Reference}.
    	 */
        Reference getInitialArray();
        
        /**
         * Gets the offset that must be added to the access index to 
         * obtain the index in the initial array where the accessed
         * element is.
         * 
         * @return a {@link Primitive}.
         */
        Primitive getOffset();
    }

    /**
     * The outcome of an access by means of an index 
     * in the range 0..array.length. Its result is 
     * a value stored in the array.
     * 
     * @author Pietro Braione
     */
    public interface AccessOutcomeInValue extends AccessOutcomeIn {
        /**
         * Gets the value obtained by accessing the array.
         * 
         * @return a {@link Value} of the array member type (possibly a  
         *        {@link ReferenceArrayImmaterial} when the value is  
         *        a reference to another array not yet available in the state's heap),
         *        or {@code null} if the value is unknown.
         */
        Value getValue();
        
        /**
         * Sets the value obtained by accessing the array.
         * 
         * @param newValue a {@link Value} of the array member type,
         *        or {@code null} if the value is unknown.
         * @throws InvalidTypeException if {@code newValue} does not
         *         agree with the array's type.
         */
        void setValue(Value newValue) throws InvalidTypeException;
    }

    /**
     * The outcome of an access by means of an index 
     * not in the range 0..array.length. 
     * 
     * @author Pietro Braione
     */
    public interface AccessOutcomeOut extends AccessOutcome { }

    /**
     * Converts the primitive type encoding for arrays into that of {@link Type}.
     * 
     * @param type a {@code char}, the element type of an array.
     * @return the corresponding primitive type, or {@link Type#ERROR ERROR} 
     *         in case {@code type} does not correspond to a valid primitive type.
     */
    static char arrayPrimitiveType(int type) {
        final int T_BOOLEAN = 4;
        final int T_CHAR    = 5;
        final int T_FLOAT   = 6;
        final int T_DOUBLE  = 7;
        final int T_BYTE    = 8;
        final int T_SHORT   = 9;
        final int T_INT     = 10;
        final int T_LONG    = 11;

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
     * Returns the length of the array.
     * 
     * @return a {@link Primitive} with {@code int} type, the length
     *         of the array.
     */
    Primitive getLength();
    
    /**
     * Returns the {@link Term} used to indicate the 
     * index in the entries.
     * 
     * @return a {@link Term}.
     */
    Term getIndex();

    /**
     * Checks whether the array has a simple representation, allowing
     * fast array access.
     * 
     * @return {@code true} iff the array has a simple representation.
     *         This happens only if (but not necessarily if) its length 
     *         is a {@link Simplex}.
     */
    boolean hasSimpleRep();

    /**
     * Checks whether the array is simple.
     * 
     * @return {@code true} iff the array is simple, i.e. 
     *         {@link #hasSimpleRep()} and all its elements are 
     *         concrete values.
     */
    boolean isSimple();

    /**
     * Checks whether the array is initial.
     * 
     * @return {@code true} iff the array is initial, i.e., iff this array is 
     *        not an array of the current state, but an (immutable) copy of a 
     *        symbolic array in the initial state. 
     */
    boolean isInitial();
    
    /**
     * Returns the outcome of an access to the array when the
     * index is a {@link Simplex} and the array has simple representation.
     * 
     * @param calc a {@link Calculator}. It must not be {@code null}.
     * @param index the index of the element in the array, a {@link Simplex}
     *        with type {@code int}.
     * @return an {@link AccessOutcome} whose {@link AccessOutcome#getExpression}s 
     *         is specialized on {@code index}.
     * @throws InvalidInputException if {@code calc == null || index == null}.
     * @throws InvalidTypeException if {@code index} has not {@code int} type.
     * @throws FastArrayAccessNotAllowedException if the array has not
     *         a simple representation.
     */
    AccessOutcome getFast(Calculator calc, Simplex index)
    throws InvalidInputException, InvalidTypeException, FastArrayAccessNotAllowedException;

    /**
     * Returns the outcomes of an access to the array.
     * 
     * @param calc a {@link Calculator}. It must not be {@code null}.
     * @param index the index of the element in the array, a {@code Primitive}
     *        with type {@code int}.
     * @return a {@link Collection}{@code <}{@link AccessOutcome}{@code >}, 
     *         whose {@link AccessOutcome#getAccessCondition}s might be
     *         set to {@code null} if they can be trivially decided to be
     *         satified.
     * @throws InvalidInputException if {@code calc == null || index == null}.
     * @throws InvalidTypeException if {@code index} has not {@code int} type.
     */
    Collection<AccessOutcome> get(Calculator calc, Primitive index) 
    throws InvalidInputException, InvalidTypeException;
    
    /**
     * Sets an element of the array when the array has a simple 
     * representation and the index is a {@link Simplex}. 
     * The array is left unchanged iff index is not in its range.  
     * 
     * @param index the position of the array element to set. It must
     *        denote an {@code int}.  
     * @param newValue the new {@link Value} to be set at {@code index}.
     * @throws InvalidInputException if {@code index == null}.
     * @throws InvalidTypeException if {@code index} is not an {@code int}
     *         or {@code newValue} is incompatible with the array member type.
     * @throws FastArrayAccessNotAllowedException if the array has not
     * a simple representation.
     */
    void setFast(Simplex index, Value newValue) 
    throws InvalidInputException, InvalidTypeException, FastArrayAccessNotAllowedException;

    /**
     * Sets an element of the array. It <em>assumes</em> that the index 
     * by which the array is accessed may be in range (i.e., that 
     * {@code this.}{@link #inRange(Calculator, Primitive) inRange(calc, index)} is 
     * satisfiable) and updates the theory accordingly by adding a 
     * new entry. All the entries already present are unaffected.
     * 
     * @param calc a {@link Calculator}. It must not be {@code null}.
     * @param index the position of the array element to set, a {@code Primitive}
     *        denoting an {@code int}.  
     * @param newValue the new {@link Value} to be set at {@code index}.
     * @throws InvalidInputException if {@code calc == null || index == null}
     *         ({@code newValue == null} in some cases means unknown value, 
     *         so we accept it).
     * @throws InvalidTypeException if {@code index} is not an {@code int} 
     *         or {@code newValue} is incompatible with the array member type.
     */
    void set(Calculator calc, Primitive index, Value newValue)
    throws InvalidInputException, InvalidTypeException;

    /**
     * Returns an iterator to the entries of this array.
     * 
     * @return an {@link Iterator}{@code <? extends }{@link AccessOutcomeIn}{@code >}
     *         to the entries of this {@link Array}, allowing their direct modification.
     */
    Iterator<? extends AccessOutcomeIn> entries();

    /**
     * Returns an iterator to the entries that are possibly affected by 
     * a set operation on this array.
     * 
     * @param calc a {@link Calculator}. It must not be {@code null}.
     * @param index the position of the array element to set, a {@code Primitive}
     *        denoting an {@code int}.  
     * @param newValue the {@link Value} to be set at {@code index}.
     * @return an {@link Iterator}{@code <? extends }{@link AccessOutcomeIn}{@code >}
     *         to the entries of this {@link Array} that are possibly 
     *         modified by the update; the caller must decide whether 
     *         constrain and possibly delete them. The returned entries 
     *         are those for which it is not a contradiction that {@code index}
     *         falls in their range, and either are {@link AccessOutcomeInInitialArray}s, 
     *         or are {@link AccessOutcomeInValue} with unknown value, or 
     *         are {@link AccessOutcomeInValue} with value different from {@code newValue}.
     * @throws InvalidInputException if {@code calc == null || index == null}
     *         ({@code newValue == null} in some cases means unknown value, 
     *         so we accept it). 
     */
    Iterator<? extends AccessOutcomeIn> entriesPossiblyAffectedByAccess(Calculator calc, Primitive index, Value newValue) 
    throws InvalidInputException;

    /**
     * Clones the entries of another array into this array,
     * completely replacing the current entries. This method
     * is used to implement {@link java.lang.Object#clone()}.
     * 
     * @param src the source {@link Array}.
     * @param calc a {@link Calculator}.
     * @throws InvalidInputException if {@code src == null || calc == null}.
     * @throws InvalidTypeException if {@code other} has different type from {@code this}.
     */
    void cloneEntries(Array src, Calculator calc) throws InvalidInputException, InvalidTypeException;

    /**
     * Implements {@code java.lang.System.arraycopy}, where this array is
     * the destination. It <em>assumes</em> that the preconditions of
     * a successful copy (source and destination copy ranges are 
     * within the bounds of the respective arrays, the arrays are 
     * type-compatible for assignment) and updates the theory accordingly. 
     * 
     * @param calc a {@link Calculator}. It must not be {@code null}.
     * @param src the source {@link Array}.
     * @param srcPos a {@link Primitive}, the initial position in {@code src}
     *        from where start to copy.
     * @param destPos a {@link Primitive}, the initial position in this {@link Array}
     *        from where start to copy.
     * @param length how many elements should be copied.
     * @param checkOk a {@link Consumer}{@code <}{@link Reference}{@code >} that
     *        checks whether a source array element (in case it is a {@link Reference})
     *        can be written in the destination array. It can be {@code null} and
     *        in such case no check is applied.
     * @return an {@link Iterator}{@code <? extends }{@link AccessOutcomeIn}{@code >}
     *         to the entries of this {@link Array} that are possibly 
     *         modified by the update; the caller must decide whether 
     *         constrain and possibly delete them.
     * @throws InvalidInputException if {@code calc} or {@code src} or {@code srcPos} 
     *         or {@code destPos} or {@code length} is {@code null}.
     * @throws InvalidTypeException if {@code srcPos} or {@code destPos} 
     *         or {@code length} is not an {@code int}. 
     */
    Iterator<? extends AccessOutcomeIn> arraycopy(Calculator calc, Array src, Primitive srcPos, Primitive destPos, Primitive length, Consumer<Reference> checkOk) 
    throws InvalidInputException, InvalidTypeException;

    /**
     * Returns a {@link Primitive} denoting the fact that an index 
     * is in the {@link Array}'s definition range.
     * 
     * @param calc a {@link Calculator}. It must not be {@code null}.
     * @param index a {@link Primitive} denoting an {@code int} value.
     * @return a {@link Primitive} denoting the fact that 
     *         {@code index} is in range. If the fact can be 
     *         proved or disproved by normalization, a {@link Simplex} 
     *         denoting a boolean value is returned, otherwise an
     *         {@link Expression} is returned. 
     * @throws InvalidInputException if {@code calc == null || index == null}. 
     * @throws InvalidTypeException if {@code index} is not an int. 
     */
    Primitive inRange(Calculator calc, Primitive index) throws InvalidInputException, InvalidTypeException;

    /**
     * Returns a {@link Primitive} denoting the fact that 
     * an index is out of range.
     * 
     * @param calc a {@link Calculator}. It must not be {@code null}.
     * @param index a {@link Primitive} denoting an {@code int} value.
     * @return a {@link Primitive} denoting the fact that 
     *         {@code index} is out of range. If the fact can be 
     *         proved or disproved by normalization, a {@link Simplex} 
     *         denoting {@code true} or {@code false} respectively 
     *         is returned, otherwise an {@link Expression} is returned. 
     * @throws InvalidInputException if {@code calc == null || index == null}. 
     * @throws InvalidTypeException if {@code index} is not an int. 
     */
    Primitive outOfRange(Calculator calc, Primitive index) throws InvalidInputException, InvalidTypeException;

    /**
     * Returns a list of {@link AccessOutcomeIn} representing the 
     * array's theory.
     * 
     * @return a {@link List}{@code <}{@link AccessOutcomeIn}{@code >}.
     */
    List<AccessOutcomeIn> values();

    /**
     * Returns a string version of an array of chars.
     * 
     * @return {@code null} iff this array is not a simple (see {@link #isSimple()})
     *         array of {@code char}s; otherwise, a {@code String} whose value
     *         is the content of this array.
     */
    String valueString();
    
    Array clone();
}

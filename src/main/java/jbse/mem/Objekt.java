package jbse.mem;

import java.util.Collection;
import java.util.Map;

import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.common.exc.InvalidInputException;
import jbse.val.HistoryPoint;
import jbse.val.Primitive;
import jbse.val.ReferenceSymbolic;
import jbse.val.Value;

/**
 * A Java object which may reside in the heap or in the static store, 
 * i.e., either a class, or an instance of a class, or an array.
 */
public interface Objekt extends Cloneable {
    /**
     * Returns the class of this {@link Objekt}.
     * 
     * @return a {@link ClassFile} or {@code null}
     *         if this object has no class (i.e., it is
     *         in the static store or is a meta level box).
     */
    ClassFile getType();

    /**
     * Returns this {@link Objekt}'s origin.
     * 
     * @return a {@link ReferenceSymbolic} or {@code null}
     *         if {@code !}{@link #isSymbolic()}.
     */
    ReferenceSymbolic getOrigin();

    /**
     * Returns the creation epoch of this object
     * as a {@link HistoryPoint}.
     * 
     * @return a {@link HistoryPoint}.
     */
    HistoryPoint historyPoint();
    
    /**
     * Checks whether this {@link Objekt} is symbolic.
     *  
     * @return {@code true} iff the object is symbolic, i.e., 
     *         it was not explicitly created during symbolic execution 
     *         by a {@code new*} bytecode, but rather assumed.
     */
    boolean isSymbolic();
    
    /**
     * Turns a concrete object into a symbolic one.
     * 
     * @param origin a {@link ReferenceSymbolic}, the origin
     *        of this {@link Objekt}.
     * @throws InvalidInputException if {@code origin == null}
     *         or if this {@link Objekt} cannot be turned into
     *         a symbolic object (e.g., because it already is).
     */
    void makeSymbolic(ReferenceSymbolic origin) throws InvalidInputException;

    /**
     * Sets the identity hash code of this {@link Objekt}.
     * 
     * @param identityHashCode a {@link Primitive} with type {@code int} 
     *        for the identity hash code of this {@link Objekt}.
     */
    void setIdentityHashCode(Primitive identityHashCode);

    /**
     * Returns the identity hash code of this {@code Objekt}.
     * 
     * @return a {@code Primitive}.
     */
    Primitive getIdentityHashCode();

    /**
     * Returns the {@link Signature}s of all the fields
     * this {@link Objekt} stores.
     * 
     * @return an immutable 
     *         {@link Collection}{@code <}{@link Signature}{@code >}.
     */
    Collection<Signature> getStoredFieldSignatures();

    /**
     * Returns all the {@link Signature}s this {@link Objekt} stores.
     *
     * @return an immutable
     *         {@link Collection}{@code <}{@link Signature}{@code >}.
     */
    Collection<Signature> getAllStoredFieldSignatures();

    /**
     * Checks whether an object has an offset.
     * 
     * @param ofst an {@code int}.
     * @return {@code true} iff {@code ofst} is a valid offset
     *         number for the object. For arrays it is {@code true}
     *         iff the array's length is concrete and {@code ofst}
     *         is a possible array index.
     */
    boolean hasOffset(int ofst);

    /**
     * Gets the value in a field of this {@link Objekt}.
     * 
     * @param sig the {@link Signature} of the field.
     * @return a {@link Value} object which is the value 
     * stored in the field of this {@link Objekt}, or 
     * {@code null} if this {@link Objekt} has no 
     * field with signature {@code sig}. 
     */
    Value getFieldValue(Signature sig);

    /**
     * Gets the value in a field of the {@link Instance}.
     * 
     * @param fieldName the name of the field.
     * @param fieldClass the name of the class 
     * where the field is declared.
     * @return the {@link Value} stored in the 
     *         field of this {@link Objekt}, or 
     *         {@code null} if this {@link Objekt} 
     *         has no field with that {@code fieldName}
     *         and {@code fieldClass}.
     */
    Value getFieldValue(String fieldName, String fieldClass);
    
    /**
     * Gets the value in a field of this {@link Objekt}.
     * 
     * @param ofst an {@code int} signifying an offset number
     *        of a field (as returned by {@code sun.misc.Unsafe} methods).
     * @return a {@link Value} object which is the value 
     *         stored in the field of this {@link Objekt}, or 
     *         {@code null} if {@code slot} is not the slot number
     *         of a field. 
     */
    Value getFieldValue(int ofst);

    /**
     * Sets the value of a field. Throws a runtime exception 
     * in the case the field does not exist or is immutable.
     * 
     * @param field the {@link Signature} of the field.
     * @param item the new {@link Value} that must be assigned to
     *        the field.
     */
    //TODO throw a exception in the case a field does not exist or is immutable
    void setFieldValue(Signature field, Value item);

    /**
     * Sets the value of a field. Throws a runtime exception 
     * in the case the field does not exist or is immutable.
     * 
     * @param ofst an {@code int} signifying an offset number
     *        of a field (as returned by {@code sun.misc.Unsafe} methods).
     * @param item the new {@link Value} that must be assigned to
     *        the field.
     */
    //TODO throw exception in the case a field does not exist or is immutable
    void setFieldValue(int ofst, Value item);

    /**
     * Returns an immutable view of this 
     * {@link Objekt}'s fields.
     * 
     * @return an immutable 
     *         {@link Map}{@code <}{@link String}{@code , }{@link Variable}{@code >}
     *         backed by this {@link Objekt}'s fields map.
     */
    Map<Signature, Variable> fields();
    
    Objekt clone();
}
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
     * @return a {@link ClassFile}.
     */
    ClassFile getType();

    /**
     * Returns this {@link Objekt}'s origin.
     * 
     * @return a {@link ReferenceSymbolic}.
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
     * Checks whether an object has a slot with a given number.
     * 
     * @param slot an {@code int}.
     * @return {@code true} iff the object has a field with slot
     *         number {@code slot}. For arrays it is {@code true}
     *         iff the array's length is concrete and {@code slot}
     *         is in range.
     */
    boolean hasSlot(int slot);

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
     * @param slot an {@code int} signifying a slot number
     * of a field.
     * @return a {@link Value} object which is the value 
     * stored in the field of this {@link Objekt}, or 
     * {@code null} if {@code slot} is not the slot number
     * of a field. 
     */
    Value getFieldValue(int slot);

    /**
     * Returns the slot number of a field.
     * 
     * @param field the {@link Signature} of the field.
     * @return an {@code int} greater or equal to zero, 
     *         signifying the slot number of the field
     *         with signature {@code field}, or {@code -1}
     *         if such field does not exist.
     */
    int getFieldSlot(Signature field);

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
     * @param slot an {@code int} signifying a slot number
     *        of a field.
     * @param item the new {@link Value} that must be assigned to
     *        the field.
     */
    //TODO throw exception in the case a field does not exist or is immutable
    void setFieldValue(int slot, Value item);

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
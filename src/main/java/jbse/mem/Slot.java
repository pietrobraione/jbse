package jbse.mem;

import static jbse.bc.Signatures.JAVA_CLONEABLE;
import static jbse.bc.Signatures.JAVA_OBJECT;
import static jbse.bc.Signatures.JAVA_SERIALIZABLE;
import static jbse.common.Type.ARRAYOF;
import static jbse.common.Type.INT;
import static jbse.common.Type.KNOWN;
import static jbse.common.Type.NULLREF;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.TYPEEND;
import static jbse.common.Type.UNKNOWN;
import static jbse.common.Type.isCat_1;
import static jbse.common.Type.isPrimitiveIntegral;

import jbse.bc.Signature;
import jbse.common.exc.InvalidInputException;
import jbse.val.Value;
import jbse.val.exc.InvalidTypeException;

public interface Slot {
	/**
	 * Gets the value of this {@link Slot}. It might
	 * be {@code null}, according to the semantics
	 * this {@link Slot} gives to {@code null}.
	 * 
	 * @return a {@link Value} or possibly {@code null}.
	 */
	public Value getValue();

	/**
	 * Sets the value of this {@link Slot}. It might
	 * be {@code null}, according to the semantics
	 * this {@link Slot} gives to {@code null}.
	 * 
	 * @param value a {@link Value}.
	 * @throws InvalidInputException if {@code value == null}
	 *         whenever this {@link Slot} does not accept
	 *         {@code null} as a possible value, otherwise
	 *         never.
	 * @throws InvalidTypeException if {@code value} does not
	 *         agree with the {@link Slot}'s type.
	 */
	public void setValue(Value value) throws InvalidInputException, InvalidTypeException;

	/**
	 * Checks whether a slot may receive a value. The check is weak
	 * as it assumes only the type (char) of the value is known. 
	 * 
	 * @param slotDescriptor a {@link String}, the descriptor of the
	 *        field or variable {@link Signature} of the slot.
	 * @param valueType a {@code char}, the type of the value.
	 * @return {@code true} if the slot with descriptor {@code slotDescriptor}
	 *         may receive a value with type {@code valueType}.
	 */
    public static boolean slotMayReceiveValueWeak(String slotDescriptor, char valueType) {
    	final char slotType = slotDescriptor.charAt(0);        
    	final boolean slotTypeIsObjectCloneableOrSerializable = 
    	(slotDescriptor.equals("" + REFERENCE + JAVA_OBJECT + TYPEEND) ||
    	slotDescriptor.equals("" + REFERENCE + JAVA_CLONEABLE + TYPEEND) ||
    	slotDescriptor.equals("" + REFERENCE + JAVA_SERIALIZABLE + TYPEEND));
    	
    	final boolean retVal;
    	if (slotType == UNKNOWN || valueType == KNOWN || slotType == valueType) {
    		//trivial compatibility or identity between slot type and value type
    		retVal = true;
    	} else if (isPrimitiveIntegral(slotType) && isCat_1(slotType) && valueType == INT) {
    		//compatibility between integral types
    		retVal = true;
    	} else if ((slotType == REFERENCE || slotType == ARRAYOF) && (valueType == REFERENCE || valueType == NULLREF)) {
    		//the slot may receive a reference, and the value is a reference;
    		//note that references to arrays may have type REFERENCE!!!!
    		retVal = true;
    	} else if (slotTypeIsObjectCloneableOrSerializable && valueType == ARRAYOF) {
    		//the slot may receive a java.lang.Object, java.lang.Cloneable, or java.lang.Serializable,
    		//and the value is an array
    		retVal = true;
    	} else {
    		retVal = false;
    	}

    	return retVal;
    }
}

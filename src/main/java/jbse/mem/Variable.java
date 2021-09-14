package jbse.mem;

import jbse.common.exc.InvalidInputException;
import jbse.val.Calculator;
import jbse.val.Value;

/**
 * Class representing a named location into the memory, either a field 
 * or a local variable. It gathers the name, the declaration type and the 
 * runtime value stored in the location. 
 */
public final class Variable implements Slot, Cloneable {
    /** Type of the variable. */
    private final String type;

    /** Generic signature type of the variable. */
    private final String genericSignatureType;

    /** Name of the variable. */
    private final String name;

    /** {@link Value} stored in the variable. */
    private Value value;

    /**
     * Constructor setting the variable to the default value for its type.
     * 
     * @param calc a {@link Calculator}.
     * @param type a {@link String}, the type of the values stored in the variable.
     * @param genericSignatureType a {@link String}, the generic signature type of 
     *        the values stored in the variable.
     * @param name a {@link String}, the name of the variable.
     */
    Variable(Calculator calc, String type, String genericSignatureType, String name) {
    	this(calc, type, genericSignatureType, name, null);
    }

    /**
     * Constructor setting the variable to an initial value.
     * 
     * @param type a {@link String}, the type of the values stored in the variable.
     * @param genericSignatureType a {@link String}, the generic signature type of 
     *        the values stored in the variable.
     * @param name a {@link String}, the name of the variable.
     * @param value the initial {@link Value} of the variable.
     */
    Variable(String type, String genericSignatureType, String name, Value value) {
    	this(null, type, genericSignatureType, name, value);
    }
    
    /**
     * Private constructor.
     * 
     * @param calc a {@link Calculator}.
     * @param type a {@link String}, the type of the values stored in the variable.
     * @param genericSignatureType a {@link String}, the generic signature type of 
     *        the values stored in the variable.
     * @param name a {@link String}, the name of the variable.
     * @param value the initial {@link Value} of the variable, or {@code null}
     *        if the variable must be initialized with the default value for its
     *        {@code type}.
     */
    private Variable(Calculator calc, String type, String genericSignatureType, String name, Value value) {
        this.type = type;
        this.genericSignatureType = genericSignatureType;
        this.name = name;
        this.value = (value == null ? calc.valDefault(type.charAt(0)) : value);
    }
    
    /**
     * Returns the value of this variable.
	 * 
	 * @return a {@link Value}.
     */
    @Override
    public Value getValue() {
        return this.value;
    }

    /**
     * Sets the value of this variable.
     * 
	 * @throws InvalidInputException if {@code value == null}.
     */
    @Override
    public void setValue(Value value) throws InvalidInputException {
    	if (value == null) {
    		throw new InvalidInputException("Attempted to invoke Variable.setValue(Value) with Value parameter set to null.");
    	}
        //TODO check type
    	this.value = value;
    }

    /**
     * Returns the name of variable.
     * 
     * @return a {@link String}.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the type of this variable.
     * 
     * @return a {@link String}.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Returns the generic signature type of tis variable.
     * 
     * @return a {@link String}.
     */
    public String getGenericSignatureType() {
        return this.genericSignatureType;
    }

    @Override
    public Variable clone() {
        final Variable o;
        try {
            o = (Variable) super.clone();
        } catch (CloneNotSupportedException e) {
			throw new InternalError(e);
        }
        return o;
    }

    @Override
    public String toString() {
        final String tmp = (this.value == null) ? "<UNASSIGNED>" : this.value.toString();
        return "[Name:" + this.name + ", Type:" + this.type + ", Value:" + tmp + "]";
    }
}
package jbse.mem;

import jbse.val.Calculator;
import jbse.val.Value;

/**
 * Class representing a variable into the memory. It gathers the name, 
 * the declaration type and the runtime value of the variable. 
 */
public class Variable implements Cloneable {
    /** Type of the variable. */
    private final String type;

    /** Name of the variable. */
    private final String name;

    /** {@link Value} stored in the variable. */
    private Value value;

    /**
     * Constructor setting the variable to its default value.
     * 
     * @param calc an {@link Calculator}.
     * @param type the type of the variable.
     * @param name the name of the variable.
     */
    Variable(Calculator calc, String type, String name) {
    	this(calc, type, name, null);
    }

    /**
     * Constructor setting the variable to a prescribed value.
     * 
     * @param type the type of the variable.
     * @param name the name of variable.
     * @param value the initial {@link Value} of the variable.
     */
    Variable(String type, String name, Value value) {
        this.value = value;
        this.type = type;
        this.name = name;
    }
    
    /**
     * Constructor setting the variable to the default value.
     * 
     * @param type type of variable
     * @param name name of variable
     * @param value value of variable
     */
    private Variable(Calculator calc, String type, String name, Value value) {
        this.value = (value == null ? calc.valDefault(type.charAt(0)) : value);
        this.type = type;
        this.name = name;
    }
    
    /**
     * Returns the value of variable
     */
    public Value getValue() {
        return this.value;
    }

    /**
     * Set the value of variable
     */
    public void setValue(Value value) {
        //TODO check type of value
    	this.value = value;
    }

    /**
     * Returns the name of variable
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the type of variable
     */
    public String getType() {
        return this.type;
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
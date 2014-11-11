package jbse.mem;

/**
 * Class representing a variable into the memory. It gathers the name, 
 * the declaration type and the runtime value of the variable. 
 */
public class Variable implements Cloneable {
    /** Type of the variable. */
    private String type;

    /** Name of the variable. */
    private String name;

    /** {@link Value} stored in the variable. */
    private Value lnkValue;

    @Override
    public Object clone() {
        final Variable o;
        try {
            o = (Variable) super.clone();
        } catch (CloneNotSupportedException e) {
			throw new InternalError(e);
        }
        o.lnkValue = o.lnkValue.clone();
        return o;
    }

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
        this.lnkValue = value;
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
        this.lnkValue = (value == null ? calc.createDefault(type) : value);
        this.type = type;
        this.name = name;
    }
    
    /**
     * Returns the value of variable
     */
    public Value getValue() {
        return this.lnkValue;
    }

    /**
     * Set the value of variable
     */
    public void setValue(Value value) {
    	this.lnkValue = value;
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

    public String toString() {
        String tmp;
        if (this.lnkValue == null)
            tmp = "<UNASSIGNED>";
        else
            tmp = lnkValue.toString();
        return "[Name:" + this.name + ", Type:" + this.type + ", Value:" + tmp + "]";
    }
}
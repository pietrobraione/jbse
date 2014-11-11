package jbse.mem;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jbse.bc.Signature;

/**
 * Class that represent an instance of an object in memory.
 */

public class Instance extends Objekt {
    /**
     * Constructor.
     * 
     * @param calc a {@link Calculator}.
     * @param fieldSignatures an array of field {@link Signature}s.
     * @param className a {@code String}, the name of the class of 
     *        this {@link Instance} (e.g. {@code "java/lang/Object"}).
     * @param origin the origin of the {@code Instance}, if symbolic, 
     *        or {@code null}, if concrete.
     */
    protected Instance(Calculator calc, Signature[] fieldSignatures, String className, String origin, Epoch epoch) {
    	super(calc, fieldSignatures, className, origin, epoch);
    }

    /**
     * Sets the value of a field. Throws a runtime exception 
     * in the case the field does not exist.
     * 
     * @param field the {@link Signature} of the field.
     * @param item the new {@link Value} that must be assigned to
     *             the field.
     */
    public void setFieldValue(Signature field, Value item) {
        this.fields.get(field.toString()).setValue(item); //toString() is necessary, type erasure doesn't play well
    }
    
    public Map<String, Variable> fields() {
    	return Collections.unmodifiableMap(this.fields);
    }
    
    @Override
    public String toString() {
        String tmp = "[Class:" + this.type + ", Fields:{";
        int i = 0;
        for (Map.Entry<String, Variable> e : this.fields.entrySet()) {
            tmp += e.getValue().toString();
            if (i < this.fields.entrySet().size() - 1)
            	tmp += ", ";
            i++;
        }
        tmp += "}]";
        return tmp;
    }
    
    @Override
    public Instance clone() {
        Instance o = null;
        o = (Instance) super.clone();
        if (o == null) return null;
        
        HashMap<String, Variable> newPropertiesMap = new HashMap<String, Variable>();
        
        for (String key : fields.keySet()) {
            Variable value = fields.get(key);
            Variable valueCopy = (Variable) value.clone();
            newPropertiesMap.put(key, valueCopy);
        }
        
        o.fields = newPropertiesMap;
        return o;
    }
}
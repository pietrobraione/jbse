package jbse.mem;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jbse.bc.Signature;
import jbse.val.Calculator;
import jbse.val.Value;

/**
 * Class that represent an instance of an object in memory.
 */

public class Instance extends Objekt {
    /**
     * Constructor.
     * 
     * @param calc a {@link Calculator}.
     * @param className a {@code String}, the name of the class of 
     *        this {@link Instance} (e.g. {@code "java/lang/Object"}).
     * @param origin the origin of the {@code Instance}, if symbolic, 
     *        or {@code null}, if concrete.
     * @param epoch the creation {@link Epoch} of this {@link Instance}.
     * @param fieldSignatures varargs of field {@link Signature}s.
     */
    protected Instance(Calculator calc, String className, String origin, Epoch epoch, Signature... fieldSignatures) {
    	super(calc, className, origin, epoch, fieldSignatures);
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
        final StringBuffer buf = new StringBuffer();
        buf.append("[Class:");
        buf.append(this.type);
        buf.append(", Fields:{");
        boolean isFirst = true;
        for (Map.Entry<String, Variable> e : this.fields.entrySet()) {
            if (isFirst) {
                isFirst = false;
            } else {
                buf.append(", ");
            }
            buf.append(e.getValue().toString());
        }
        buf.append("}]");
        return buf.toString();
    }
    
    @Override
    public Instance clone() {
        final Instance o = (Instance) super.clone();
        
        final HashMap<String, Variable> newPropertiesMap = new HashMap<>();
        
        for (String key : fields.keySet()) {
            Variable value = fields.get(key);
            Variable valueCopy = (Variable) value.clone();
            newPropertiesMap.put(key, valueCopy);
        }
        
        o.fields = newPropertiesMap;
        return o;
    }
}
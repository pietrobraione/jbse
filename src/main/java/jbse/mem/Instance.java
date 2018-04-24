package jbse.mem;

import java.util.Map;

import jbse.bc.Signature;
import jbse.val.Calculator;
import jbse.val.HistoryPoint;
import jbse.val.ReferenceSymbolic;

/**
 * Class that represent an instance of an object in the heap.
 */
public class Instance extends Objekt {
    /**
     * Constructor.
     * 
     * @param symbolic a {@code boolean}, whether this object is symbolic
     *        (i.e., not explicitly created during symbolic execution by
     *        a {@code new*} bytecode, but rather assumed).
     * @param calc a {@link Calculator}.
     * @param className a {@code String}, the name of the class of 
     *        this {@link Instance} (e.g. {@code "java/lang/Object"}).
     * @param origin the {@link ReferenceSymbolic} providing origin of 
     *        the {@code Instance}, if symbolic, or {@code null}, if concrete.
     * @param epoch the creation {@link HistoryPoint} of this {@link Instance}. 
     * @param fieldSignatures varargs of field {@link Signature}s.
     */
    protected Instance(boolean symbolic, Calculator calc, String className, ReferenceSymbolic origin, HistoryPoint epoch, Signature... fieldSignatures) {
    	super(symbolic, calc, className, origin, epoch, fieldSignatures);
    }
    
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
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
        o.fields = fieldsDeepCopy();
        
        return o;
    }
}
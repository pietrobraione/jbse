package jbse.mem;

import java.util.Map;

import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.val.Calculator;
import jbse.val.HistoryPoint;
import jbse.val.ReferenceSymbolic;
import jbse.val.exc.InvalidTypeException;

/**
 * Class that implements {@link Instance}.
 */
public class InstanceImpl extends ObjektImpl implements Instance {
    /**
     * Constructor.
     * 
     * @param symbolic a {@code boolean}, whether this object is symbolic
     *        (i.e., not explicitly created during symbolic execution by
     *        a {@code new*} bytecode, but rather assumed).
     * @param calc a {@link Calculator}.
     * @param classFile a {@code classFile}, the class of 
     *        this {@link InstanceImpl}; It must be {@code classFile.}{@link ClassFile#isReference() isReference}{@code () == true}.
     * @param origin the {@link ReferenceSymbolic} providing origin of 
     *        the {@code Instance}, if symbolic, or {@code null}, if concrete.
     * @param epoch the creation {@link HistoryPoint} of this {@link InstanceImpl}. 
     * @param numOfStaticFields an {@code int}, the number of static fields.
     * @param fieldSignatures varargs of field {@link Signature}s, all the
     *        fields this instance knows.
     * @throws InvalidTypeException iff {@code classFile} is invalid. 
     */
    protected InstanceImpl(boolean symbolic, Calculator calc, ClassFile classFile, ReferenceSymbolic origin, HistoryPoint epoch, int numOfStaticFields, Signature... fieldSignatures) 
    throws InvalidTypeException {
        super(symbolic, calc, classFile, origin, epoch, false, numOfStaticFields, fieldSignatures);
        if (classFile == null || !classFile.isReference()) {
            throw new InvalidTypeException("Attempted creation of an instance with type " + classFile.getClassName());
        }
    }

	@Override
	ObjektWrapper<? extends ObjektImpl> makeWrapper(Heap destinationHeap, long destinationPosition) {
		return new InstanceWrapper(destinationHeap, destinationPosition, this);
	}
    
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append("[Class:");
        buf.append(this.classFile);
        buf.append(", Fields:{");
        boolean isFirst = true;
        for (Map.Entry<Signature, Variable> e : this.fields.entrySet()) {
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
    public InstanceImpl clone() {
        final InstanceImpl o = (InstanceImpl) super.clone();
        o.fields = fieldsDeepCopy();
        
        return o;
    }
}
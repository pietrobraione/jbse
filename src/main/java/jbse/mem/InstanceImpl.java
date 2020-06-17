package jbse.mem;

import java.util.Map;

import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.common.exc.InvalidInputException;
import jbse.val.Calculator;
import jbse.val.HistoryPoint;
import jbse.val.ReferenceSymbolic;
import jbse.val.exc.InvalidTypeException;

/**
 * Abstract superclass for the implementation of all kind of {@link Instance}s.
 */
public abstract class InstanceImpl extends HeapObjektImpl implements Instance {
	private boolean initial;
	
    /**
     * Constructor.
     * 
     * @param calc a {@link Calculator}. It must not be {@code null}. It will
     *        only be used during object construction and will not be stored
     *        in this {@link InstanceImpl}.
     * @param symbolic a {@code boolean}, whether this object is symbolic
     *        (i.e., not explicitly created during symbolic execution by
     *        a {@code new*} bytecode, but rather assumed).
     * @param classFile a {@code classFile}, the class of 
     *        this {@link InstanceImpl}; It must be 
     *        {@code classFile.}{@link ClassFile#isReference() isReference}{@code () == true}.
     * @param origin the {@link ReferenceSymbolic} providing origin of 
     *        the {@code Instance}, if symbolic, or {@code null}, if concrete.
     * @param epoch the creation {@link HistoryPoint} of this {@link InstanceImpl}. 
     * @param numOfStaticFields an {@code int}, the number of static fields.
     * @param fieldSignatures varargs of field {@link Signature}s, all the
     *        fields this instance knows.
     * @throws InvalidTypeException iff {@code classFile} is invalid. 
     */
    protected InstanceImpl(Calculator calc, boolean symbolic, ClassFile classFile, ReferenceSymbolic origin, HistoryPoint epoch, int numOfStaticFields, Signature... fieldSignatures) 
    throws InvalidTypeException {
        super(calc, symbolic, classFile, origin, epoch, false, numOfStaticFields, fieldSignatures);
        if (classFile == null || !classFile.isReference()) {
            throw new InvalidTypeException("Attempted creation of an instance with type " + classFile.getClassName() + ".");
        }
    }

	@Override
	abstract InstanceWrapper<? extends InstanceImpl> makeWrapper(Heap destinationHeap, long destinationPosition);
	
	@Override
	public final boolean isInitial() {
		return this.initial;
	}
	
	@Override
	public final void makeInitial() throws InvalidInputException {
		if (!isSymbolic()) {
			throw new InvalidInputException("Tried to invoke makeInitial() on an Instance that is not symbolic.");
		}
		this.initial = true;
	}
	
    @Override
    public final String toString() {
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
package jbse.mem;

import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.val.Calculator;
import jbse.val.HistoryPoint;
import jbse.val.ReferenceSymbolic;
import jbse.val.exc.InvalidTypeException;

/**
 * Class that implements the {@link Instance}s that do not deserve
 * special treatment.
 */
public final class InstanceImpl_DEFAULT extends InstanceImpl {
    /**
     * Constructor.
     * 
     * @param calc a {@link Calculator}. It must not be {@code null}. It will
     *        only be used during object construction and will not be stored
     *        in this {@link InstanceImpl_DEFAULT}.
     * @param symbolic a {@code boolean}, whether this object is symbolic
     *        (i.e., not explicitly created during symbolic execution by
     *        a {@code new*} bytecode, but rather assumed).
     * @param classFile a {@code classFile}, the class of 
     *        this {@link InstanceImpl_DEFAULT}; It must be 
     *        {@code classFile.}{@link ClassFile#isReference() isReference}{@code () == true}.
     * @param origin the {@link ReferenceSymbolic} providing origin of 
     *        the {@code Instance}, if symbolic, or {@code null}, if concrete.
     * @param epoch the creation {@link HistoryPoint} of this {@link InstanceImpl_DEFAULT}. 
     * @param numOfStaticFields an {@code int}, the number of static fields.
     * @param fieldSignatures varargs of field {@link Signature}s, all the
     *        fields this instance knows.
     * @throws InvalidTypeException iff {@code classFile} is invalid. 
     */
    protected InstanceImpl_DEFAULT(Calculator calc, boolean symbolic, ClassFile classFile, ReferenceSymbolic origin, HistoryPoint epoch, int numOfStaticFields, Signature... fieldSignatures) 
    throws InvalidTypeException {
        super(calc, symbolic, classFile, origin, epoch, numOfStaticFields, fieldSignatures);
    }

	@Override
	InstanceWrapper_DEFAULT makeWrapper(Heap destinationHeap, long destinationPosition) {
		return new InstanceWrapper_DEFAULT(destinationHeap, destinationPosition, this);
	}
    
    @Override
    public InstanceImpl_DEFAULT clone() {
        final InstanceImpl_DEFAULT o = (InstanceImpl_DEFAULT) super.clone();
        o.fields = fieldsDeepCopy();
        
        return o;
    }
}
package jbse.mem;

import static jbse.bc.Signatures.JAVA_CLASS;

import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.common.exc.InvalidInputException;
import jbse.val.Calculator;
import jbse.val.HistoryPoint;
import jbse.val.ReferenceConcrete;
import jbse.val.ReferenceSymbolic;
import jbse.val.exc.InvalidTypeException;

/**
 * Class that implements {@link Instance_JAVA_CLASS}.
 */
public final class InstanceImpl_JAVA_CLASS extends InstanceImpl implements Instance_JAVA_CLASS {
    /** The java class it represents. Immutable. */
    private final ClassFile representedClass;
    
    /** The signers of this class. Mutable. */
    private ReferenceConcrete signers;
    
    protected InstanceImpl_JAVA_CLASS(Calculator calc, ClassFile cf_JAVA_CLASS, ReferenceSymbolic origin, HistoryPoint epoch, ClassFile representedClass, int numOfStaticFields, Signature... fieldSignatures) 
    throws InvalidTypeException {
        super(calc, false, cf_JAVA_CLASS, origin, epoch, numOfStaticFields, fieldSignatures);
        if (cf_JAVA_CLASS == null || !JAVA_CLASS.equals(cf_JAVA_CLASS.getClassName())) {
            throw new InvalidTypeException("Attempted creation of an instance of java.lang.Class with type " + classFile.getClassName());
        }
        this.representedClass = representedClass;
        this.signers = null;
    }
    
    @Override
    InstanceWrapper_JAVA_CLASS makeWrapper(Heap destinationHeap, long destinationPosition) {
    	return new InstanceWrapper_JAVA_CLASS(destinationHeap, destinationPosition, this);
    }

    @Override
    public ClassFile representedClass() {
        return this.representedClass;
    }
    
    @Override
    public void setSigners(ReferenceConcrete signers) throws InvalidInputException {
        if (signers == null) {
            throw new InvalidInputException("It is not possible to set the signers of a class to null.");
        }
        //TODO check that signers points to an array of objects
        this.signers = signers;
    }
    
    @Override
    public ReferenceConcrete getSigners() {
        return this.signers;
    }
    
    @Override
    public void makeSymbolic(ReferenceSymbolic origin) throws InvalidInputException {
		throw new InvalidInputException("Attempted to makeSymbolic an instance of java.lang.Class.");
    }
    
    @Override
    public InstanceImpl_JAVA_CLASS clone() {
    	return (InstanceImpl_JAVA_CLASS) super.clone();
    }
}

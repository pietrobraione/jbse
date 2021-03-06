package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.algo.Util.invokeClassLoaderLoadClass;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.JAVA_FIELD;
import static jbse.bc.Signatures.JAVA_FIELD_ANNOTATIONS;
import static jbse.bc.Signatures.JAVA_FIELD_CLAZZ;
import static jbse.bc.Signatures.JAVA_FIELD_MODIFIERS;
import static jbse.bc.Signatures.JAVA_FIELD_NAME;
import static jbse.bc.Signatures.JAVA_FIELD_SIGNATURE;
import static jbse.bc.Signatures.JAVA_FIELD_SLOT;
import static jbse.bc.Signatures.JAVA_FIELD_TYPE;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.bc.Signatures.UNSUPPORTED_CLASS_VERSION_ERROR;
import static jbse.common.Type.ARRAYOF;
import static jbse.common.Type.className;
import static jbse.common.Type.isPrimitive;
import static jbse.common.Type.toPrimitiveOrVoidCanonicalName;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.TYPEEND;

import jbse.algo.InterruptException;
import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.FieldNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.PleaseLoadClassException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.Instance;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Calculator;
import jbse.val.ReferenceConcrete;

/**
 * Meta-level implementation of {@link java.lang.Class#getDeclaredFields0(boolean)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_CLASS_GETDECLAREDFIELDS0 extends Algo_JAVA_CLASS_GETDECLAREDX0 {
	public Algo_JAVA_CLASS_GETDECLAREDFIELDS0() {
		super("getDeclaredFields0", JAVA_FIELD_CLAZZ, JAVA_FIELD_SLOT, JAVA_FIELD_NAME, JAVA_FIELD_MODIFIERS, 
		      JAVA_FIELD_SIGNATURE, JAVA_FIELD_ANNOTATIONS);
	}
	
	@Override
	protected Signature[] getDeclared() {
		return this.thisClass.getDeclaredFields();
	}
	
	@Override
	protected boolean isPublic(Signature signature) {
		try {
			return this.thisClass.isFieldPublic(signature);
		} catch (FieldNotFoundException e) {
            throw new RuntimeException(e);
		}
	}
	
	@Override
	protected ReferenceConcrete createArray(State state, Calculator calc, int numDeclaredSignatures)
	throws ClassFileNotFoundException, ClassFileIllFormedException, BadClassFileVersionException,
	WrongClassNameException, IncompatibleClassFileException, ClassFileNotAccessibleException,
	RenameUnsupportedException, HeapMemoryExhaustedException, InvalidInputException {
        final ClassFile cf_arraOfJAVA_FIELD = state.getClassHierarchy().loadCreateClass("" + ARRAYOF + REFERENCE + JAVA_FIELD + TYPEEND);
        final ReferenceConcrete retVal = state.createArray(calc, null, calc.valInt(numDeclaredSignatures), cf_arraOfJAVA_FIELD);
        return retVal;
	}
	
	@Override
	protected ReferenceConcrete createInstance(State state, Calculator calc) throws ClassFileNotFoundException,
	ClassFileIllFormedException, BadClassFileVersionException, WrongClassNameException, IncompatibleClassFileException,
	ClassFileNotAccessibleException, RenameUnsupportedException, HeapMemoryExhaustedException, InvalidInputException {
        final ClassFile cf_JAVA_FIELD = state.getClassHierarchy().loadCreateClass(JAVA_FIELD);
        final ReferenceConcrete retVal = state.createInstance(calc, cf_JAVA_FIELD);
        return retVal;
	}
	
	@Override
	protected int getModifiers(Signature signature) {
		try {
			return this.thisClass.getFieldModifiers(signature);
		} catch (FieldNotFoundException e) {
            //this should never happen
            failExecution(e);
		}
		return 0; //to keep the compiler happy
	}
	
	@Override
	protected String getGenericSignatureType(Signature signature) {
		try {
			return this.thisClass.getFieldGenericSignatureType(signature);
		} catch (FieldNotFoundException e) {
            //this should never happen
            failExecution(e);
		}
		return null; //to keep the compiler happy
	}
	
	@Override
	protected byte[] getAnnotationsRaw(Signature signature) {
		try {
			return this.thisClass.getFieldAnnotationsRaw(signature);
		} catch (FieldNotFoundException e) {
            //this should never happen
            failExecution(e);
		}
		return null; //to keep the compiler happy
	}
	
    @Override
    protected void setRemainingFields(State state, Calculator calc, Signature signature, Instance object) 
    throws FrozenStateException, InvalidInputException, ClasspathException, ThreadStackEmptyException, 
    InterruptException {
    	//sets type
    	try {
    		final String fieldType = signature.getDescriptor();
    		ReferenceConcrete typeClassRef = null; //to keep the compiler happy
    		if (isPrimitive(fieldType)) {
    			try {
    				final String fieldTypeNameCanonical = toPrimitiveOrVoidCanonicalName(fieldType);
    				state.ensureInstance_JAVA_CLASS_primitiveOrVoid(calc, fieldTypeNameCanonical);
    				typeClassRef = state.referenceToInstance_JAVA_CLASS_primitiveOrVoid(fieldTypeNameCanonical);
    			} catch (ClassFileNotFoundException e) {
    				//this should never happen
    				failExecution(e);
    			}
    		} else {
    			final String fieldTypeClassName = className(fieldType);
    			//TODO *absolutely* put resolution of field type OUTSIDE (in cookMore)
    			final ClassFile fieldTypeClass = state.getClassHierarchy().resolveClass(this.thisClass, fieldTypeClassName, state.bypassStandardLoading()); //note that the accessor is the owner of the field, i.e., the 'this' class
    			state.ensureInstance_JAVA_CLASS(calc, fieldTypeClass);
    			typeClassRef = state.referenceToInstance_JAVA_CLASS(fieldTypeClass);
    		}
    		object.setFieldValue(JAVA_FIELD_TYPE, typeClassRef);
    	} catch (PleaseLoadClassException e) {
    		invokeClassLoaderLoadClass(state, calc, e);
    		exitFromAlgorithm();
    	} catch (HeapMemoryExhaustedException e) {
    		throwNew(state, calc, OUT_OF_MEMORY_ERROR);
    		exitFromAlgorithm();
    	} catch (ClassFileNotFoundException e) {
    		//TODO this exception should wrap a ClassNotFoundException
    		//TODO is it right?
    		throwNew(state, calc, NO_CLASS_DEFINITION_FOUND_ERROR);
    		exitFromAlgorithm();
    	} catch (BadClassFileVersionException e) {
    		throwNew(state, calc, UNSUPPORTED_CLASS_VERSION_ERROR);
    		exitFromAlgorithm();
    	} catch (WrongClassNameException e) {
    		throwNew(state, calc, NO_CLASS_DEFINITION_FOUND_ERROR); //without wrapping a ClassNotFoundException
    		exitFromAlgorithm();
    	} catch (ClassFileNotAccessibleException e) {
    		throwNew(state, calc, ILLEGAL_ACCESS_ERROR);
    		exitFromAlgorithm();
    	} catch (IncompatibleClassFileException e) {
    		throwNew(state, calc, INCOMPATIBLE_CLASS_CHANGE_ERROR);
    		exitFromAlgorithm();
    	} catch (ClassFileIllFormedException e) {
    		//TODO should throw a subclass of LinkageError
    		throwVerifyError(state, calc);
    		exitFromAlgorithm();
    	} catch (RenameUnsupportedException e) {
    		//this should never happen
    		failExecution(e);
    	}
    }
}

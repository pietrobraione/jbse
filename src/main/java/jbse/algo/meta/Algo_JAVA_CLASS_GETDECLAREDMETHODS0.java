package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.algo.Util.invokeClassLoaderLoadClass;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.JAVA_CLASS;
import static jbse.bc.Signatures.JAVA_METHOD;
import static jbse.bc.Signatures.JAVA_METHOD_ANNOTATIONS;
import static jbse.bc.Signatures.JAVA_METHOD_CLAZZ;
import static jbse.bc.Signatures.JAVA_METHOD_EXCEPTIONTYPES;
import static jbse.bc.Signatures.JAVA_METHOD_MODIFIERS;
import static jbse.bc.Signatures.JAVA_METHOD_NAME;
import static jbse.bc.Signatures.JAVA_METHOD_PARAMETERTYPES;
import static jbse.bc.Signatures.JAVA_METHOD_RETURNTYPE;
import static jbse.bc.Signatures.JAVA_METHOD_SIGNATURE;
import static jbse.bc.Signatures.JAVA_METHOD_SLOT;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.bc.Signatures.UNSUPPORTED_CLASS_VERSION_ERROR;
import static jbse.common.Type.ARRAYOF;
import static jbse.common.Type.className;
import static jbse.common.Type.isArray;
import static jbse.common.Type.isPrimitive;
import static jbse.common.Type.isReference;
import static jbse.common.Type.isVoid;
import static jbse.common.Type.toPrimitiveOrVoidCanonicalName;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.splitParametersDescriptors;
import static jbse.common.Type.splitReturnValueDescriptor;
import static jbse.common.Type.TYPEEND;

import jbse.algo.InterruptException;
import jbse.bc.ClassFile;
import jbse.bc.ClassHierarchy;
import jbse.bc.Signature;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.bc.exc.PleaseLoadClassException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.Array;
import jbse.mem.Instance;
import jbse.mem.State;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Calculator;
import jbse.val.ReferenceConcrete;
import jbse.val.exc.InvalidTypeException;

/**
 * Meta-level implementation of {@link java.lang.Class#getDeclaredMethods0(boolean)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_CLASS_GETDECLAREDMETHODS0 extends Algo_JAVA_CLASS_GETDECLAREDX0 {
	public Algo_JAVA_CLASS_GETDECLAREDMETHODS0() {
		super("getDeclaredMethods0", JAVA_METHOD_CLAZZ, JAVA_METHOD_SLOT, JAVA_METHOD_NAME, JAVA_METHOD_MODIFIERS, 
		      JAVA_METHOD_SIGNATURE, JAVA_METHOD_ANNOTATIONS);
	}
	
	@Override
	protected Signature[] getDeclared() {
		return this.thisClass.getDeclaredMethods();
	}
	
	@Override
	protected boolean isPublic(Signature signature) {
		try {
			return this.thisClass.isMethodPublic(signature);
		} catch (MethodNotFoundException e) {
            throw new RuntimeException(e);
		}
	}
	
	@Override
	protected ReferenceConcrete createArray(State state, Calculator calc, int numDeclaredSignatures)
	throws ClassFileNotFoundException, ClassFileIllFormedException, BadClassFileVersionException,
	WrongClassNameException, IncompatibleClassFileException, ClassFileNotAccessibleException,
	RenameUnsupportedException, HeapMemoryExhaustedException, InvalidInputException {
        final ClassFile cf_arraOfJAVA_METHOD = state.getClassHierarchy().loadCreateClass("" + ARRAYOF + REFERENCE + JAVA_METHOD + TYPEEND);
        final ReferenceConcrete retVal = state.createArray(calc, null, calc.valInt(numDeclaredSignatures), cf_arraOfJAVA_METHOD);	
        return retVal;
	}
	
	@Override
	protected ReferenceConcrete createInstance(State state, Calculator calc) 
	throws ClassFileNotFoundException, ClassFileIllFormedException, BadClassFileVersionException, 
	WrongClassNameException, IncompatibleClassFileException, ClassFileNotAccessibleException, 
	RenameUnsupportedException, HeapMemoryExhaustedException, InvalidInputException {
        final ClassFile cf_JAVA_METHOD = state.getClassHierarchy().loadCreateClass(JAVA_METHOD);
        final ReferenceConcrete retVal = state.createInstance(calc, cf_JAVA_METHOD);
        return retVal;
	}

	@Override
	protected int getModifiers(Signature signature) {
		try {
			return this.thisClass.getMethodModifiers(signature);
		} catch (MethodNotFoundException e) {
            //this should never happen
            failExecution(e);
		}
		return 0; //to keep the compiler happy
	}
	
	@Override
	protected String getGenericSignatureType(Signature signature) {
		try {
			return this.thisClass.getMethodGenericSignatureType(signature);
		} catch (MethodNotFoundException e) {
            //this should never happen
            failExecution(e);
		}
		return null; //to keep the compiler happy
	}
	
	@Override
	protected byte[] getAnnotationsRaw(Signature signature) {
		try {
			return this.thisClass.getMethodAnnotationsRaw(signature);
		} catch (MethodNotFoundException e) {
            //this should never happen
            failExecution(e);
		}
		return null; //to keep the compiler happy
	}
	
    @Override
    protected void setRemainingFields(State state, Calculator calc, Signature signature, Instance object) 
    throws FrozenStateException, InvalidInputException, InvalidTypeException, ClasspathException, 
    ThreadStackEmptyException, InterruptException {
    	final ClassHierarchy hier = state.getClassHierarchy();
    	
    	//gets class for Class[]
    	ClassFile cf_arrayOfJAVA_CLASS = null; //to keep the compiler happy
    	try {
    		cf_arrayOfJAVA_CLASS = hier.loadCreateClass("" + ARRAYOF + REFERENCE + JAVA_CLASS + TYPEEND);
    	} catch (ClassFileNotFoundException | ClassFileIllFormedException | BadClassFileVersionException |
    	WrongClassNameException | IncompatibleClassFileException | ClassFileNotAccessibleException e) {
    		throw new ClasspathException(e);
    	} catch (RenameUnsupportedException e) {
    		//this should never happen
    		failExecution(e);
    	}

    	//sets returnType and parameterTypes
    	try {
    		//sets returnType
    		final String returnType = splitReturnValueDescriptor(signature.getDescriptor());
    		final ReferenceConcrete returnClassRef;
    		if (isPrimitive(returnType) || isVoid(returnType)) {
    			final String returnTypeNameCanonical = toPrimitiveOrVoidCanonicalName(returnType);
    			try {
    				state.ensureInstance_JAVA_CLASS_primitiveOrVoid(calc, returnTypeNameCanonical);
    			} catch (ClassFileNotFoundException e) {
    				//this should never happen
    				failExecution(e);
    			}
    			returnClassRef = state.referenceToInstance_JAVA_CLASS_primitiveOrVoid(returnTypeNameCanonical);
    		} else if (isArray(returnType) || isReference(returnType)) {
    			final String returnTypeClassName = className(returnType);
    			//TODO *absolutely* put resolution of return type OUTSIDE (in cookMore)
    			final ClassFile returnTypeClass = hier.resolveClass(this.thisClass, returnTypeClassName, state.bypassStandardLoading()); //note that the accessor is the owner of the constructor, i.e., the 'this' class
    			state.ensureInstance_JAVA_CLASS(calc, returnTypeClass);
    			returnClassRef = state.referenceToInstance_JAVA_CLASS(returnTypeClass);
    		} else {
    			//this should never happen
    			failExecution("Found an ill-formed descriptor (return type) in method signature " + signature.toString() + ".");
    			return; //to keep the compiler happy
    		}
    		object.setFieldValue(JAVA_METHOD_RETURNTYPE, returnClassRef);

    		//creates the array and puts it in parameterTypes
    		final String[] params = splitParametersDescriptors(signature.getDescriptor());
    		final ReferenceConcrete arrayParamClassesRef = state.createArray(calc, null, calc.valInt(params.length), cf_arrayOfJAVA_CLASS);
    		object.setFieldValue(JAVA_METHOD_PARAMETERTYPES, arrayParamClassesRef);
    		final Array arrayParamClasses = (Array) state.getObject(arrayParamClassesRef);

    		//populates parameterTypes
    		int i = 0;
    		for (String paramType : params) {
    			final ReferenceConcrete paramClassRef;
    			if (isPrimitive(paramType)) {
    				final String paramTypeNameCanonical = toPrimitiveOrVoidCanonicalName(paramType);
    				try {
    					state.ensureInstance_JAVA_CLASS_primitiveOrVoid(calc, paramTypeNameCanonical);
    				} catch (ClassFileNotFoundException e) {
    					//this should never happen
    					failExecution(e);
    				}
    				paramClassRef = state.referenceToInstance_JAVA_CLASS_primitiveOrVoid(paramTypeNameCanonical);
    			} else if (isArray(paramType) || isReference(paramType)) {
    				final String paramTypeClassName = className(paramType);
    				//TODO *absolutely* put resolution of parameter types OUTSIDE (in cookMore)
    				final ClassFile paramTypeClass = hier.resolveClass(this.thisClass, paramTypeClassName, state.bypassStandardLoading()); //note that the accessor is the owner of the constructor, i.e., the 'this' class
    				state.ensureInstance_JAVA_CLASS(calc, paramTypeClass);
    				paramClassRef = state.referenceToInstance_JAVA_CLASS(paramTypeClass);
    			} else {
    				//this should never happen
    				failExecution("Found an ill-formed descriptor (parameter type) in method signature " + signature.toString() + ".");
    				return; //to keep the compiler happy
    			}
    			arrayParamClasses.setFast(calc.valInt(i), paramClassRef);
    			++i;
    		}
    	} catch (PleaseLoadClassException e) {
    		invokeClassLoaderLoadClass(state, calc, e);
    		exitFromAlgorithm();
    	} catch (HeapMemoryExhaustedException e) {
    		throwNew(state, calc, OUT_OF_MEMORY_ERROR);
    		exitFromAlgorithm();
    	} catch (ClassFileNotFoundException e) {
    		//TODO this exception should wrap a ClassNotFoundException
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
    	} catch (RenameUnsupportedException | FastArrayAccessNotAllowedException e) {
    		//this should never happen
    		failExecution(e);
    	}

    	//sets exceptionTypes
    	try {
    		//creates the array and puts it in exceptionTypes
    		final String[] exceptions = this.thisClass.getMethodThrownExceptions(signature);
    		final ReferenceConcrete arrayExcClassesRef = state.createArray(calc, null, calc.valInt(exceptions.length), cf_arrayOfJAVA_CLASS);
    		object.setFieldValue(JAVA_METHOD_EXCEPTIONTYPES, arrayExcClassesRef);
    		final Array arrayExcClasses = (Array) state.getObject(arrayExcClassesRef);

    		//populates exceptionTypes
    		int i = 0;
    		for (String excClassName : exceptions) {
    			//TODO *absolutely* put resolution of exception types OUTSIDE (in cookMore)
    			final ClassFile excClass = hier.resolveClass(this.thisClass, excClassName, state.bypassStandardLoading());
    			state.ensureInstance_JAVA_CLASS(calc, excClass);
    			final ReferenceConcrete excClazz = state.referenceToInstance_JAVA_CLASS(excClass);
    			arrayExcClasses.setFast(calc.valInt(i), excClazz);
    			++i;
    		}
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
    	} catch (MethodNotFoundException e) {
    		//TODO is it ok?
    		throwVerifyError(state, calc);
    		exitFromAlgorithm();
    	} catch (RenameUnsupportedException | FastArrayAccessNotAllowedException e) {
    		//this should never happen
    		failExecution(e);
    	}

    	//TODO parameterAnnotations, annotationDefault, (??) typeAnnotations
    }
}

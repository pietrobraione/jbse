package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.invokeClassLoaderLoadClass;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.JAVA_ACCESSIBLEOBJECT_OVERRIDE;
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
import static jbse.common.Type.BYTE;
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

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.CannotManageStateException;
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
import jbse.dec.exc.DecisionException;
import jbse.mem.Array;
import jbse.mem.Instance;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Null;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;
import jbse.val.Simplex;

/**
 * Meta-level implementation of {@link java.lang.Class#getDeclaredMethods0(boolean)}.
 * 
 * @author Pietro Braione
 */
//TODO unify with Algo_JAVA_CLASS_GETDECLAREDFIELDS0 and Algo_JAVA_CLASS_GETDECLAREDCONSTRUCTORS0
public final class Algo_JAVA_CLASS_GETDECLAREDMETHODS0 extends Algo_INVOKEMETA_Nonbranching {
    private ClassFile thisClass; //set by cookMore

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }

    @Override
    protected void cookMore(State state)
    throws ThreadStackEmptyException, DecisionException, ClasspathException,
    CannotManageStateException, InterruptException, FrozenStateException {
        try {           
            //gets the classfile for the class represented by 'this'
            final Reference thisClassRef = (Reference) this.data.operand(0);
            if (state.isNull(thisClassRef)) {
                //this should never happen
                failExecution("The 'this' parameter to java.lang.Class.getDeclaredMethods0 method is null.");
            }
            final Instance_JAVA_CLASS thisClassObject = (Instance_JAVA_CLASS) state.getObject(thisClassRef);
            this.thisClass = thisClassObject.representedClass();
        } catch (ClassCastException e) {
            throwVerifyError(state, this.ctx.getCalculator());
            exitFromAlgorithm();
        }
        //TODO check that operands are concrete and kill path if they are not
        
        //TODO resolve all parameter/return/exception types of all methods!!!
    }

    @Override
    protected final StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            final ClassHierarchy hier = state.getClassHierarchy();
            final Calculator calc = this.ctx.getCalculator();
            
            //gets the signatures of the methods to emit; the position of the signature
            //in sigMethods indicates its slot
            final boolean onlyPublic = ((Simplex) this.data.operand(1)).surelyTrue();
            final List<Signature> sigMethods;
            try {
                sigMethods = Arrays.stream(this.thisClass.getDeclaredMethods())
                .map(sig -> {
                    try {
                        if (onlyPublic && !this.thisClass.isMethodPublic(sig)) {
                            return null;
                        } else {
                            return sig;
                        }
                    } catch (MethodNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
            } catch (RuntimeException e) {
                if (e.getCause() instanceof MethodNotFoundException) {
                    //this should never happen
                    failExecution((Exception) e.getCause());
                }
                throw e;
            }

            final int numDeclaredMethods = sigMethods.stream()
            .map(s -> (s == null ? 0 : 1))
            .reduce(0, (a, b) -> a + b);


            //builds the array to return
            ReferenceConcrete result = null; //to keep the compiler happy
            try {
                final ClassFile cf_arraOfJAVA_METHOD = hier.loadCreateClass("" + ARRAYOF + REFERENCE + JAVA_METHOD + TYPEEND);
                result = state.createArray(calc, null, calc.valInt(numDeclaredMethods), cf_arraOfJAVA_METHOD);
            } catch (HeapMemoryExhaustedException e) {
                throwNew(state, calc, OUT_OF_MEMORY_ERROR);
                exitFromAlgorithm();
            } catch (ClassFileNotFoundException | ClassFileIllFormedException | BadClassFileVersionException |
                     WrongClassNameException | IncompatibleClassFileException | ClassFileNotAccessibleException e) {
                throw new ClasspathException(e);
            } catch (RenameUnsupportedException e) {
            	//this should never happen
            	failExecution(e);
            }

            //constructs the java.lang.reflect.Method objects and fills the array
            final Reference thisClassRef = (Reference) this.data.operand(0);
            final Array resultArray = (Array) state.getObject(result);
            int index = 0;
            int slot = 0;
            for (Signature sigMethod : sigMethods) {
                if (sigMethod != null) {
                    //creates an instance of java.lang.reflect.Method and 
                    //puts it in the return array
                    ReferenceConcrete methodRef = null; //to keep the compiler happy
                    try {
                        final ClassFile cf_JAVA_METHOD = hier.loadCreateClass(JAVA_METHOD);
                        methodRef = state.createInstance(calc, cf_JAVA_METHOD);
                        resultArray.setFast(calc.valInt(index), methodRef);
                    } catch (HeapMemoryExhaustedException e) {
                        throwNew(state, calc, OUT_OF_MEMORY_ERROR);
                        exitFromAlgorithm();
                    } catch (ClassFileNotFoundException | ClassFileIllFormedException | BadClassFileVersionException |
                             WrongClassNameException | IncompatibleClassFileException | ClassFileNotAccessibleException e) {
                        throw new ClasspathException(e);
                    } catch (RenameUnsupportedException | FastArrayAccessNotAllowedException e) {
                        //this should never happen
                        failExecution(e);
                    }

                    //from here initializes the java.lang.reflect.Method instance
                    final Instance method = (Instance) state.getObject(methodRef);

                    //sets clazz
                    method.setFieldValue(JAVA_METHOD_CLAZZ, thisClassRef);

                    //sets slot
                    method.setFieldValue(JAVA_METHOD_SLOT, calc.valInt(slot));

                    //gets class for Class[]
                    ClassFile cf_arraOfJAVA_CLASS = null; //to keep the compiler happy
                    try {
                        cf_arraOfJAVA_CLASS = hier.loadCreateClass("" + ARRAYOF + REFERENCE + JAVA_CLASS + TYPEEND);
                    } catch (ClassFileNotFoundException | ClassFileIllFormedException | BadClassFileVersionException |
                             WrongClassNameException | IncompatibleClassFileException | ClassFileNotAccessibleException e) {
                        throw new ClasspathException(e);
                    } catch (RenameUnsupportedException e) {
                    	//this should never happen
                    	failExecution(e);
                    }
                    
                    //sets name
                    try {
                        state.ensureStringLiteral(calc, sigMethod.getName());
                        method.setFieldValue(JAVA_METHOD_NAME, state.referenceToStringLiteral(sigMethod.getName()));
                    } catch (HeapMemoryExhaustedException e) {
                        throwNew(state, calc, OUT_OF_MEMORY_ERROR);
                        exitFromAlgorithm();
                    }

                    //sets returnType and parameterTypes
                    try {
                        //sets returnType
                        final String returnType = splitReturnValueDescriptor(sigMethod.getDescriptor());
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
                            failExecution("Found an ill-formed descriptor (return type) in method signature " + sigMethod.toString() + ".");
                            return; //to keep the compiler happy
                        }
                        method.setFieldValue(JAVA_METHOD_RETURNTYPE, returnClassRef);
                        
                        //creates the array and puts it in parameterTypes
                        final String[] params = splitParametersDescriptors(sigMethod.getDescriptor());
                        final ReferenceConcrete arrayParamClassesRef = state.createArray(calc, null, calc.valInt(params.length), cf_arraOfJAVA_CLASS);
                        method.setFieldValue(JAVA_METHOD_PARAMETERTYPES, arrayParamClassesRef);
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
                                failExecution("Found an ill-formed descriptor (parameter type) in method signature " + sigMethod.toString() + ".");
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
                        final String[] exceptions = this.thisClass.getMethodThrownExceptions(sigMethod);
                        final ReferenceConcrete arrayExcClassesRef = state.createArray(calc, null, calc.valInt(exceptions.length), cf_arraOfJAVA_CLASS);
                        method.setFieldValue(JAVA_METHOD_EXCEPTIONTYPES, arrayExcClassesRef);
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

                    //sets modifiers
                    try {
                        method.setFieldValue(JAVA_METHOD_MODIFIERS, calc.valInt(this.thisClass.getMethodModifiers(sigMethod)));
                    } catch (MethodNotFoundException e) {
                        //this should never happen
                        failExecution(e);
                    }

                    //sets override
                    method.setFieldValue(JAVA_ACCESSIBLEOBJECT_OVERRIDE, calc.valBoolean(false));

                    //sets signature
                    try {
                        final String sigType = this.thisClass.getMethodGenericSignatureType(sigMethod);
                        final ReferenceConcrete refSigType;
                        if (sigType == null) {
                            refSigType = Null.getInstance();
                        } else {
                            state.ensureStringLiteral(calc, sigType);
                            refSigType = state.referenceToStringLiteral(sigType);
                        }
                        method.setFieldValue(JAVA_METHOD_SIGNATURE, refSigType);
                    } catch (HeapMemoryExhaustedException e) {
                        throwNew(state, calc, OUT_OF_MEMORY_ERROR);
                        exitFromAlgorithm();
                    } catch (MethodNotFoundException e) {
                        //this should never happen
                        failExecution(e);
                    }

                    //sets annotations
                    try {
                        final byte[] annotations = this.thisClass.getMethodAnnotationsRaw(sigMethod);
                        final ClassFile cf_arrayOfBYTE = hier.loadCreateClass("" + ARRAYOF + BYTE);
                        final ReferenceConcrete annotationsRef = (annotations.length == 0 ? Null.getInstance() : state.createArray(calc, null, calc.valInt(annotations.length), cf_arrayOfBYTE));
                        method.setFieldValue(JAVA_METHOD_ANNOTATIONS, annotationsRef);

                        //populates annotations
                        if (annotations.length > 0) {
                        	final Array annotationsArray = (Array) state.getObject(annotationsRef);
                        	for (int i = 0; i < annotations.length; ++i) {
                        		annotationsArray.setFast(calc.valInt(i), calc.valByte(annotations[i]));
                        	}
                        }
                    } catch (HeapMemoryExhaustedException e) {
                        throwNew(state, calc, OUT_OF_MEMORY_ERROR);
                        exitFromAlgorithm();
                    } catch (MethodNotFoundException | ClassFileNotFoundException | 
                             ClassFileIllFormedException | BadClassFileVersionException |
                             RenameUnsupportedException | WrongClassNameException | 
                             ClassFileNotAccessibleException | IncompatibleClassFileException | 
                             FastArrayAccessNotAllowedException e) {
                        //this should never happen
                        failExecution(e);
                    }

                    //TODO parameterAnnotations, annotationDefault, (??) typeAnnotations

                    ++index;
                }
                ++slot;
            }


            //returns the array
            state.pushOperand(result);
        };
    }
}

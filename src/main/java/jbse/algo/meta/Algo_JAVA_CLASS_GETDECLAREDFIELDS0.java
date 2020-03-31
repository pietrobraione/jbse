package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.invokeClassLoaderLoadClass;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.JAVA_ACCESSIBLEOBJECT_OVERRIDE;
import static jbse.bc.Signatures.JAVA_FIELD;
import static jbse.bc.Signatures.JAVA_FIELD_ANNOTATIONS;
import static jbse.bc.Signatures.JAVA_FIELD_CLAZZ;
import static jbse.bc.Signatures.JAVA_FIELD_MODIFIERS;
import static jbse.bc.Signatures.JAVA_FIELD_NAME;
import static jbse.bc.Signatures.JAVA_FIELD_SIGNATURE;
import static jbse.bc.Signatures.JAVA_FIELD_SLOT;
import static jbse.bc.Signatures.JAVA_FIELD_TYPE;
import static jbse.bc.Signatures.JAVA_THROWABLE;
import static jbse.bc.Signatures.JAVA_THROWABLE_BACKTRACE;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.bc.Signatures.UNSUPPORTED_CLASS_VERSION_ERROR;
import static jbse.common.Type.ARRAYOF;
import static jbse.common.Type.BYTE;
import static jbse.common.Type.className;
import static jbse.common.Type.isPrimitive;
import static jbse.common.Type.toPrimitiveOrVoidCanonicalName;
import static jbse.common.Type.REFERENCE;
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
import jbse.bc.exc.FieldNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
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
 * Meta-level implementation of {@link java.lang.Class#getDeclaredFields0(boolean)}.
 * 
 * @author Pietro Braione
 */
//TODO unify with Algo_JAVA_CLASS_GETDECLAREDCONSTRUCTORS0 and Algo_JAVA_CLASS_GETDECLAREDMETHODS0
public final class Algo_JAVA_CLASS_GETDECLAREDFIELDS0 extends Algo_INVOKEMETA_Nonbranching {
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
            //gets the classfile represented by the 'this' parameter
            final Reference thisClassRef = (Reference) this.data.operand(0);
            if (state.isNull(thisClassRef)) {
                //this should never happen
                failExecution("The 'this' parameter to java.lang.Class.getDeclaredFields0 method is null.");
            }
            final Instance_JAVA_CLASS thisClassObject = (Instance_JAVA_CLASS) state.getObject(thisClassRef); //TODO check that operand is concrete and not null
            this.thisClass = thisClassObject.representedClass();
        } catch (ClassCastException e) {
            throwVerifyError(state, this.ctx.getCalculator());
            exitFromAlgorithm();
        }
        
        //TODO resolve all field types!!!
    }
    
    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            final ClassHierarchy hier = state.getClassHierarchy();
            final Calculator calc = this.ctx.getCalculator();
            
            //gets the signatures of the fields to emit; the position of the signature
            //in sigFields indicates its slot
            final boolean onlyPublic = ((Simplex) this.data.operand(1)).surelyTrue();
            final List<Signature> sigFields;
            final boolean skipBacktrace;
            try {
                final boolean[] _skipBacktrace = new boolean[1]; //boxed boolean so the closure can access it
                sigFields = Arrays.stream(this.thisClass.getDeclaredFields())
                .map(sig -> {
                    try {
                        if (onlyPublic && !this.thisClass.isFieldPublic(sig)) {
                            return null;
                        } else if (JAVA_THROWABLE.equals(this.thisClass.getClassName()) && JAVA_THROWABLE_BACKTRACE.equals(sig)) {
                            //we need to exclude the backtrace field of java.lang.Throwable
                            _skipBacktrace[0] = true;
                            return null;
                        } else {
                            return sig;
                        }
                    } catch (FieldNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
                skipBacktrace = _skipBacktrace[0]; //unboxes
            } catch (RuntimeException e) {
                if (e.getCause() instanceof FieldNotFoundException) {
                    //this should never happen
                    failExecution((Exception) e.getCause());
                }
                throw e;
            }

            final int numDeclaredFields = sigFields.stream()
            .map(s -> (s == null ? 0 : 1))
            .reduce(0, (a, b) -> a + b);
            
            //gets class for Field[]
            ClassFile cf_arraOfJAVA_FIELD = null; //to keep the compiler happy
            try {
                cf_arraOfJAVA_FIELD = hier.loadCreateClass("" + ARRAYOF + REFERENCE + JAVA_FIELD + TYPEEND);
            } catch (ClassFileNotFoundException | ClassFileIllFormedException | BadClassFileVersionException |
                     WrongClassNameException | IncompatibleClassFileException | ClassFileNotAccessibleException e) {
                throw new ClasspathException(e);
            } catch (RenameUnsupportedException e) {
            	//this should never happen
            	failExecution(e);
            }


            //builds the array to return
            ReferenceConcrete result = null; //to keep the compiler happy
            try {
                result = state.createArray(calc, null, calc.valInt(numDeclaredFields), cf_arraOfJAVA_FIELD);
            } catch (HeapMemoryExhaustedException e) {
                throwNew(state, calc, OUT_OF_MEMORY_ERROR);
                exitFromAlgorithm();
            }

            //constructs the java.lang.reflect.Field objects and fills the array
            final Reference thisClassRef = (Reference) this.data.operand(0);
            final Array resultArray = (Array) state.getObject(result);
            int index = 0;
            int slot = 0;
            for (Signature sigField : sigFields) {
                if (sigField != null && !(skipBacktrace && JAVA_THROWABLE_BACKTRACE.equals(sigField))) {
                    //creates an instance of java.lang.reflect.Field and 
                    //puts it in the return array
                    ReferenceConcrete fieldRef = null; //to keep the compiler happy
                    try {
                        final ClassFile cf_JAVA_FIELD = hier.loadCreateClass(JAVA_FIELD);
                        fieldRef = state.createInstance(calc, cf_JAVA_FIELD);
                        resultArray.setFast(calc.valInt(index) , fieldRef);
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

                    //from here initializes the java.lang.reflect.Field instance
                    final Instance field = (Instance) state.getObject(fieldRef);

                    //sets clazz
                    field.setFieldValue(JAVA_FIELD_CLAZZ, thisClassRef);

                    //sets name
                    try {
                        state.ensureStringLiteral(calc, sigField.getName());
                    } catch (HeapMemoryExhaustedException e) {
                        throwNew(state, calc, OUT_OF_MEMORY_ERROR);
                        exitFromAlgorithm();
                    }
                    final ReferenceConcrete refSigName = state.referenceToStringLiteral(sigField.getName());
                    field.setFieldValue(JAVA_FIELD_NAME, refSigName);

                    //sets modifiers
                    try {
                        field.setFieldValue(JAVA_FIELD_MODIFIERS, calc.valInt(this.thisClass.getFieldModifiers(sigField)));
                    } catch (FieldNotFoundException e) {
                        //this should never happen
                        failExecution(e);
                    }

                    //sets signature
                    try {
                        final String sigType = this.thisClass.getFieldGenericSignatureType(sigField);
                        final ReferenceConcrete refSigType;
                        if (sigType == null) {
                            refSigType = Null.getInstance();
                        } else {
                            state.ensureStringLiteral(calc, sigType);
                            refSigType = state.referenceToStringLiteral(sigType);
                        }
                        field.setFieldValue(JAVA_FIELD_SIGNATURE, refSigType);
                    } catch (HeapMemoryExhaustedException e) {
                        throwNew(state, calc, OUT_OF_MEMORY_ERROR);
                        exitFromAlgorithm();
                    } catch (FieldNotFoundException e) {
                        //this should never happen
                        failExecution(e);
                    }

                    //sets slot
                    field.setFieldValue(JAVA_FIELD_SLOT, calc.valInt(slot));

                    //sets type
                    try {
                        final String fieldType = sigField.getDescriptor();
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
                            final ClassFile fieldTypeClass = hier.resolveClass(this.thisClass, fieldTypeClassName, state.bypassStandardLoading()); //note that the accessor is the owner of the field, i.e., the 'this' class
                            state.ensureInstance_JAVA_CLASS(calc, fieldTypeClass);
                            typeClassRef = state.referenceToInstance_JAVA_CLASS(fieldTypeClass);
                        }
                        field.setFieldValue(JAVA_FIELD_TYPE, typeClassRef);
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

                    //sets override
                    field.setFieldValue(JAVA_ACCESSIBLEOBJECT_OVERRIDE, calc.valBoolean(false));

                    //sets annotations
                    try {
                        final byte[] annotations = this.thisClass.getFieldAnnotationsRaw(sigField);
                        final ClassFile cf_arrayOfBYTE = hier.loadCreateClass("" + ARRAYOF + BYTE);
                        final ReferenceConcrete annotationsRef = state.createArray(calc, null, calc.valInt(annotations.length), cf_arrayOfBYTE);
                        field.setFieldValue(JAVA_FIELD_ANNOTATIONS, annotationsRef);
                        
                        //populates annotations
                        final Array annotationsArray = (Array) state.getObject(annotationsRef);
                        for (int i = 0; i < annotations.length; ++i) {
                            annotationsArray.setFast(calc.valInt(i), calc.valByte(annotations[i]));
                        }
                    } catch (HeapMemoryExhaustedException e) {
                        throwNew(state, calc, OUT_OF_MEMORY_ERROR);
                        exitFromAlgorithm();
                    } catch (FieldNotFoundException | ClassFileNotFoundException | 
                             ClassFileIllFormedException | BadClassFileVersionException | 
                             RenameUnsupportedException | WrongClassNameException | 
                             IncompatibleClassFileException | ClassFileNotAccessibleException | 
                             FastArrayAccessNotAllowedException e) {
                        //this should never happen
                        failExecution(e);
                    }
                    
                    ++index;
                }
                ++slot;
            }


            //returns the array
            state.pushOperand(result);
        };
    }
}

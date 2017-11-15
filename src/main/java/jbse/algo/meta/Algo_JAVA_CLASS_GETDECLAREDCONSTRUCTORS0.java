package jbse.algo.meta;

import static jbse.algo.Util.ensureInstance_JAVA_CLASS;
import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.JAVA_ACCESSIBLEOBJECT_OVERRIDE;
import static jbse.bc.Signatures.JAVA_CLASS;
import static jbse.bc.Signatures.JAVA_CONSTRUCTOR;
import static jbse.bc.Signatures.JAVA_CONSTRUCTOR_ANNOTATIONS;
import static jbse.bc.Signatures.JAVA_CONSTRUCTOR_CLAZZ;
import static jbse.bc.Signatures.JAVA_CONSTRUCTOR_EXCEPTIONTYPES;
import static jbse.bc.Signatures.JAVA_CONSTRUCTOR_MODIFIERS;
import static jbse.bc.Signatures.JAVA_CONSTRUCTOR_PARAMETERTYPES;
import static jbse.bc.Signatures.JAVA_CONSTRUCTOR_SIGNATURE;
import static jbse.bc.Signatures.JAVA_CONSTRUCTOR_SLOT;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.common.Type.ARRAYOF;
import static jbse.common.Type.BYTE;
import static jbse.common.Type.className;
import static jbse.common.Type.isPrimitive;
import static jbse.common.Type.toPrimitiveBinaryClassName;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.splitParametersDescriptors;
import static jbse.common.Type.TYPEEND;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.exc.CannotManageStateException;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.FieldNotFoundException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.common.exc.ClasspathException;
import jbse.dec.exc.DecisionException;
import jbse.mem.Array;
import jbse.mem.Instance;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Calculator;
import jbse.val.Null;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;
import jbse.val.Simplex;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * Meta-level implementation of {@link java.lang.Class#getDeclaredConstructors0(boolean)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_CLASS_GETDECLAREDCONSTRUCTORS0 extends Algo_INVOKEMETA_Nonbranching {
    private ClassFile cf; //set by cookMore

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }

    @Override
    protected void cookMore(State state)
    throws ThreadStackEmptyException, DecisionException, ClasspathException,
    CannotManageStateException, InterruptException {
        try {           
            //gets the binary name of the primitive type and converts it to a string
            final Reference classRef = (Reference) this.data.operand(0);
            final Instance_JAVA_CLASS clazz = (Instance_JAVA_CLASS) state.getObject(classRef);
            final String className = clazz.representedClass();
            this.cf = state.getClassHierarchy().getClassFile(className);
        } catch (ClassCastException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        } catch (BadClassFileException e) {
            //this should never happen
            failExecution(e);
        }
        //TODO check that operands are concrete and kill trace if they are not
    }

    @Override
    protected void update(State state) 
    throws SymbolicValueNotAllowedException, InterruptException, ThreadStackEmptyException {
        //gets the signatures of the fields to emit; the position of the signature
        //in sigFields indicates its slot
        final boolean onlyPublic = ((Simplex) this.data.operand(1)).surelyTrue();
        final List<Signature> sigConstructors;
        try {
            sigConstructors = Arrays.stream(this.cf.getDeclaredConstructors())
            .map(sig -> {
                try {
                    if (onlyPublic && !this.cf.isMethodPublic(sig)) {
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
            if (e.getCause() instanceof FieldNotFoundException) {
                //this should never happen
                failExecution((Exception) e.getCause());
            }
            throw e;
        }

        final int numConstructors = sigConstructors.stream()
        .map(s -> (s == null ? 0 : 1))
        .reduce(0, (a, b) -> a + b);


        //builds the array to return
        ReferenceConcrete result = null; //to keep the compiler happy
        try {
            result = state.createArray(null, state.getCalculator().valInt(numConstructors), "" + ARRAYOF + REFERENCE + JAVA_CONSTRUCTOR + TYPEEND);
        } catch (HeapMemoryExhaustedException e) {
            throwNew(state, OUT_OF_MEMORY_ERROR);
            exitFromAlgorithm();
        } catch (InvalidTypeException e) {
            //this should never happen
            failExecution(e);
        }

        //constructs the java.lang.reflect.Constructor objects and fills the array
        final Reference classRef = (Reference) this.data.operand(0);
        final Array resultArray = (Array) state.getObject(result);
        final Calculator calc = state.getCalculator();
        int index = 0;
        int slot = 0;
        for (Signature sigConstructor : sigConstructors) {
            if (sigConstructor != null) {
                //creates an instance of java.lang.reflect.Constructor and 
                //puts it in the return array
                ReferenceConcrete constructorRef = null; //to keep the compiler happy
                try {
                    constructorRef = state.createInstance(JAVA_CONSTRUCTOR);
                    resultArray.setFast(calc.valInt(index) , constructorRef);
                } catch (HeapMemoryExhaustedException e) {
                    throwNew(state, OUT_OF_MEMORY_ERROR);
                    exitFromAlgorithm();
                } catch (InvalidOperandException | InvalidTypeException | FastArrayAccessNotAllowedException e) {
                    //this should never happen
                    failExecution(e);
                }

                //from here initializes the java.lang.reflect.Constructor instance
                final Instance constructor = (Instance) state.getObject(constructorRef);

                //sets clazz
                constructor.setFieldValue(JAVA_CONSTRUCTOR_CLAZZ, classRef);
                
                //sets slot
                constructor.setFieldValue(JAVA_CONSTRUCTOR_SLOT, calc.valInt(slot));

                //sets parameterTypes
                final String[] params = splitParametersDescriptors(sigConstructor.getDescriptor());
                ReferenceConcrete arrayParamClassesRef = null; //to keep the compiler happy
                try {
                    arrayParamClassesRef = state.createArray(null, calc.valInt(params.length), "" + ARRAYOF + REFERENCE + JAVA_CLASS + TYPEEND);
                } catch (HeapMemoryExhaustedException e) {
                    throwNew(state, OUT_OF_MEMORY_ERROR);
                    exitFromAlgorithm();
                } catch (InvalidTypeException exc) {
                    //this should never happen
                    failExecution(exc);
                }
                constructor.setFieldValue(JAVA_CONSTRUCTOR_PARAMETERTYPES, arrayParamClassesRef);
                final Array arrayParamClasses = (Array) state.getObject(arrayParamClassesRef);
                try {
                    //populates parameterTypes
                    int i = 0;
                    for (String paramType : params) {
                        final ReferenceConcrete paramClazz;
                        if (isPrimitive(paramType)) {
                            final String primType = toPrimitiveBinaryClassName(paramType);
                            try {
                                state.ensureInstance_JAVA_CLASS_primitive(primType);
                            } catch (HeapMemoryExhaustedException e) {
                                throwNew(state, OUT_OF_MEMORY_ERROR);
                                exitFromAlgorithm();
                            } catch (ClassFileNotFoundException e) {
                                //this should never happen
                                failExecution(e);
                            }
                            paramClazz = state.referenceToInstance_JAVA_CLASS_primitive(primType);
                        } else {
                            final String paramTypeClass = className(paramType);
                            ensureInstance_JAVA_CLASS(state, paramTypeClass, paramTypeClass, this.ctx);
                            paramClazz = state.referenceToInstance_JAVA_CLASS(paramTypeClass);
                        }
                        arrayParamClasses.setFast(calc.valInt(i), paramClazz);
                        ++i;
                    }
                } catch (HeapMemoryExhaustedException e) {
                    throwNew(state, OUT_OF_MEMORY_ERROR);
                    exitFromAlgorithm();
                } catch (BadClassFileException e) {
                    //TODO is it ok?
                    throwVerifyError(state);
                    exitFromAlgorithm();
                } catch (ClassFileNotAccessibleException | InvalidTypeException |
                         InvalidOperandException | FastArrayAccessNotAllowedException e) {
                    //this should never happen
                    failExecution(e);
                }

                //sets exceptionTypes
                try {
                    final String[] exceptions = this.cf.getMethodThrownExceptions(sigConstructor);
                    final ReferenceConcrete arrayExcClassesRef = state.createArray(null, calc.valInt(exceptions.length), "" + ARRAYOF + REFERENCE + JAVA_CLASS + TYPEEND);
                    constructor.setFieldValue(JAVA_CONSTRUCTOR_EXCEPTIONTYPES, arrayExcClassesRef);
                    
                    //populates exceptionTypes
                    int i = 0;
                    for (String excType : exceptions) {
                        final String excClassName = className(excType);
                        ensureInstance_JAVA_CLASS(state, excClassName, excClassName, this.ctx);
                        final ReferenceConcrete excClazz = state.referenceToInstance_JAVA_CLASS(excClassName);
                        arrayParamClasses.setFast(calc.valInt(i), excClazz);
                        ++i;
                    }
                } catch (HeapMemoryExhaustedException e) {
                    throwNew(state, OUT_OF_MEMORY_ERROR);
                    exitFromAlgorithm();
                } catch (BadClassFileException e) {
                    //TODO is it ok?
                    throwVerifyError(state);
                    exitFromAlgorithm();
                } catch (MethodNotFoundException | ClassFileNotAccessibleException | InvalidTypeException |
                         InvalidOperandException | FastArrayAccessNotAllowedException e) {
                    //this should never happen
                    failExecution(e);
                }

                //sets modifiers
                try {
                    constructor.setFieldValue(JAVA_CONSTRUCTOR_MODIFIERS, calc.valInt(this.cf.getMethodModifiers(sigConstructor)));
                } catch (MethodNotFoundException e) {
                    //this should never happen
                    failExecution(e);
                }
                
                //sets override
                constructor.setFieldValue(JAVA_ACCESSIBLEOBJECT_OVERRIDE, calc.valBoolean(false));

                //sets signature
                try {
                    final String sigType = this.cf.getMethodGenericSignatureType(sigConstructor);
                    final ReferenceConcrete refSigType;
                    if (sigType == null) {
                        refSigType = Null.getInstance();
                    } else {
                        state.ensureStringLiteral(sigType);
                        refSigType = state.referenceToStringLiteral(sigType);
                    }
                    constructor.setFieldValue(JAVA_CONSTRUCTOR_SIGNATURE, refSigType);
                } catch (HeapMemoryExhaustedException e) {
                    throwNew(state, OUT_OF_MEMORY_ERROR);
                    exitFromAlgorithm();
                } catch (MethodNotFoundException e) {
                    //this should never happen
                    failExecution(e);
                }

                //sets annotations
                try {
                    final byte[] annotations = this.cf.getMethodAnnotationsRaw(sigConstructor);
                    final ReferenceConcrete annotationsRef = state.createArray(null, calc.valInt(annotations.length), "" + ARRAYOF + BYTE);
                    constructor.setFieldValue(JAVA_CONSTRUCTOR_ANNOTATIONS, annotationsRef);
                    
                    //populates annotations
                    final Array annotationsArray = (Array) state.getObject(annotationsRef);
                    for (int i = 0; i < annotations.length; ++i) {
                        annotationsArray.setFast(calc.valInt(i), calc.valByte(annotations[i]));
                    }
                } catch (HeapMemoryExhaustedException e) {
                    throwNew(state, OUT_OF_MEMORY_ERROR);
                    exitFromAlgorithm();
                } catch (MethodNotFoundException | InvalidTypeException | 
                         InvalidOperandException | FastArrayAccessNotAllowedException e) {
                    //this should never happen
                    failExecution(e);
                }
                
                //TODO parameterAnnotations

                ++index;
            }
            ++slot;
        }


        //returns the array
        state.pushOperand(result);
    }
}

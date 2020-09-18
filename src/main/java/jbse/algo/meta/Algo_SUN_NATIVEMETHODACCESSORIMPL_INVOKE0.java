package jbse.algo.meta;

import static jbse.algo.Util.checkOverridingMethodFits;
import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.lookupMethodImplOverriding;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.ClassLoaders.CLASSLOADER_APP;
import static jbse.bc.Offsets.INVOKESPECIALSTATICVIRTUAL_OFFSET;
import static jbse.bc.Signatures.ILLEGAL_ARGUMENT_EXCEPTION;
import static jbse.bc.Signatures.INTERNAL_ERROR;
import static jbse.bc.Signatures.JAVA_BOOLEAN;
import static jbse.bc.Signatures.JAVA_BOOLEAN_VALUE;
import static jbse.bc.Signatures.JAVA_BYTE;
import static jbse.bc.Signatures.JAVA_BYTE_VALUE;
import static jbse.bc.Signatures.JAVA_CHARACTER;
import static jbse.bc.Signatures.JAVA_CHARACTER_VALUE;
import static jbse.bc.Signatures.JAVA_DOUBLE;
import static jbse.bc.Signatures.JAVA_DOUBLE_VALUE;
import static jbse.bc.Signatures.JAVA_FLOAT;
import static jbse.bc.Signatures.JAVA_FLOAT_VALUE;
import static jbse.bc.Signatures.JAVA_INTEGER;
import static jbse.bc.Signatures.JAVA_INTEGER_VALUE;
import static jbse.bc.Signatures.JAVA_LONG;
import static jbse.bc.Signatures.JAVA_LONG_VALUE;
import static jbse.bc.Signatures.JAVA_METHOD_CLAZZ;
import static jbse.bc.Signatures.JAVA_METHOD_PARAMETERTYPES;
import static jbse.bc.Signatures.JAVA_METHOD_SLOT;
import static jbse.bc.Signatures.JAVA_SHORT;
import static jbse.bc.Signatures.JAVA_SHORT_VALUE;
import static jbse.bc.Signatures.JBSE_BASE;
import static jbse.bc.Signatures.JBSE_BASE_BOXINVOCATIONTARGETEXCEPTIONANDRETURN_B;
import static jbse.bc.Signatures.JBSE_BASE_BOXINVOCATIONTARGETEXCEPTIONANDRETURN_C;
import static jbse.bc.Signatures.JBSE_BASE_BOXINVOCATIONTARGETEXCEPTIONANDRETURN_D;
import static jbse.bc.Signatures.JBSE_BASE_BOXINVOCATIONTARGETEXCEPTIONANDRETURN_F;
import static jbse.bc.Signatures.JBSE_BASE_BOXINVOCATIONTARGETEXCEPTIONANDRETURN_I;
import static jbse.bc.Signatures.JBSE_BASE_BOXINVOCATIONTARGETEXCEPTIONANDRETURN_J;
import static jbse.bc.Signatures.JBSE_BASE_BOXINVOCATIONTARGETEXCEPTIONANDRETURN_L;
import static jbse.bc.Signatures.JBSE_BASE_BOXINVOCATIONTARGETEXCEPTIONANDRETURN_NULL;
import static jbse.bc.Signatures.JBSE_BASE_BOXINVOCATIONTARGETEXCEPTIONANDRETURN_S;
import static jbse.bc.Signatures.JBSE_BASE_BOXINVOCATIONTARGETEXCEPTIONANDRETURN_Z;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;
import static jbse.common.Type.ARRAYOF;
import static jbse.common.Type.BOOLEAN;
import static jbse.common.Type.BYTE;
import static jbse.common.Type.CHAR;
import static jbse.common.Type.DOUBLE;
import static jbse.common.Type.FLOAT;
import static jbse.common.Type.INT;
import static jbse.common.Type.LONG;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.SHORT;
import static jbse.common.Type.VOID;
import static jbse.common.Type.isPrimitiveOpStack;
import static jbse.common.Type.splitParametersDescriptors;
import static jbse.common.Type.splitReturnValueDescriptor;
import static jbse.common.Type.toPrimitiveOrVoidInternalName;
import static jbse.common.Type.widens;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.BaseUnsupportedException;
import jbse.algo.exc.MetaUnsupportedException;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.algo.meta.exc.UndefinedResultException;
import jbse.bc.ClassFile;
import jbse.bc.ClassHierarchy;
import jbse.bc.Signature;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.MethodCodeNotFoundException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.bc.exc.NullMethodReceiverException;
import jbse.bc.exc.PleaseLoadClassException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.Array;
import jbse.mem.Array.AccessOutcomeInValue;
import jbse.mem.Frame;
import jbse.mem.Instance;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.InvalidNumberOfOperandsException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.InvalidSlotException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.Simplex;
import jbse.val.Value;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * Meta-level implementation of {@link sun.reflect.NativeMethodAccessorImpl#invoke0(Method, Object, Object[])}.
 * 
 * @author Pietro Braione
 */
//TODO merge with Algo_SUN_NATIVECONSTRUCTORACCESSORIMPL_NEWINSTANCE0
public final class Algo_SUN_NATIVEMETHODACCESSORIMPL_INVOKE0 extends Algo_INVOKEMETA_Nonbranching {
    private ClassFile methodClassFile; //set by cookMore
    private Signature methodSignature; //set by cookMore
    private boolean isNative; //set by cookMore
    private Value[] params; //set by cookMore except for params[0] that is set by updater
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 3;
    }

    @Override
    protected void cookMore(State state) 
    throws InterruptException, UndefinedResultException, 
    SymbolicValueNotAllowedException, ClasspathException, 
    FrozenStateException, InvalidInputException, InvalidTypeException, 
    BaseUnsupportedException, MetaUnsupportedException, ThreadStackEmptyException {
    	final Calculator calc = this.ctx.getCalculator();
        try {
            //gets and check the class where the method belongs to
            final Reference refMethod = (Reference) this.data.operand(0);
            if (state.isNull(refMethod)) {
                //Hotspot crashes with SIGSEGV if the first parameter is null
                throw new UndefinedResultException("The Method m argument to sun.reflect.NativeMethodAccessorImpl.invoke0 was null.");
            }
            final Instance method = (Instance) state.getObject(refMethod);
            final Instance_JAVA_CLASS methodJavaClass = (Instance_JAVA_CLASS) state.getObject((Reference) method.getFieldValue(JAVA_METHOD_CLAZZ));
            this.methodClassFile = methodJavaClass.representedClass();
            
            //gets the parameters types and counts them
            final Array methodParameterTypes = (Array) state.getObject((Reference) method.getFieldValue(JAVA_METHOD_PARAMETERTYPES));
            if (methodParameterTypes == null || !methodParameterTypes.hasSimpleRep()) {
                //this should never happen
                failExecution("The parameterTypes field in a java.lang.reflect.Method object is null or has not simple representation.");
            }
            final int numOfMethodParameterTypes = ((Integer) ((Simplex) methodParameterTypes.getLength()).getActualValue()).intValue();

            //gets the slot
            final Primitive _slot = (Primitive) method.getFieldValue(JAVA_METHOD_SLOT);
            if (_slot.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The slot field in a java.lang.reflect.Method object is symbolic.");
            }
            final int slot = ((Integer) ((Simplex) _slot).getActualValue()).intValue();
            
            //gets the method signature
            final Signature[] declaredMethods = this.methodClassFile.getDeclaredMethods();
            if (slot < 0 || slot >= declaredMethods.length) {
                //invalid slot number
                throwNew(state, calc, INTERNAL_ERROR);
                exitFromAlgorithm();
            }
            this.methodSignature = declaredMethods[slot];
            
            //determines the features of the method
            //TODO resolve/lookup the method ???????
            this.isInterface = false; //TODO is it ok?
            this.isSpecial = false;   //TODO is it ok?
            this.isStatic = this.methodClassFile.isMethodStatic(this.methodSignature);
            this.isNative = this.methodClassFile.isMethodNative(this.methodSignature);

            //determines the number of parameters
            final int numAdditional = (this.isStatic ? 0 : 1);
            final int numOfMethodParametersFormal = 
                splitParametersDescriptors(this.methodSignature.getDescriptor()).length + numAdditional;
            if (numOfMethodParametersFormal != numOfMethodParameterTypes + numAdditional) {
                throwNew(state, calc, INTERNAL_ERROR);
                exitFromAlgorithm();
            }
            
            //gets and checks the parameters
            final Reference refThis = (Reference) this.data.operand(1);
            final Reference refParameters = (Reference) this.data.operand(2);
            if (state.isNull(refThis) && !this.isStatic) {
                throwNew(state, calc, NULL_POINTER_EXCEPTION);
                exitFromAlgorithm();
            }
            if (state.isNull(refParameters) && numOfMethodParametersFormal - numAdditional != 0) {
                throwNew(state, calc, ILLEGAL_ARGUMENT_EXCEPTION);
                exitFromAlgorithm();
            }            
            final Array methodParameters = (Array) state.getObject(refParameters);
            if (methodParameters != null && !methodParameters.hasSimpleRep()) {
                throw new SymbolicValueNotAllowedException("The Object[] args parameter to sun.reflect.NativeMethodAccessorImpl.invoke0 was a symbolic object, or an array without simple representation.");
            }
            final int numOfMethodParametersActual = (methodParameters == null ? 0 : ((Integer) ((Simplex) methodParameters.getLength()).getActualValue()).intValue()) + numAdditional;
            if (numOfMethodParametersFormal != numOfMethodParametersActual) {
                throwNew(state, calc, ILLEGAL_ARGUMENT_EXCEPTION);
                exitFromAlgorithm();
            }
            
            //scans the parameters and checks/unboxes/widens them
            this.params = new Value[numOfMethodParametersActual];
            if (!this.isStatic) {
                final Value actualConverted = checkAndConvert(state, (Reference) method.getFieldValue(JAVA_METHOD_CLAZZ), refThis);
                this.params[0] = actualConverted;
            }
            for (int i = 0; i < numOfMethodParametersActual - numAdditional; ++i) {
                final Reference refTypeFormal = (Reference) ((AccessOutcomeInValue) methodParameterTypes.getFast(calc, calc.valInt(i))).getValue();
                final Reference refValActual = (Reference) ((AccessOutcomeInValue) methodParameters.getFast(calc, calc.valInt(i))).getValue();
                final Value actualConverted = checkAndConvert(state, refTypeFormal, refValActual);
                this.params[i + numAdditional] = actualConverted;
            }
        } catch (ClassCastException e) {
            throwVerifyError(state, calc); //TODO is it right?
            exitFromAlgorithm();
        } catch (FastArrayAccessNotAllowedException | MethodNotFoundException e) {
            //this should never happen
            failExecution(e);
        }
    }
    
    private Value checkAndConvert(State state, Reference refTypeFormal, Reference refValActual) 
    throws InterruptException, ClasspathException, FrozenStateException {
    	final Calculator calc = this.ctx.getCalculator();
        try {
            final Instance_JAVA_CLASS typeFormalJavaClass = (Instance_JAVA_CLASS) state.getObject(refTypeFormal);
            final ClassFile typeFormal = typeFormalJavaClass.representedClass();
            final Objekt actual = state.getObject(refValActual);
            final ClassFile typeActual = actual.getType();
            if (typeFormal.isPrimitiveOrVoid()) {
                //unboxes the parameter
                final Primitive actualValue;
                switch (typeActual.getClassName()) {
                case JAVA_BOOLEAN:
                    actualValue = (Primitive) actual.getFieldValue(JAVA_BOOLEAN_VALUE);
                    break;
                case JAVA_BYTE:
                    actualValue = (Primitive) actual.getFieldValue(JAVA_BYTE_VALUE);
                    break;
                case JAVA_CHARACTER:
                    actualValue = (Primitive) actual.getFieldValue(JAVA_CHARACTER_VALUE);
                    break;
                case JAVA_DOUBLE:
                    actualValue = (Primitive) actual.getFieldValue(JAVA_DOUBLE_VALUE);
                    break;
                case JAVA_FLOAT:
                    actualValue = (Primitive) actual.getFieldValue(JAVA_FLOAT_VALUE);
                    break;
                case JAVA_INTEGER:
                    actualValue = (Primitive) actual.getFieldValue(JAVA_INTEGER_VALUE);
                    break;
                case JAVA_LONG:
                    actualValue = (Primitive) actual.getFieldValue(JAVA_LONG_VALUE);
                    break;
                case JAVA_SHORT:
                    actualValue = (Primitive) actual.getFieldValue(JAVA_SHORT_VALUE);
                    break;
                default:
                    throwNew(state, calc, ILLEGAL_ARGUMENT_EXCEPTION);
                    exitFromAlgorithm();
                    return null; //to keep the compiler happy
                }
                
                //possibly widens the unboxed value and returns it
                final char typeFormalPrimitive = toPrimitiveOrVoidInternalName(typeFormal.getClassName());
                final char destinationType = (isPrimitiveOpStack(typeFormalPrimitive) ? typeFormalPrimitive : INT);
                final char typeActualValue = actualValue.getType();
                if (destinationType == typeActualValue) {
                    return actualValue;
                } else if (widens(destinationType, typeActualValue)) {
                    try {
                        return calc.push(actualValue).widen(destinationType).pop();
                    } catch (InvalidTypeException | InvalidOperandException e) {
                        //this should never happen
                        failExecution(e);
                    }
                } else {
                    throwNew(state, calc, ILLEGAL_ARGUMENT_EXCEPTION);
                    exitFromAlgorithm();
                }
            } else { //the formal parameter has reference type
                if (state.getClassHierarchy().isAssignmentCompatible(typeActual, typeFormal)) {
                    return refValActual;
                } else {
                    throwNew(state, calc, ILLEGAL_ARGUMENT_EXCEPTION);
                    exitFromAlgorithm();
                }
            }
        } catch (ClassCastException e) {
            //this should never happen
            failExecution(e);
        }
        return null; //to keep the compiler happy
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
        	final Calculator calc = this.ctx.getCalculator();
            final ClassHierarchy hier = state.getClassHierarchy();
            try {
                //pushes the frame for the method that boxes 
                //the exceptions raised by the method to be
                //invoked
                final ClassFile cf_JBSE_BASE = hier.loadCreateClass(CLASSLOADER_APP, JBSE_BASE, state.bypassStandardLoading());
                final char returnType = splitReturnValueDescriptor(this.methodSignature.getDescriptor()).charAt(0);
                switch (returnType) {
                case ARRAYOF:
                case REFERENCE:
                    state.pushFrame(calc, cf_JBSE_BASE, JBSE_BASE_BOXINVOCATIONTARGETEXCEPTIONANDRETURN_L, false, this.pcOffset);
                    break;
                case BOOLEAN:
                    state.pushFrame(calc, cf_JBSE_BASE, JBSE_BASE_BOXINVOCATIONTARGETEXCEPTIONANDRETURN_Z, false, this.pcOffset);
                    break;
                case BYTE:
                    state.pushFrame(calc, cf_JBSE_BASE, JBSE_BASE_BOXINVOCATIONTARGETEXCEPTIONANDRETURN_B, false, this.pcOffset);
                    break;
                case CHAR:
                    state.pushFrame(calc, cf_JBSE_BASE, JBSE_BASE_BOXINVOCATIONTARGETEXCEPTIONANDRETURN_C, false, this.pcOffset);
                    break;
                case DOUBLE:
                    state.pushFrame(calc, cf_JBSE_BASE, JBSE_BASE_BOXINVOCATIONTARGETEXCEPTIONANDRETURN_D, false, this.pcOffset);
                    break;
                case FLOAT:
                    state.pushFrame(calc, cf_JBSE_BASE, JBSE_BASE_BOXINVOCATIONTARGETEXCEPTIONANDRETURN_F, false, this.pcOffset);
                    break;
                case INT:
                    state.pushFrame(calc, cf_JBSE_BASE, JBSE_BASE_BOXINVOCATIONTARGETEXCEPTIONANDRETURN_I, false, this.pcOffset);
                    break;
                case LONG:
                    state.pushFrame(calc, cf_JBSE_BASE, JBSE_BASE_BOXINVOCATIONTARGETEXCEPTIONANDRETURN_J, false, this.pcOffset);
                    break;
                case SHORT:
                    state.pushFrame(calc, cf_JBSE_BASE, JBSE_BASE_BOXINVOCATIONTARGETEXCEPTIONANDRETURN_S, false, this.pcOffset);
                    break;
                case VOID:
                    state.pushFrame(calc, cf_JBSE_BASE, JBSE_BASE_BOXINVOCATIONTARGETEXCEPTIONANDRETURN_NULL, false, this.pcOffset);
                    break;
                }
            } catch (ClassFileNotFoundException | ClassFileIllFormedException | BadClassFileVersionException | 
                     RenameUnsupportedException | WrongClassNameException | IncompatibleClassFileException | 
                     ClassFileNotAccessibleException | PleaseLoadClassException | NullMethodReceiverException | 
                     MethodNotFoundException | MethodCodeNotFoundException | InvalidSlotException | InvalidProgramCounterException e) {
                //this should never happen
                //TODO really?
                failExecution(e);
            }
            
            //determines whether the method is overridden; we do that now because
            //we need to push on the operand stack the converted parameters in case
            //the method is meta-overridden
            try {
                final Signature methodSignatureOverriding = lookupMethodImplOverriding(state, this.ctx, this.methodClassFile, this.methodSignature, this.isInterface, this.isSpecial, this.isStatic, this.isNative, false);
                if (methodSignatureOverriding != null) {
                    final ClassFile classFileMethodOverriding = hier.getClassFileClassArray(CLASSLOADER_APP, methodSignatureOverriding.getClassName()); //if lookup had success, the overriding class is already loaded
                    checkOverridingMethodFits(state, this.methodClassFile, this.methodSignature, classFileMethodOverriding, methodSignatureOverriding);
                    this.methodClassFile = classFileMethodOverriding;
                    this.methodSignature = methodSignatureOverriding;
                }
            } catch (InterruptException e) {
                //the method is meta-overridden: push back the parameters on the operand stack
                final Frame currentFrame = state.getCurrentFrame();
                for (Value param : this.params) {
                    currentFrame.push(param);
                }
                throw e;
            } catch (MethodNotFoundException e) {
                throw new BaseUnsupportedException(e);
            } catch (InvalidNumberOfOperandsException e) {
				//this should never happen
				failExecution(e);
			}
            
            try {
                state.pushFrame(calc, this.methodClassFile, this.methodSignature, false, INVOKESPECIALSTATICVIRTUAL_OFFSET, this.params);
            } catch (MethodCodeNotFoundException e) {
                throw new BaseUnsupportedException(e);
            } catch (NullMethodReceiverException | MethodNotFoundException |
                     InvalidSlotException | InvalidProgramCounterException e) {
                //this should never happen
                //TODO really?
                failExecution(e);
            }
        };
    }
    
    @Override
    protected Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }

    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> 0; //nothing to add to the program counter of the pushed frame
    }
}

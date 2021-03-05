package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.algo.Util.checkOverridingMethodFits;
import static jbse.algo.Util.lookupMethodImplOverriding;
import static jbse.bc.ClassLoaders.CLASSLOADER_APP;
import static jbse.bc.Offsets.INVOKESPECIALSTATICVIRTUAL_OFFSET;
import static jbse.bc.Signatures.ILLEGAL_ARGUMENT_EXCEPTION;
import static jbse.bc.Signatures.INSTANTIATION_EXCEPTION;
import static jbse.bc.Signatures.INTERNAL_ERROR;
import static jbse.bc.Signatures.JAVA_BOOLEAN;
import static jbse.bc.Signatures.JAVA_BOOLEAN_VALUE;
import static jbse.bc.Signatures.JAVA_BYTE;
import static jbse.bc.Signatures.JAVA_BYTE_VALUE;
import static jbse.bc.Signatures.JAVA_CHARACTER;
import static jbse.bc.Signatures.JAVA_CHARACTER_VALUE;
import static jbse.bc.Signatures.JAVA_CONSTRUCTOR_CLAZZ;
import static jbse.bc.Signatures.JAVA_CONSTRUCTOR_PARAMETERTYPES;
import static jbse.bc.Signatures.JAVA_CONSTRUCTOR_SLOT;
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
import static jbse.bc.Signatures.JBSE_BASE_BOXINVOCATIONTARGETEXCEPTIONANDRETURN_V;
import static jbse.bc.Signatures.JBSE_BASE_BOXINVOCATIONTARGETEXCEPTIONANDRETURN_Z;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
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
import jbse.mem.Frame;
import jbse.mem.Array.AccessOutcomeInValue;
import jbse.mem.Instance;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.InvalidNumberOfOperandsException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.InvalidSlotException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;
import jbse.val.Simplex;
import jbse.val.Value;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * Meta-level implementation of {@link sun.reflect.NativeMethodAccessorImpl#invoke0(Method, Object, Object[])}
 * and {@link sun.reflect.NativeConstructorAccessorImpl#newInstance0(Constructor, Object[])}.
 * 
 * @author Pietro Braione
 */
abstract class Algo_SUN_NATIVEXACCESSORIMPL_X0 extends Algo_INVOKEMETA_Nonbranching {
	private final boolean isMethod;                  //set by constructor
	private final String classAndMethodName;         //set by constructor
	private final String javaReflectObject;          //set by constructor
	private final String javaReflectObjectParamName; //set by constructor
	private final Signature signatureClazz;          //set by constructor
	private final Signature signatureParameterTypes; //set by constructor
	private final Signature signatureSlot;           //set by constructor
	private ClassFile methodConstructorClassFile;    //set by cookMore
	private Signature methodConstructorSignature;    //set by cookMore
	private boolean isNative;                        //set by cookMore
	private Value[] params;                          //set by cookMore except for params[0] that is set by updater in the case of constructor
    
    public Algo_SUN_NATIVEXACCESSORIMPL_X0(boolean isMethod) {
    	this.isMethod = isMethod;
    	if (isMethod) {
    		this.classAndMethodName = "NativeMethodAccessorImpl.invoke0";
    		this.javaReflectObject = "Method";
    		this.javaReflectObjectParamName = "m";
    		this.signatureClazz = JAVA_METHOD_CLAZZ;
    		this.signatureParameterTypes = JAVA_METHOD_PARAMETERTYPES;
    		this.signatureSlot = JAVA_METHOD_SLOT;
    	} else {
    		this.classAndMethodName = "NativeConstructorAccessorImpl.newInstance0";
    		this.javaReflectObject = "Constructor";
    		this.javaReflectObjectParamName = "c";
    		this.signatureClazz = JAVA_CONSTRUCTOR_CLAZZ;
    		this.signatureParameterTypes = JAVA_CONSTRUCTOR_PARAMETERTYPES;
    		this.signatureSlot = JAVA_CONSTRUCTOR_SLOT;
    	}
    }
    
    @Override
    protected final void cookMore(State state) 
    throws InterruptException, UndefinedResultException, 
    SymbolicValueNotAllowedException, ClasspathException, 
    FrozenStateException, InvalidInputException, InvalidTypeException, 
    BaseUnsupportedException, MetaUnsupportedException, ThreadStackEmptyException {
    	final Calculator calc = this.ctx.getCalculator();
        try {
            //gets and check the class the method or constructor belongs to
            final Reference refConstructorMethod = (Reference) this.data.operand(0);
            if (state.isNull(refConstructorMethod)) {
                //Hotspot crashes with SIGSEGV if the first parameter is null
                throw new UndefinedResultException("The " + this.javaReflectObject + " " + this.javaReflectObjectParamName + " argument to sun.reflect." + this.classAndMethodName + " was null");
            }
            final Instance methodConstructor = (Instance) state.getObject(refConstructorMethod);
            final Instance_JAVA_CLASS methodConstructorJavaClass = (Instance_JAVA_CLASS) state.getObject((Reference) methodConstructor.getFieldValue(this.signatureClazz));
            this.methodConstructorClassFile = methodConstructorJavaClass.representedClass();
            
            if (!this.isMethod) {
                //constructor-specific checks
                if (this.methodConstructorClassFile.isAbstract()) {
                    throwNew(state, calc, INSTANTIATION_EXCEPTION);
                    exitFromAlgorithm();
                }
                if (this.methodConstructorClassFile.isEnum()) {
                    //don't know what Hotspot does if the constructor is that of an enum,
                    //but we will suppose it crashes
                    throw new UndefinedResultException("The first argument to sun.reflect.NativeConstructorAccessorImpl.newInstance0 was the constructor of an enum class");
                }
            }
            
            //gets the parameters types and counts them
            final Array parameterTypes = (Array) state.getObject((Reference) methodConstructor.getFieldValue(this.signatureParameterTypes));
            if (parameterTypes == null || !parameterTypes.hasSimpleRep()) {
                //this should never happen
                failExecution("The parameterTypes field in a java.lang.reflect." + this.javaReflectObject + " object is null or has not simple representation");
            }
            final int numOfParameterTypes = ((Integer) ((Simplex) parameterTypes.getLength()).getActualValue()).intValue();

            //gets the slot
            final Primitive _slot = (Primitive) methodConstructor.getFieldValue(this.signatureSlot);
            if (_slot.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The slot field in a java.lang.reflect." + this.javaReflectObject + " object is symbolic");
            }
            final int slot = ((Integer) ((Simplex) _slot).getActualValue()).intValue();
            
            //gets the signature
            final Signature[] declaredMethodsConstructors = (this.isMethod ? this.methodConstructorClassFile.getDeclaredMethods() : this.methodConstructorClassFile.getDeclaredConstructors());
            if (slot < 0 || slot >= declaredMethodsConstructors.length) {
                //invalid slot number
                throwNew(state, calc, INTERNAL_ERROR);
                exitFromAlgorithm();
            }
            this.methodConstructorSignature = declaredMethodsConstructors[slot];
            
            //determines the features of the method or constructor
            this.isInterface = false;        //TODO is it ok for methods?
            this.isSpecial = !this.isMethod; //TODO is it ok for methods?
            this.isStatic = (this.isMethod ? this.methodConstructorClassFile.isMethodStatic(this.methodConstructorSignature) : false);
            this.isNative = (this.isMethod ? this.methodConstructorClassFile.isMethodNative(this.methodConstructorSignature) : false); 

            //determines the number of parameters
            final int numAdditional = (this.isStatic ? 0 : 1);
            final int numOfParametersFormal = 
                splitParametersDescriptors(this.methodConstructorSignature.getDescriptor()).length + numAdditional;
            if (numOfParametersFormal != numOfParameterTypes + numAdditional) {
                throwNew(state, calc, INTERNAL_ERROR);
                exitFromAlgorithm();
            }
            
            //gets and checks the parameters
            final Reference refThis = (this.isMethod ? (Reference) this.data.operand(1) : null);
            final Reference refParameters = (Reference) this.data.operand(this.isMethod ? 2 : 1);
            if (this.isMethod && !this.isStatic && state.isNull(refThis)) {
                throwNew(state, calc, NULL_POINTER_EXCEPTION);
                exitFromAlgorithm();
            }
            if (state.isNull(refParameters) && numOfParametersFormal - numAdditional != 0) {
                throwNew(state, calc, ILLEGAL_ARGUMENT_EXCEPTION);
                exitFromAlgorithm();
            }            
            final Array parameters = (Array) state.getObject(refParameters);
            if (parameters != null && !parameters.hasSimpleRep()) {
                throw new SymbolicValueNotAllowedException("The Object[] args parameter to sun.reflect." + this.classAndMethodName + " was a symbolic object, or an array without simple representation.");
            }
            final int numOfParametersActual = (parameters == null ? 0 : ((Integer) ((Simplex) parameters.getLength()).getActualValue()).intValue()) + numAdditional;
            if (numOfParametersFormal != numOfParametersActual) {
                throwNew(state, calc, ILLEGAL_ARGUMENT_EXCEPTION);
                exitFromAlgorithm();
            }
            
            //scans the parameters and checks/unboxes/widens them
            this.params = new Value[numOfParametersActual];
            if (this.isMethod && !this.isStatic) {
                final Value actualConverted = checkAndConvert(state, (Reference) methodConstructor.getFieldValue(JAVA_METHOD_CLAZZ), refThis);
                this.params[0] = actualConverted;
            }
            for (int i = 0; i < numOfParametersActual - numAdditional; ++i) {
                final Reference refTypeFormal = (Reference) ((AccessOutcomeInValue) parameterTypes.getFast(calc, calc.valInt(i))).getValue();
                final Reference refValActual = (Reference) ((AccessOutcomeInValue) parameters.getFast(calc, calc.valInt(i))).getValue();
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
            
            if (!this.isMethod) {
                try {
                    //creates the new object in the heap
    	            final ReferenceConcrete refNew = state.createInstance(calc, this.methodConstructorClassFile);
    	            state.pushOperand(refNew);
    	            this.params[0] = refNew;
                } catch (HeapMemoryExhaustedException e) {
                    throwNew(state, calc, OUT_OF_MEMORY_ERROR);
                    exitFromAlgorithm();
                }
            }
            
            try {
                //pushes the frame for the method that boxes 
                //the exceptions raised by the method to be
                //invoked
                final ClassFile cf_JBSE_BASE = hier.loadCreateClass(CLASSLOADER_APP, JBSE_BASE, state.bypassStandardLoading());
                if (this.isMethod) {
                	final char returnType = splitReturnValueDescriptor(this.methodConstructorSignature.getDescriptor()).charAt(0);
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
                } else {
                	state.pushFrame(calc, cf_JBSE_BASE, JBSE_BASE_BOXINVOCATIONTARGETEXCEPTIONANDRETURN_V, false, this.pcOffset);
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
                final Signature methodSignatureOverriding = lookupMethodImplOverriding(state, this.ctx, this.methodConstructorClassFile, this.methodConstructorSignature, this.isInterface, this.isSpecial, this.isStatic, this.isNative, false);
                if (methodSignatureOverriding != null) {
                    final ClassFile classFileMethodOverriding = hier.getClassFileClassArray(CLASSLOADER_APP, methodSignatureOverriding.getClassName()); //if lookup had success, the overriding class is already loaded
                    checkOverridingMethodFits(state, this.methodConstructorClassFile, this.methodConstructorSignature, classFileMethodOverriding, methodSignatureOverriding);
                    this.methodConstructorClassFile = classFileMethodOverriding;
                    this.methodConstructorSignature = methodSignatureOverriding;
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
            
            //pushes the frame of the method
            try {
                state.pushFrame(calc, this.methodConstructorClassFile, this.methodConstructorSignature, false, INVOKESPECIALSTATICVIRTUAL_OFFSET, this.params);
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
    protected final Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }

    @Override
    protected final Supplier<Integer> programCounterUpdate() {
        return () -> 0; //nothing to add to the program counter of the pushed frame
    }
}

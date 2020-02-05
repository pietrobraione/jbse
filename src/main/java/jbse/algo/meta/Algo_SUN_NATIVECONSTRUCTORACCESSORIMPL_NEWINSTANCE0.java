package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
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
import static jbse.bc.Signatures.JAVA_SHORT;
import static jbse.bc.Signatures.JAVA_SHORT_VALUE;
import static jbse.bc.Signatures.JBSE_BASE;
import static jbse.bc.Signatures.JBSE_BASE_BOXINVOCATIONTARGETEXCEPTIONANDRETURN_V;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.common.Type.INT;
import static jbse.common.Type.isPrimitiveOpStack;
import static jbse.common.Type.splitParametersDescriptors;
import static jbse.common.Type.toPrimitiveOrVoidInternalName;
import static jbse.common.Type.widens;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.algo.meta.exc.UndefinedResultException;
import jbse.bc.ClassFile;
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
import jbse.mem.Instance;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.InvalidSlotException;
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
 * Meta-level implementation of {@link sun.reflect.NativeConstructorAccessorImpl#newInstance0(Constructor, Object[])}.
 * 
 * @author Pietro Braione
 */
//TODO merge with Algo_SUN_NATIVEMETHODACCESSORIMPL_INVOKE0
public final class Algo_SUN_NATIVECONSTRUCTORACCESSORIMPL_NEWINSTANCE0 extends Algo_INVOKEMETA_Nonbranching {
    private ClassFile constructorClassFile; //set by cookMore
    private Signature constructorSignature; //set by cookMore
    private Value[] params; //set by cookMore except for params[0] that is set by updater
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }

    @Override
    protected void cookMore(State state) 
    throws InterruptException, UndefinedResultException, 
    SymbolicValueNotAllowedException, ClasspathException, 
    FrozenStateException, InvalidInputException, InvalidTypeException {
    	final Calculator calc = this.ctx.getCalculator();
        try {
            //gets and check the class of the object that must be created
            final Reference refConstructor = (Reference) this.data.operand(0);
            if (state.isNull(refConstructor)) {
                //Hotspot crashes with SIGSEGV if the first parameter is null
                throw new UndefinedResultException("The Constructor<?> c argument to sun.reflect.NativeConstructorAccessorImpl.newInstance0 was null.");
            }
            final Instance constructor = (Instance) state.getObject(refConstructor);
            final Instance_JAVA_CLASS constructorJavaClass = (Instance_JAVA_CLASS) state.getObject((Reference) constructor.getFieldValue(JAVA_CONSTRUCTOR_CLAZZ));
            this.constructorClassFile = constructorJavaClass.representedClass();
            if (this.constructorClassFile.isAbstract()) {
                throwNew(state, calc, INSTANTIATION_EXCEPTION);
                exitFromAlgorithm();
            }
            if (this.constructorClassFile.isEnum()) {
                //don't know what Hotspot does if the constructor is that of an enum,
                //but we will suppose it crashes
                throw new UndefinedResultException("The first argument to sun.reflect.NativeConstructorAccessorImpl.newInstance0 was the constructor of an enum class.");
            }
            
            //gets the parameters types
            final Array constructorParameterTypes = (Array) state.getObject((Reference) constructor.getFieldValue(JAVA_CONSTRUCTOR_PARAMETERTYPES));
            if (constructorParameterTypes == null || !constructorParameterTypes.hasSimpleRep()) {
                //this should never happen
                failExecution("The parameterTypes field in a java.lang.reflect.Constructor object is null or has not simple representation.");
            }
            final int numOfConstructorParametersTypes = ((Integer) ((Simplex) constructorParameterTypes.getLength()).getActualValue()).intValue();

            //gets the slot
            final Primitive _slot = (Primitive) constructor.getFieldValue(JAVA_CONSTRUCTOR_SLOT);
            if (_slot.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The slot field in a java.lang.reflect.Constructor object is symbolic.");
            }
            final int slot = ((Integer) ((Simplex) _slot).getActualValue()).intValue();
            
            //gets the constructor signature
            final Signature[] declaredConstructors = this.constructorClassFile.getDeclaredConstructors();
            if (slot < 0 || slot >= declaredConstructors.length) {
                //invalid slot number
                throwNew(state, calc, INTERNAL_ERROR);
                exitFromAlgorithm();
            }            
            this.constructorSignature = declaredConstructors[slot];

            //determines the number of parameters
            final int numOfConstructorParametersFormal =
                splitParametersDescriptors(this.constructorSignature.getDescriptor()).length + 1; //one more for "this"
            if (numOfConstructorParametersFormal != numOfConstructorParametersTypes + 1) {
                throwNew(state, calc, INTERNAL_ERROR);
                exitFromAlgorithm();
            }

            //gets and checks the parameters
            final Reference refParameters = (Reference) this.data.operand(1);
            if (state.isNull(refParameters) && numOfConstructorParametersFormal != 1) {
                throwNew(state, calc, ILLEGAL_ARGUMENT_EXCEPTION);
                exitFromAlgorithm();
            }
            final Array constructorParameters = (Array) state.getObject(refParameters);
            if (constructorParameters != null && !constructorParameters.hasSimpleRep()) {
                throw new SymbolicValueNotAllowedException("The Object[] args argument to sun.reflect.NativeConstructorAccessorImpl.newInstance0 was a symbolic object, or an array without simple representation.");
            }
            final int numOfConstructorParametersActual = (constructorParameters == null ? 0 : ((Integer) ((Simplex) constructorParameters.getLength()).getActualValue()).intValue()) + 1; //one more for "this"
            if (numOfConstructorParametersFormal != numOfConstructorParametersActual) {
                throwNew(state, calc, ILLEGAL_ARGUMENT_EXCEPTION);
                exitFromAlgorithm();
            }
            
            //scans the parameters and checks/unboxes/widens them
            this.params = new Value[numOfConstructorParametersActual];
            for (int i = 0; i < numOfConstructorParametersActual - 1; ++i) {
                final Reference refTypeFormal = (Reference) ((AccessOutcomeInValue) constructorParameterTypes.getFast(calc, calc.valInt(i))).getValue();
                final Reference refValActual = (Reference) ((AccessOutcomeInValue) constructorParameters.getFast(calc, calc.valInt(i))).getValue();
                final Value actualConverted = checkAndConvert(state, refTypeFormal, refValActual);
                this.params[i + 1] = actualConverted;
            }
        } catch (ClassCastException e) {
            throwVerifyError(state, calc); //TODO is it right?
            exitFromAlgorithm();
        } catch (FastArrayAccessNotAllowedException e) {
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
    	final Calculator calc = this.ctx.getCalculator();
        return (state, alt) -> {
            try {
                //creates the new object in the heap
                final ReferenceConcrete refNew = state.createInstance(calc, this.constructorClassFile);
                state.pushOperand(refNew);
                this.params[0] = refNew;

                //pushes the frames for the constructor and for the 
                //method that boxes the exceptions raised by the constructor
                final ClassFile cf_JBSE_BASE = state.getClassHierarchy().loadCreateClass(CLASSLOADER_APP, JBSE_BASE, state.bypassStandardLoading());
                state.pushFrame(calc, cf_JBSE_BASE, JBSE_BASE_BOXINVOCATIONTARGETEXCEPTIONANDRETURN_V, false, this.pcOffset);
                state.pushFrame(calc, this.constructorClassFile, this.constructorSignature, false, INVOKESPECIALSTATICVIRTUAL_OFFSET, this.params);
            } catch (HeapMemoryExhaustedException e) {
                throwNew(state, calc, OUT_OF_MEMORY_ERROR);
                exitFromAlgorithm();
            } catch (ClassFileNotFoundException | ClassFileIllFormedException | BadClassFileVersionException | 
                     RenameUnsupportedException | WrongClassNameException | IncompatibleClassFileException | ClassFileNotAccessibleException | 
                     PleaseLoadClassException | NullMethodReceiverException | MethodNotFoundException | 
                     MethodCodeNotFoundException | InvalidSlotException | InvalidProgramCounterException e) {
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

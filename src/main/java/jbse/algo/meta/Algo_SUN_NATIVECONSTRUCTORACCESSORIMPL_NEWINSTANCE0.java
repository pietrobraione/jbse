package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.ClassLoaders.CLASSLOADER_APP;
import static jbse.bc.Signatures.ILLEGAL_ARGUMENT_EXCEPTION;
import static jbse.bc.Signatures.INSTANTIATION_EXCEPTION;
import static jbse.bc.Signatures.JAVA_BOOLEAN;
import static jbse.bc.Signatures.JAVA_BOOLEAN_VALUE;
import static jbse.bc.Signatures.JAVA_BYTE;
import static jbse.bc.Signatures.JAVA_BYTE_VALUE;
import static jbse.bc.Signatures.JAVA_CHARACTER;
import static jbse.bc.Signatures.JAVA_CHARACTER_VALUE;
import static jbse.bc.Signatures.JAVA_DOUBLE;
import static jbse.bc.Signatures.JAVA_DOUBLE_VALUE;
import static jbse.bc.Signatures.JAVA_CONSTRUCTOR_CLAZZ;
import static jbse.bc.Signatures.JAVA_CONSTRUCTOR_PARAMETERTYPES;
import static jbse.bc.Signatures.JAVA_FLOAT;
import static jbse.bc.Signatures.JAVA_FLOAT_VALUE;
import static jbse.bc.Signatures.JAVA_INTEGER;
import static jbse.bc.Signatures.JAVA_INTEGER_VALUE;
import static jbse.bc.Signatures.JAVA_LONG;
import static jbse.bc.Signatures.JAVA_LONG_VALUE;
import static jbse.bc.Signatures.JAVA_SHORT;
import static jbse.bc.Signatures.JAVA_SHORT_VALUE;
import static jbse.bc.Signatures.JBSE_BASE;
import static jbse.bc.Signatures.JBSE_BASE_BOXINVOCATIONTARGETEXCEPTION;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.common.Type.toPrimitiveOrVoidInternalName;
import static jbse.common.Type.VOID;
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
import jbse.bc.exc.WrongClassNameException;
import jbse.common.exc.ClasspathException;
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

public final class Algo_SUN_NATIVECONSTRUCTORACCESSORIMPL_NEWINSTANCE0 extends Algo_INVOKEMETA_Nonbranching {
    private ClassFile constructorClassFile; //set by cookMore
    private String descriptor; //set by cookMore
    private Value[] params; //set by cookMore except for params[0] that is set by updater
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }

    @Override
    protected void cookMore(State state) 
    throws InterruptException, UndefinedResultException, 
    SymbolicValueNotAllowedException, ClasspathException, FrozenStateException {
        try {
            //gets and check the class of the object that must be created
            final Reference refConstructor = (Reference) this.data.operand(0);
            if (state.isNull(refConstructor)) {
                //Hotspot crashes with SIGSEGV if the first parameter is null
                throw new UndefinedResultException("the first argument to sun.reflect.NativeConstructorAccessorImpl.newInstance0 was null");
            }
            final Instance constructor = (Instance) state.getObject(refConstructor);
            final Instance_JAVA_CLASS constructorJavaClass = (Instance_JAVA_CLASS) state.getObject((Reference) constructor.getFieldValue(JAVA_CONSTRUCTOR_CLAZZ));
            this.constructorClassFile = constructorJavaClass.representedClass();
            if (this.constructorClassFile.isAbstract()) {
                throwNew(state, INSTANTIATION_EXCEPTION);
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
            final int numOfConstructorParametersFormal = ((Integer) ((Simplex) constructorParameterTypes.getLength()).getActualValue()).intValue();

            final Calculator calc = state.getCalculator();
            
            //reconstructs the descriptor of the constructor
            final StringBuilder sbDescriptor = new StringBuilder("(");
            for (int i = 0; i < numOfConstructorParametersFormal; ++i) {
                final Reference typeFormalJavaClassReference = (Reference) ((AccessOutcomeInValue) constructorParameterTypes.getFast(calc.valInt(i))).getValue();
                final Instance_JAVA_CLASS typeFormalJavaClass = (Instance_JAVA_CLASS) state.getObject(typeFormalJavaClassReference);
                final ClassFile typeFormal = typeFormalJavaClass.representedClass();
                sbDescriptor.append(typeFormal.getInternalTypeName());
            }
            sbDescriptor.append(")");
            sbDescriptor.append(VOID);
            this.descriptor = sbDescriptor.toString();
            
            
            //scans the parameters and checks/unboxes/widens them
            final Reference refParameters = (Reference) this.data.operand(1);
            if (state.isNull(refParameters)) {
                if (numOfConstructorParametersFormal == 0) {
                    this.params = new Value[1]; //one for 'this' (the new object)
                } else {
                    throwNew(state, ILLEGAL_ARGUMENT_EXCEPTION);
                    exitFromAlgorithm();
                }
            } else {
                final Array constructorParameters = (Array) state.getObject(refParameters);
                if (constructorParameters == null || !constructorParameters.hasSimpleRep()) {
                    throw new SymbolicValueNotAllowedException("the args argument to an invocation of sun.reflect.NativeConstructorAccessorImpl.newInstance0 was a symbolic object, or an array without simple representation");
                }
                final int numOfConstructorParametersActual = ((Integer) ((Simplex) constructorParameters.getLength()).getActualValue()).intValue();
                if (numOfConstructorParametersFormal == numOfConstructorParametersActual) {
                    this.params = new Value[numOfConstructorParametersActual + 1]; //one more for 'this' (the new object)
                    for (int i = 0; i < numOfConstructorParametersActual; ++i) {
                        final Reference refTypeFormal = (Reference) ((AccessOutcomeInValue) constructorParameterTypes.getFast(calc.valInt(i))).getValue();
                        final Reference refValActual = (Reference) ((AccessOutcomeInValue) constructorParameters.getFast(calc.valInt(i))).getValue();
                        final Value actualConverted = checkAndConvert(state, refTypeFormal, refValActual);
                        this.params[i + 1] = actualConverted;
                    }
                } else {
                    throwNew(state, ILLEGAL_ARGUMENT_EXCEPTION);
                    exitFromAlgorithm();
                }
            }
        } catch (ClassCastException e) {
            throwVerifyError(state); //TODO is it right?
            exitFromAlgorithm();
        } catch (InvalidOperandException | InvalidTypeException | FastArrayAccessNotAllowedException e) {
            //this should never happen
            failExecution(e);
        }
    }
    
    private Value checkAndConvert(State state, Reference refTypeFormal, Reference refValActual) 
    throws InterruptException, ClasspathException, FrozenStateException {
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
                    throwNew(state, ILLEGAL_ARGUMENT_EXCEPTION);
                    exitFromAlgorithm();
                    return null; //to keep the compiler happy
                }
                
                //possibly widens the unboxed value and returns it
                final char typeFormalPrimitive = toPrimitiveOrVoidInternalName(typeFormal.getClassName());
                final char typeActualValue = actualValue.getType();
                if (typeFormalPrimitive == typeActualValue) {
                    return actualValue;
                } else if (widens(typeFormalPrimitive, typeActualValue)) {
                    try {
                        return actualValue.widen(typeFormalPrimitive);
                    } catch (InvalidTypeException e) {
                        //this should never happen
                        failExecution(e);
                    }
                } else {
                    throwNew(state, ILLEGAL_ARGUMENT_EXCEPTION);
                    exitFromAlgorithm();
                }
            } else { //the formal parameter has reference type
                if (state.getClassHierarchy().isAssignmentCompatible(typeActual, typeFormal)) {
                    return refValActual;
                } else {
                    throwNew(state, ILLEGAL_ARGUMENT_EXCEPTION);
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
            try {
                //creates the new object in the heap
                final ReferenceConcrete refNew = state.createInstance(this.constructorClassFile);
                state.pushOperand(refNew);
                this.params[0] = refNew;

                //pushes the frames for the constructor and for the 
                //method that boxes the exceptions raised by the constructor
                final ClassFile cf_JBSE_BASE = state.getClassHierarchy().loadCreateClass(CLASSLOADER_APP, JBSE_BASE, state.bypassStandardLoading());
                state.pushFrame(cf_JBSE_BASE, JBSE_BASE_BOXINVOCATIONTARGETEXCEPTION, false, this.pcOffset);
                final Signature constructorSignature = new Signature(this.constructorClassFile.getClassName(), this.descriptor, "<init>");
                state.pushFrame(this.constructorClassFile, constructorSignature, false, 0, this.params);
            } catch (HeapMemoryExhaustedException e) {
                throwNew(state, OUT_OF_MEMORY_ERROR);
                exitFromAlgorithm();
            } catch (ClassFileNotFoundException | ClassFileIllFormedException | BadClassFileVersionException | 
                     WrongClassNameException | IncompatibleClassFileException | ClassFileNotAccessibleException | 
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

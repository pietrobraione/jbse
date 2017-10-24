package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.ILLEGAL_ARGUMENT_EXCEPTION;
import static jbse.bc.Signatures.INSTANTIATION_EXCEPTION;
import static jbse.bc.Signatures.JAVA_CONSTRUCTOR_CLAZZ;
import static jbse.bc.Signatures.JAVA_CONSTRUCTOR_PARAMETERTYPES;
import static jbse.common.Type.isPrimitive;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.algo.meta.exc.UndefinedResultException;
import jbse.bc.ClassFile;
import jbse.bc.exc.BadClassFileException;
import jbse.mem.Array;
import jbse.mem.Array.AccessOutcomeInValue;
import jbse.mem.Instance;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Calculator;
import jbse.val.Reference;
import jbse.val.Simplex;
import jbse.val.Value;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

public final class Algo_SUN_NATIVECONSTRUCTORACCESSORIMPL_NEWINSTANCE0 extends Algo_INVOKEMETA_Nonbranching {
    private Value[] params; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }

    @Override
    protected void cookMore(State state) throws InterruptException, UndefinedResultException, SymbolicValueNotAllowedException {
        try {
            final Reference refConstructor = (Reference) this.data.operand(0);
            //hotspot crashes with SIGSEGV if the first parameter is null
            if (state.isNull(refConstructor)) {
                throw new UndefinedResultException("the first argument to sun.reflect.NativeConstructorAccessorImpl.newInstance0 was null");
            }
            final Instance constructor = (Instance) state.getObject(refConstructor);
            final Instance_JAVA_CLASS constructorJavaClass = (Instance_JAVA_CLASS) state.getObject((Reference) constructor.getFieldValue(JAVA_CONSTRUCTOR_CLAZZ));
            final String constructorClassName = constructorJavaClass.representedClass();
            final ClassFile constructorClassFile = state.getClassHierarchy().getClassFile(constructorClassName);
            if (constructorClassFile.isAbstract()) {
                throwNew(state, INSTANTIATION_EXCEPTION);
                exitFromAlgorithm();
            }
            //don't know what hotspot does if the constructor is that of an enum,
            //but we will suppose it crashes
            if (constructorClassFile.isEnum()) {
                throw new UndefinedResultException("the first argument to sun.reflect.NativeConstructorAccessorImpl.newInstance0 was the constructor of an enum class");
            }
            
            //scans the parameters and checks/converts/boxes them
            final Array constructorParameterTypes = (Array) state.getObject((Reference) constructorJavaClass.getFieldValue(JAVA_CONSTRUCTOR_PARAMETERTYPES));
            if (constructorParameterTypes == null || !constructorParameterTypes.hasSimpleRep()) {
                //this should never happen
                failExecution("the parameterTypes field in a java.lang.reflect.Constructor object is null or has not simple representation");
            }
            final int numOfConstructorParametersFormal = ((Integer) ((Simplex) constructorParameterTypes.getLength()).getActualValue()).intValue();
            
            final Reference refParameters = (Reference) this.data.operand(1);
            if (state.isNull(refParameters)) {
                if (numOfConstructorParametersFormal == 0) {
                    this.params = new Value[0];
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
                    this.params = new Value[numOfConstructorParametersActual];
                    final Calculator calc = state.getCalculator();
                    for (int i = 0; i < this.params.length; ++i) {
                        final AccessOutcomeInValue typeFormal = (AccessOutcomeInValue) constructorParameterTypes.getFast(calc.valInt(i));
                        final AccessOutcomeInValue actual = (AccessOutcomeInValue) constructorParameters.getFast(calc.valInt(i));
                        final Value actualConverted = checkAndConvert(state, (Reference) typeFormal.getValue(), (Reference) actual.getValue());
                        this.params[i] = actualConverted;
                    }
                } else {
                    throwNew(state, ILLEGAL_ARGUMENT_EXCEPTION);
                    exitFromAlgorithm();
                }
            }
        } catch (ClassCastException e) {
            throwVerifyError(state); //TODO is it right?
            exitFromAlgorithm();
        } catch (BadClassFileException | InvalidOperandException | InvalidTypeException | FastArrayAccessNotAllowedException e) {
            //this should never happen
            failExecution(e);
        }
    }
    
    private Value checkAndConvert(State state, Reference refTypeFormal, Reference refValActual) {
        try {
            final Instance_JAVA_CLASS typeFormalJavaClass = (Instance_JAVA_CLASS) state.getObject(refTypeFormal);
            final String typeFormal = typeFormalJavaClass.representedClass();
            final Objekt actual = state.getObject(refValActual);
            final String typeActual = actual.getType();
            if (isPrimitive(typeFormal)) {
                return null; //TODO
            } else {
                return null; //TODO
            }
        } catch (ClassCastException e) {
            //this should never happen
            failExecution(e);
            return null; //to keep the compiler happy
        }
    }


    @Override
    protected void update(State state) throws ThreadStackEmptyException {
    }
}

package jbse.algo;

import static jbse.algo.Util.failExecution;
import static jbse.common.Type.binaryClassName;
import static jbse.common.Type.parametersNumber;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.Supplier;

import jbse.algo.exc.CannotAccessImplementationReflectively;
import jbse.algo.exc.CannotInvokeNativeException;
import jbse.algo.exc.CannotManageStateException;
import jbse.common.Type;
import jbse.common.exc.ClasspathException;
import jbse.dec.exc.DecisionException;
import jbse.jvm.exc.FailureException;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Calculator;
import jbse.val.FunctionApplication;
import jbse.val.Primitive;
import jbse.val.PrimitiveSymbolic;
import jbse.val.Simplex;
import jbse.val.Value;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;
import jbse.val.exc.ValueDoesNotSupportNativeException;

/**
 * {@link Algo_INVOKEMETA_Nonbranching} implementing the effect of 
 * a method call assuming that the invoked 
 * method is pure, i.e., it does not produce any effect other than 
 * returning a value on the operand stack according to the method's 
 * signature. More precisely:
 * <ul>
 * <li>If the method's return type is {@code void}, then the 
 *     method invocation has no effect;</li>
 * <li>If the method returns a {@link Primitive}, and all its parameters 
 *     are {@link Simplex}, then reflection is used
 *     is used to metacircularly invoke the native method on the reified 
 *     parameters, and the corresponding value is reflected back and pushed 
 *     on the operand stack;</li>
 * <li>If the method returns a {@link Primitive}, and all its parameters 
 *     are {@link Primitive} with some {@link PrimitiveSymbolic}, then a 
 *     {@link FunctionApplication} mirroring the method's invocation is 
 *     pushed on the operand stack;</li>
 * <li>In all the other cases, throws a {@link ValueDoesNotSupportNativeException}.</li>
 * </ul>
 * 
 * @author Pietro Braione
 */
public class Algo_INVOKEMETA_Pure extends Algo_INVOKEMETA_Nonbranching {
    private Value returnValue;
    
    @Override
    protected final Supplier<Integer> numOperands() {
        return () -> {
            return parametersNumber(this.data.signature().getDescriptor(), this.isStatic);
        };
    }
    
    @Override
    protected void cookMore(State state) 
    throws ThreadStackEmptyException, DecisionException, ClasspathException,
    CannotManageStateException, InterruptException {
        final Value[] args = this.data.operands();
        
        //determines the return value
        final String returnType = Type.splitReturnValueDescriptor(this.data.signature().getDescriptor());
        if (Type.isVoid(returnType)) {
            this.returnValue = null;
        } else if (Type.isPrimitive(returnType)) {
            //requires all arguments are primitive
            final Primitive[] argsPrim = new Primitive[args.length];
            boolean someSymbolic = false;
            for (int i = 0; i < args.length; ++i) {
                if (args[i] instanceof Primitive) {
                    argsPrim[i] = (Primitive) args[i];
                    someSymbolic = someSymbolic || (argsPrim[i].isSymbolic());
                } else { 
                    throw new ValueDoesNotSupportNativeException("invoked method " + this.data.signature().toString() + " with args " + Arrays.toString(args));
                }
            }
            if (someSymbolic) {
                try {
                    this.returnValue = state.getCalculator().applyFunction(returnType.charAt(0), this.data.signature().getName(), argsPrim);
                } catch (InvalidOperandException | InvalidTypeException e) {
                    //this should never happen
                    failExecution(e);
                }
            } else {
                invokeMetacircularly(state, Arrays.stream(argsPrim).map(p -> (Simplex) p).toArray(Simplex[]::new));
            }
        } else {
            throw new ValueDoesNotSupportNativeException("invoked method " + this.data.signature().toString() + " with args " + Arrays.toString(args));
            //TODO put reference resolution here or in the invoke* bytecodes and assign returnValue = state.createSymbol(returnType, "__NATIVE[" + state.getIdentifier() + "[" + state.getSequenceNumber() + "]");
        }
    }
    
    private void invokeMetacircularly(State state, Simplex[] args) throws CannotInvokeNativeException {
        try {
            //converts the arguments
            final String[] argsType = Type.splitParametersDescriptors(this.data.signature().getDescriptor());
            final Object[] argsRefl = new Object[args.length];
            final Class<?>[] argsClass = new Class[args.length];
            for (int i = 0; i < args.length; ++i) {
                argsRefl[i] = args[i].getValueForNative();
                argsClass[i] = getClass(argsType[i]);
            }

            //gets the method and invokes it
            final Class<?> c = Class.forName(binaryClassName(this.data.signature().getClassName()));
            final Method m = c.getMethod(this.data.signature().getName(), argsClass);
            final Object retValRefl;
            if (Modifier.isStatic(m.getModifiers())) {
                retValRefl = m.invoke(null, argsRefl);
            } else {
                final Object argThis = argsRefl[0]; //TODO reify argsRefl[0]
                final Object[] argsReflOthers = new Object[argsRefl.length - 1];
                for (int i = 0; i < argsReflOthers.length; ++i) {
                    argsReflOthers[i] = argsRefl[i + 1];
                }
                retValRefl = m.invoke(argThis, argsReflOthers);
            }

            //reifies the return value
            final String retType =  Type.splitReturnValueDescriptor(this.data.signature().getDescriptor());
            this.returnValue = toValue(state.getCalculator(), retValRefl, retType);
        } catch (ClassNotFoundException | SecurityException | 
                 NoSuchMethodException | IllegalArgumentException | 
                 IllegalAccessException | InvocationTargetException e) {
            throw new CannotAccessImplementationReflectively(e);
        }
    }

    private static Class<?> getClass(String type) throws ClassNotFoundException {
        if (type.equals("" + Type.BYTE)) {
            return byte.class;
        } else if (type.equals("" + Type.SHORT)) {
            return short.class;
        } else if (type.equals("" + Type.INT)) {
            return int.class;
        } else if (type.equals("" + Type.LONG)) {
            return long.class;
        } else if (type.equals("" + Type.FLOAT)) {
            return float.class;
        } else if (type.equals("" + Type.DOUBLE)) {
            return double.class;
        } else if (type.equals("" + Type.CHAR)) {
            return char.class;
        } else if (type.equals("" + Type.BOOLEAN)) {
            return boolean.class;
        } else {
            return Class.forName(type);
        }
    }

    private static Value toValue(Calculator calc, Object retValRefl, String type) 
    throws CannotInvokeNativeException {
        if (type.equals("" + Type.VOID)) {
            return null;
        } else if (Type.isPrimitive(type)) {
            return calc.val_(retValRefl);
        } else {
            //TODO reification of objects?
            throw new ValueDoesNotSupportNativeException("cannot reflect metacircularly values with type " + type);
        }
    }
    
    @Override
    protected void update(State state) throws ThreadStackEmptyException, ClasspathException, CannotManageStateException,
    DecisionException, ContradictionException, FailureException, InterruptException {
        if (this.returnValue != null) {
            state.pushOperand(this.returnValue); //TODO possibly widen to integer if it is not an operand stack primitive
        }
    }
}

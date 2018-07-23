package jbse.algo;

import static java.lang.System.arraycopy;

import static jbse.algo.Util.failExecution;
import static jbse.common.Type.binaryClassName;
import static jbse.common.Type.isPrimitive;
import static jbse.common.Type.parametersNumber;
import static jbse.common.Type.splitParametersDescriptors;
import static jbse.common.Type.splitReturnValueDescriptor;

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
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Primitive;
import jbse.val.ReferenceSymbolicApply;
import jbse.val.Simplex;
import jbse.val.Value;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;
import jbse.val.exc.ValueDoesNotSupportNativeException;

/**
 * {@link Algo_INVOKEMETA_Nonbranching} implementing the effect of 
 * a method call assuming that the invoked method does not produce 
 * any effect other than returning a value. More precisely:
 * <ul>
 * <li>If the method's return type is {@code void}, then the 
 *     method invocation has no effect;</li>
 * <li>If the method returns a {@link Primitive}, and all its parameters 
 *     are {@link Simplex}, then reflection is used
 *     is used to metacircularly invoke the native method on the reified 
 *     parameters, and the corresponding value is reflected back and pushed 
 *     on the operand stack;</li>
 * <li>Otherwise, a {@link PrimitiveSymbolicApply} or a {@link ReferenceSymbolicApply} 
 *     mirroring the method's invocation is pushed on the operand stack.</li>
 * </ul>
 * 
 * @author Pietro Braione
 */
public class Algo_INVOKEMETA_OnlyReturn extends Algo_INVOKEMETA_Nonbranching {
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
        } else {
            //checks the parameters
            boolean allSimplex = true;
            for (int i = 0; i < args.length; ++i) {
                if (!(args[i] instanceof Simplex)) {
                    allSimplex = false;
                    break;
                }
            }
            
            if (allSimplex && Type.isPrimitive(returnType)) {
                //delegates to metacircular invocation
                invokeMetacircularly(state, Arrays.stream(args).map(p -> (Simplex) p).toArray(Simplex[]::new));
            } else {
                //otherwise, builds a term
                try {
                    if (isPrimitive(returnType)) {
                        this.returnValue = state.getCalculator().applyFunctionPrimitive(returnType.charAt(0), state.getHistoryPoint(), this.data.signature().toString(), args);
                    } else {
                        this.returnValue = new ReferenceSymbolicApply(returnType, state.getHistoryPoint(), this.data.signature().toString(), args);
                    }
                } catch (InvalidOperandException | InvalidTypeException e) {
                    //this should never happen
                    failExecution(e);
                }
            }
        }
    }
    
    private void invokeMetacircularly(State state, Simplex[] args) throws CannotInvokeNativeException {
        try {
            //converts the arguments
            final String[] argsType = splitParametersDescriptors(this.data.signature().getDescriptor());
            final Object[] argsRefl = new Object[args.length];
            final Class<?>[] argsClass = new Class[args.length];
            for (int i = 0; i < args.length; ++i) {
                argsRefl[i] = args[i].getValueForNative();
                argsClass[i] = getClass(argsType[i]);
            }

            //gets the method and invokes it
            final Class<?> c = Class.forName(binaryClassName(this.data.signature().getClassName()));
            final Method m = c.getDeclaredMethod(this.data.signature().getName(), argsClass);
            m.setAccessible(true);
            final Object retValRefl;
            if (Modifier.isStatic(m.getModifiers())) {
                retValRefl = m.invoke(null, argsRefl);
            } else {
                //TODO currently this block does not work because we do not reify argsRefl[0] to a Java object
                final Object argThis = argsRefl[0]; 
                final Object[] argsReflOthers = new Object[argsRefl.length - 1];
                arraycopy(argsRefl, 1, argsReflOthers, 0, argsReflOthers.length);
                retValRefl = m.invoke(argThis, argsReflOthers);
            }

            //reifies the return value
            final String retType =  splitReturnValueDescriptor(this.data.signature().getDescriptor());
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
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> { 
            if (this.returnValue != null) {
                state.pushOperand(this.returnValue); //TODO possibly widen to integer if it is not an operand stack primitive
            }
        };
    }
}

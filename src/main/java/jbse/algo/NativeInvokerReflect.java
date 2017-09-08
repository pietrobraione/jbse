package jbse.algo;

import static jbse.algo.Util.throwVerifyError;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import jbse.algo.exc.CannotInvokeNativeException;
import jbse.bc.Signature;
import jbse.common.Type;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Calculator;
import jbse.val.Value;
import jbse.val.exc.ValueDoesNotSupportNativeException;

/**
 * Implements native method invocation by invoking an 
 * implementation which must be available through Java reflection 
 * of the virtual machine we are running on, and reflecting back 
 * the return value. Only the method parameters are reified, so 
 * it should be used only with pure functions and values.
 * 
 * @author Pietro Braione
 *
 */
public class NativeInvokerReflect implements NativeInvoker {
	@Override
	public void doInvokeNative(State state, Signature methodSignatureResolved, Value[] args, int pcOffset)
	throws CannotInvokeNativeException, ThreadStackEmptyException {
		try {
			//converts the arguments
			final String[] argsType = Type.splitParametersDescriptors(methodSignatureResolved.getDescriptor());
			final Object[] argsRefl = new Object[args.length];
			final Class<?>[] argsClass = new Class[args.length];
			for (int i = 0; i < args.length; ++i) {
				argsRefl[i] = args[i].getValueForNative();
				argsClass[i] = getClass(argsType[i]);
			}
			
			//gets the method and invokes it
			final Class<?> c = Class.forName(methodSignatureResolved.getClassName().replace('/', '.'));
			final Method m = c.getMethod(methodSignatureResolved.getName(), argsClass);
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
			final String retType =  Type.splitReturnValueDescriptor(methodSignatureResolved.getDescriptor());
			final Value retVal = toValue(state.getCalculator(), retValRefl, retType);

			//pushes the return value on the operand stack
			if (retVal != null) {
				try {
					state.pushOperand(retVal);
				} catch (ThreadStackEmptyException e) {
					state.setStuckReturn(retVal);
				}
			}
		} catch (ClassNotFoundException | SecurityException | 
				NoSuchMethodException | IllegalArgumentException | 
				IllegalAccessException | InvocationTargetException e) {
			//TODO invent some relevant exception?
			throw new CannotInvokeNativeException(e);
		}

		//increments the program counter
		try {
			state.incProgramCounter(pcOffset);
		} catch (InvalidProgramCounterException e) {
		    throwVerifyError(state);
		}
	}
	
	private Class<?> getClass(String type) throws ClassNotFoundException {
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
	
	private Value toValue(Calculator calc, Object retValRefl, String type) 
	throws CannotInvokeNativeException {
	    if (type.equals("" + Type.VOID)) {
            return null;
        } else if (Type.isPrimitive(type)) {
            return calc.val_(retValRefl);
		} else {
			//TODO implement reification of objects
			throw new ValueDoesNotSupportNativeException("cannot reflect metacircularly values with type " + type + ".");
		}
	}
}

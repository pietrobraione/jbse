package jbse.algo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import jbse.Type;
import jbse.Util;
import jbse.bc.Signature;
import jbse.exc.algo.CannotInvokeNativeException;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.mem.Calculator;
import jbse.mem.State;
import jbse.mem.Value;



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
	throws CannotInvokeNativeException, ThreadStackEmptyException, UnexpectedInternalException {
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
					state.push(retVal);
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
			state.incPC(pcOffset);
		} catch (InvalidProgramCounterException e) {
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
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
	throws CannotInvokeNativeException, UnexpectedInternalException {
		if (type.equals("" + Type.BYTE)) {
			return calc.valByte(((Byte) retValRefl).byteValue());
		} else if (type.equals("" + Type.SHORT)) {
			return calc.valShort(((Short) retValRefl).shortValue());
		} else if (type.equals("" + Type.INT)) {
			return calc.valInt(((Integer) retValRefl).intValue());
		} else if (type.equals("" + Type.LONG)) {
			return calc.valLong(((Long) retValRefl).longValue());
		} else if (type.equals("" + Type.FLOAT)) {
			return calc.valFloat(((Float) retValRefl).floatValue());
		} else if (type.equals("" + Type.DOUBLE)) {
			return calc.valDouble(((Double) retValRefl).doubleValue());
		} else if (type.equals("" + Type.CHAR)) {
			return calc.valChar(((Character) retValRefl).charValue());
		} else if (type.equals("" + Type.BOOLEAN)) {
			return calc.valBoolean(((Boolean) retValRefl).booleanValue());
		} else if (type.equals("" + Type.VOID)) {
			return null;
		} else {
			//TODO implement reification of objects
			throw new CannotInvokeNativeException();
		}
	}
}

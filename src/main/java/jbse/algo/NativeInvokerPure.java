package jbse.algo;

import static jbse.algo.Util.throwVerifyError;
import static jbse.common.Type.isPrimitive;

import jbse.algo.exc.CannotInvokeNativeException;
import jbse.bc.Signature;
import jbse.common.Type;
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.PrimitiveSymbolicApply;
import jbse.val.ReferenceSymbolicApply;
import jbse.val.Primitive;
import jbse.val.Value;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;
import jbse.val.exc.ValueDoesNotSupportNativeException;

/**
 * Implements native method invocation by assuming that the invoked 
 * method is pure, i.e., it does not produce any effect other than 
 * returning a value on the operand stack according to the method's 
 * signature. More precisely:
 * <ul>
 * <li>If the method's return type is {@code void}, then the 
 *     method invocation has no effect;</li>
 * <li>If the method's return type is primitive, and all its parameters 
 *     have primitive type and are not symbolic, then {@link NativeInvokerReflect}
 *     is used to execute the native method, and the corresponding value is
 *     pushed on the operand stack;</li>
 * <li>If the method's return type is primitive, and all its parameters 
 *     have primitive type and some is symbolic, then a {@link PrimitiveSymbolicApply}  
 *     mirroring the method's invocation is pushed on the operand stack;</li>
 * <li>In all the other cases, throws a {@link ValueDoesNotSupportNativeException}. 
 * </ul>
 * 
 * @author Pietro Braione
 */
public class NativeInvokerPure implements NativeInvoker {
    final NativeInvokerReflect delegate = new NativeInvokerReflect();
	@Override
	public void doInvokeNative(State state, Signature methodSignatureResolved, Value[] args, int pcOffset) 
	throws CannotInvokeNativeException, ThreadStackEmptyException {
		//determines the return value
		final String returnType = Type.splitReturnValueDescriptor(methodSignatureResolved.getDescriptor());
		final Value returnValue;
		if (Type.isVoid(returnType)) {
			returnValue = null;
		} else {
		        //checks the parameters
			boolean allConcrete = true;
			boolean allPrimitive = true;
			for (int i = 0; i < args.length; ++i) {
			    if (args[i].isSymbolic()) {
			        allConcrete = false;
			    }
				if (args[i] instanceof Primitive) {
				    allPrimitive = false;
				}
			}
			
			//delegates if all parameters are concrete and primitive
			if (allConcrete && allPrimitive) {
                            this.delegate.doInvokeNative(state, methodSignatureResolved, args, pcOffset);
                            return;
                        }
			
			//otherwise, builds a term
			try {
		            if (isPrimitive(returnType)) {
	                            returnValue = state.getCalculator().applyFunctionPrimitive(returnType.charAt(0), state.getHistoryPoint(), methodSignatureResolved.toString(), args);
		            } else {
		                returnValue = new ReferenceSymbolicApply(returnType, state.getHistoryPoint(), methodSignatureResolved.toString(), args);
		            }
			} catch (InvalidOperandException | InvalidTypeException e) {
			    //this should never happen
			    throw new UnexpectedInternalException(e);
			}
		}
		
		//pushes the return value (if present) on the operand stack, 
		//or sets the state to stuck if no current frame exists
		try {
			if (returnValue != null) {
				state.pushOperand(returnValue);
			}
		} catch (ThreadStackEmptyException e) {
			state.setStuckReturn(returnValue);
			return;
		}		

		//increments the program counter
		try {
			state.incProgramCounter(pcOffset);
		} catch (InvalidProgramCounterException e) {
		    throwVerifyError(state);
		}
	}
}

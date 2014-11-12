package jbse.algo;

import jbse.bc.Signature;
import jbse.exc.algo.CannotInvokeNativeException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.mem.State;
import jbse.mem.Value;

/**
 * A strategy whose responsibility is to produce the effects of a 
 * native method invocation.
 * 
 * @author Pietro Braione
 */
public interface NativeInvoker {
	/**
	 * Produces the result of a native method invocation.
	 * 
	 * @param state
     *        the {@link State} which must be modified.
	 * @param methodSignatureResolved
     *        the resolved {@link Signature} of the method which 
     *        must be invoked.
	 * @param args
     *        an array of {@link Value}s which are the arguments 
     *        of the invocation.
	 * @param pcOffset 
	 *        the offset of the program counter after invocation.
	 * @throws CannotInvokeNativeException whenever the preconditions of
	 *         {@code doInvokeNative} are violated.
	 * @throws ThreadStackEmptyException 
	 */
	void doInvokeNative(State state, Signature methodSignatureResolved, Value[] args, int pcOffset) 
	throws CannotInvokeNativeException, ThreadStackEmptyException;
}

package jbse.algo;

import static jbse.algo.Util.throwVerifyError;

import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Primitive;
import jbse.val.Value;
import jbse.val.exc.InvalidTypeException;

/**
 * Command implementing all the *2* bytecodes (i2[b/s/l/f/d/c], l2[i/f/d], f2[i/l/d], d2[i/l/f]).
 * 
 * @author Pietro Braione
 *
 */
final class Algo_X2Y implements Algorithm {
	char fromType;
	char toType;

	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ThreadStackEmptyException {
		//pops the value on the operand stack
	    final Value value;
	    try {
	        value = state.popOperand();
	    } catch (OperandStackEmptyException e) {
	        throwVerifyError(state);
	        return;
	    }
		
		//casts the value
		final Value castValue;
		if (value instanceof Primitive) {
			try {
			    final Primitive fromValue = (Primitive) value;
			    if (fromValue.getType() != this.fromType) {
	                throwVerifyError(state);
	                return;
			    }
				castValue = fromValue.to(this.toType);
			} catch (InvalidTypeException e) {
	            throwVerifyError(state);
				return;
			}
		} else {
            throwVerifyError(state);
			return;
		}
		
		//pushes the obtained value
		state.pushOperand(castValue);

		//increments the program counter
		try {
			state.incPC();
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
		}
	} 
}